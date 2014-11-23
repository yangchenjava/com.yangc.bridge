package com.yangc.bridge.comm.handler.processor;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yangc.bridge.bean.ResultBean;
import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.bean.TBridgeFile;
import com.yangc.bridge.bean.TBridgeText;
import com.yangc.bridge.bean.UserBean;
import com.yangc.bridge.comm.cache.SessionCache;
import com.yangc.bridge.comm.handler.SendHandler;
import com.yangc.bridge.comm.handler.ServerHandler;
import com.yangc.bridge.service.ChatService;
import com.yangc.system.bean.TSysUser;
import com.yangc.system.service.UserService;
import com.yangc.utils.Message;
import com.yangc.utils.encryption.Md5Utils;

@Service
public class LoginProcessor {

	@Autowired
	private SessionCache sessionCache;
	@Autowired
	private UserService userService;
	@Autowired
	private ChatService chatService;

	private ThreadPoolExecutor threadPool;

	public LoginProcessor() {
		// 初始化线程池
		this.threadPool = new ThreadPoolExecutor(5, 10, 1, TimeUnit.HOURS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.DiscardOldestPolicy());
	}

	public void process(IoSession session, UserBean user) {
		// 处理登录逻辑
		this.threadPool.execute(new Task(session, user));
	}

	class Task implements Runnable {
		private IoSession session;
		private UserBean user;

		private Task(IoSession session, UserBean user) {
			this.session = session;
			this.user = user;
		}

		@Override
		public void run() {
			try {
				List<TSysUser> users = userService.getUserListByUsernameAndPassword(this.user.getUsername(), Md5Utils.getMD5(this.user.getPassword()));

				ResultBean result = new ResultBean();
				result.setUuid(this.user.getUuid());
				if (CollectionUtils.isEmpty(users)) {
					result.setSuccess(false);
					result.setData("用户名或密码错误");
				} else if (users.size() > 1) {
					result.setSuccess(false);
					result.setData("用户重复");
				} else {
					Long sessionId = sessionCache.getSessionId(this.user.getUsername());
					if (sessionId != null) {
						IoSession s = this.session.getService().getManagedSessions().get(sessionId);
						if (s != null) s.close(true);
					}
					this.session.setAttribute(ServerHandler.USER, user);
					// 添加缓存
					sessionCache.putSessionId(this.user.getUsername(), this.session.getId());

					result.setSuccess(true);
					result.setData("登录成功");
				}
				SendHandler.sendResult(this.session, result);

				// 登录失败, 标记登录次数, 超过登录阀值就踢出
				if (!result.isSuccess()) {
					Integer loginCount = (Integer) this.session.getAttribute(ServerHandler.LOGIN_COUNT, 1);
					if (loginCount > 2) {
						this.session.close(false);
					} else {
						this.session.setAttribute(ServerHandler.LOGIN_COUNT, ++loginCount);
					}
				}
				// 登录成功, 如果存在未读消息, 则发送
				else if (StringUtils.equals(Message.getMessage("bridge.offline_data"), "1")) {
					List<TBridgeChat> chats = chatService.getUnreadChatListByTo(this.user.getUsername());
					if (CollectionUtils.isNotEmpty(chats)) {
						for (TBridgeChat chat : chats) {
							if (chat instanceof TBridgeText) {
								SendHandler.sendChat(this.session, (TBridgeText) chat);
							} else if (chat instanceof TBridgeFile) {
								SendHandler.sendFile(this.session, (TBridgeFile) chat);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
