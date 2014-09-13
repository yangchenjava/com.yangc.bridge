package com.yangc.bridge.comm.handler;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yangc.bridge.bean.ResultBean;
import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.bean.TBridgeFile;
import com.yangc.bridge.bean.UserBean;
import com.yangc.bridge.comm.cache.SessionCache;
import com.yangc.bridge.comm.protocol.ContentType;
import com.yangc.bridge.comm.protocol.TransmitStatus;
import com.yangc.bridge.comm.protocol.prototype.ProtocolHeart;
import com.yangc.bridge.service.ChatService;
import com.yangc.bridge.service.FileService;
import com.yangc.system.bean.TSysUser;
import com.yangc.system.service.UserService;
import com.yangc.utils.Message;
import com.yangc.utils.encryption.Md5Utils;

@Service
public class ServerHandler extends IoHandlerAdapter implements Runnable {

	private static final Logger logger = Logger.getLogger(ServerHandler.class);

	private static final ConcurrentMap<String, TBridgeChat> CHAT_CACHE = new ConcurrentHashMap<String, TBridgeChat>();
	private static final List<String> FILE_CACHE = new ArrayList<String>();
	private static final Map<String, Map<String, TBridgeFile>> OFFLINE_FILE_CACHE = new HashMap<String, Map<String, TBridgeFile>>();

	@Autowired
	private UserService userService;
	@Autowired
	private ChatService chatService;
	@Autowired
	private FileService fileService;

	public ServerHandler() {
		new Thread(this).start();
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		if (session.getRemoteAddress() != null) {
			InetAddress address = ((InetSocketAddress) session.getRemoteAddress()).getAddress();
			if (address != null) {
				logger.info("sessionClosed - " + address.getHostAddress());
			}
		}
		// 移除缓存
		String username = SessionCache.removeSessionId(session.getId());
		if (StringUtils.isNotBlank(username)) {
			OFFLINE_FILE_CACHE.remove(username);
		}
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		logger.info("sessionIdle");
		if (status.equals(IdleStatus.BOTH_IDLE)) {
			session.close(true);
		}
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		logger.error("exceptionCaught - " + cause.getMessage(), cause);
		session.close(true);
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		logger.info("messageReceived");
		if (message instanceof ProtocolHeart) {
			session.write(message);
		} else if (message instanceof ResultBean) {
			this.resultReceived(session, (ResultBean) message);
		} else if (message instanceof UserBean) {
			this.loginReceived(session, (UserBean) message);
		} else if (message instanceof TBridgeChat) {
			this.chatReceived(session, (TBridgeChat) message);
		} else if (message instanceof TBridgeFile) {
			this.fileReceived(session, (TBridgeFile) message);
		} else {
			session.close(true);
		}
	}

	/**
	 * @功能: 接收结果并转发
	 * @作者: yangc
	 * @创建日期: 2014年8月26日 上午10:39:03
	 * @param session
	 * @param result
	 * @throws Exception
	 */
	private void resultReceived(IoSession session, ResultBean result) throws Exception {
		// 如果未登录则断开连接
		if (!SessionCache.contains(result.getFrom())) {
			session.close(true);
			return;
		}

		// 清空已送达的文本
		TBridgeChat chat = CHAT_CACHE.remove(result.getUuid());
		if (chat != null) {
			chat.setStatus(1L);
			this.chatService.addOrUpdateChat(chat);
		}

		// 转发离线文件
		Map<String, TBridgeFile> offlineFileMap = OFFLINE_FILE_CACHE.get(result.getFrom());
		if (offlineFileMap != null && !offlineFileMap.isEmpty()) {
			TBridgeFile file = offlineFileMap.remove(result.getUuid());
			if (file != null) {
				MessageHandler.sendFile(session, file, result.isSuccess());
				this.fileService.delFile(file.getId());
				if (offlineFileMap.isEmpty()) {
					OFFLINE_FILE_CACHE.remove(result.getFrom());
				}
				return;
			}
		}

		Long sessionId = SessionCache.getSessionId(result.getTo());
		if (sessionId != null) {
			MessageHandler.sendResult(session.getService().getManagedSessions().get(sessionId), result);
		}
	}

	/**
	 * @功能: 验证登录, 并发送验证结果, 如果登录成功, 并且支持离线文本, 转发存在的离线文本, 转发存在的离线文件
	 * @作者: yangc
	 * @创建日期: 2014年8月26日 上午10:39:37
	 * @param session
	 * @param user
	 * @throws Exception
	 */
	private void loginReceived(IoSession session, UserBean user) throws Exception {
		List<TSysUser> users = this.userService.getUserListByUsernameAndPassword(user.getUsername(), Md5Utils.getMD5(user.getPassword()));

		ResultBean result = new ResultBean();
		result.setUuid(user.getUuid());
		result.setFrom(user.getUsername());
		result.setTo(user.getUsername());
		if (users == null || users.isEmpty()) {
			result.setSuccess(false);
			result.setData("用户名或密码错误");
		} else if (users.size() > 1) {
			result.setSuccess(false);
			result.setData("用户重复");
		} else {
			// 添加缓存
			SessionCache.putSessionId(user.getUsername(), session.getId());

			result.setSuccess(true);
			result.setData("登录成功");
		}
		MessageHandler.sendResult(session, result);

		// 登录失败, 标记登录次数, 超过登录阀值就踢出
		if (!result.isSuccess()) {
			Integer loginCount = (Integer) session.getAttribute("loginCount", 1);
			if (loginCount > 2) {
				session.close(false);
			} else {
				session.setAttribute("loginCount", ++loginCount);
			}
		}
		// 登录成功, 如果存在离线文本, 则发送
		else if (StringUtils.equals(Message.getMessage("bridge.offline_data"), "1")) {
			List<TBridgeChat> chatList = this.chatService.getUnreadChatListByTo(user.getUsername());
			if (chatList != null && !chatList.isEmpty()) {
				for (TBridgeChat chat : chatList) {
					chat.setMillisecond(System.currentTimeMillis());
					CHAT_CACHE.put(chat.getUuid(), chat);
					MessageHandler.sendChat(session, chat);
				}
			}

			List<TBridgeFile> fileList = this.fileService.getUnreceiveFileListByTo(user.getUsername());
			if (fileList != null && !fileList.isEmpty()) {
				Map<String, TBridgeFile> offlineFileMap = new HashMap<String, TBridgeFile>();
				for (TBridgeFile file : fileList) {
					offlineFileMap.put(file.getUuid(), file);
				}
				OFFLINE_FILE_CACHE.put(user.getUsername(), offlineFileMap);
				for (TBridgeFile file : fileList) {
					MessageHandler.sendReadyFile(session, file);
				}
			}
		}
	}

	/**
	 * @功能: 接收文本并转发
	 * @作者: yangc
	 * @创建日期: 2014年9月2日 下午6:32:45
	 * @param session
	 * @param chat
	 * @throws Exception
	 */
	private void chatReceived(IoSession session, TBridgeChat chat) throws Exception {
		// 如果未登录则断开连接
		if (!SessionCache.contains(chat.getFrom())) {
			session.close(true);
			return;
		}

		Long sessionId = SessionCache.getSessionId(chat.getTo());
		if (sessionId != null) {
			chat.setMillisecond(System.currentTimeMillis());
			CHAT_CACHE.put(chat.getUuid(), chat);
			MessageHandler.sendChat(session.getService().getManagedSessions().get(sessionId), chat);
		} else if (StringUtils.equals(Message.getMessage("bridge.offline_data"), "1")) {
			chat.setStatus(0L);
			this.chatService.addOrUpdateChat(chat);
		}
	}

	/**
	 * @功能: 文件确认, 文件传输转发
	 * @作者: yangc
	 * @创建日期: 2014年9月2日 下午6:33:23
	 * @param session
	 * @param file
	 * @throws Exception
	 */
	private void fileReceived(IoSession session, TBridgeFile file) throws Exception {
		// 如果未登录则断开连接
		if (!SessionCache.contains(file.getFrom())) {
			session.close(true);
			return;
		}

		byte contentType = file.getContentType();
		// 发送在线文件
		if (contentType == ContentType.READY_FILE || file.getTransmitStatus() == TransmitStatus.ONLINE) {
			Long sessionId = SessionCache.getSessionId(file.getTo());
			if (sessionId != null) {
				if (contentType == ContentType.READY_FILE) {
					MessageHandler.sendReadyFile(session.getService().getManagedSessions().get(sessionId), file);
				} else if (contentType == ContentType.TRANSMIT_FILE) {
					MessageHandler.sendTransmitFile(session.getService().getManagedSessions().get(sessionId), file);
				}
			}
		}
		// 发送离线文件
		else {
			File offlineFile = null;
			if (!FILE_CACHE.contains(file.getUuid())) {
				FILE_CACHE.add(file.getUuid());
				File dir = new File(FileUtils.getTempDirectoryPath() + "/com.yangc.bridge/" + file.getTo());
				if (!dir.exists() || !dir.isDirectory()) {
					dir.delete();
					dir.mkdirs();
				}
				offlineFile = new File(dir, file.getUuid());
				offlineFile.delete();
				offlineFile.createNewFile();
			} else {
				offlineFile = new File(FileUtils.getTempDirectoryPath() + "/com.yangc.bridge/" + file.getTo() + "/" + file.getUuid());
			}
			RandomAccessFile raf = new RandomAccessFile(offlineFile, "rw");
			raf.seek(raf.length());
			raf.write(file.getData(), 0, file.getOffset());
			raf.close();

			if (offlineFile.length() == file.getFileSize() && Md5Utils.getMD5String(offlineFile).equals(file.getFileMd5())) {
				FILE_CACHE.remove(file.getUuid());
				this.fileService.addFile(file);

				ResultBean result = new ResultBean();
				result.setUuid(file.getUuid());
				result.setFrom(file.getFrom());
				result.setTo(file.getFrom());
				result.setSuccess(true);
				result.setData("ok");
				MessageHandler.sendResult(session, result);
			}
		}
	}

	/**
	 * @功能: 超时无响应, 标记为未读
	 * @作者: yangc
	 * @创建日期: 2014年9月2日 下午6:33:23
	 */
	@Override
	public void run() {
		try {
			while (true) {
				long currentMillisecond = System.currentTimeMillis();
				for (Entry<String, TBridgeChat> entry : CHAT_CACHE.entrySet()) {
					TBridgeChat chat = entry.getValue();
					if (currentMillisecond - chat.getMillisecond() > 8000) {
						logger.info("unread - uuid=" + chat.getUuid() + ", from=" + chat.getFrom() + ", to=" + chat.getTo());
						chat.setStatus(0L);
						this.chatService.addOrUpdateChat(chat);
						CHAT_CACHE.remove(entry.getKey());
					}
				}
				Thread.sleep(5000);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 出现异常重启线程
			new Thread(this).start();
		}
	}

}
