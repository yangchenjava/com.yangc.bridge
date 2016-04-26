package com.yangc.bridge.comm.handler.processor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import com.yangc.bridge.bean.ResultBean;
import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.bean.TBridgeCommon;
import com.yangc.bridge.bean.TBridgeFile;
import com.yangc.bridge.bean.UserBean;
import com.yangc.bridge.comm.Server;
import com.yangc.bridge.comm.cache.SessionCache;
import com.yangc.bridge.comm.handler.SendHandler;
import com.yangc.bridge.comm.handler.ServerHandler;
import com.yangc.bridge.service.CommonService;
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
	private CommonService commonService;
	@Autowired
	private JmsTemplate jmsTemplate;

	private ExecutorService executorService;

	public LoginProcessor() {
		this.executorService = Executors.newSingleThreadExecutor();
	}

	/**
	 * @功能: 处理登录逻辑
	 * @作者: yangc
	 * @创建日期: 2015年1月7日 下午5:31:08
	 * @param session
	 * @param user
	 */
	public void process(IoSession session, UserBean user) {
		this.executorService.execute(new Task(session, user));
	}

	private class Task implements Runnable {
		private IoSession session;
		private UserBean user;

		private Task(IoSession session, UserBean user) {
			this.session = session;
			this.user = user;
		}

		@Override
		public void run() {
			try {
				String username = this.user.getUsername();
				List<TSysUser> users = userService.getUserListByUsernameAndPassword(username, Md5Utils.getMD5(this.user.getPassword()));

				ResultBean result = new ResultBean();
				result.setUuid(this.user.getUuid());
				if (CollectionUtils.isEmpty(users)) {
					result.setSuccess(false);
					result.setData("用户名或密码错误");
				} else if (users.size() > 1) {
					result.setSuccess(false);
					result.setData("用户重复");
				} else {
					Long expireSessionId = sessionCache.getSessionId(username);
					if (expireSessionId != null) {
						// IoSession expireSession = this.session.getService().getManagedSessions().get(expireSessionId);
						// if (expireSession != null) expireSession.close(true);
						IoSession expireSession = this.session.getService().getManagedSessions().get(expireSessionId);
						if (expireSession != null && expireSession.getAttribute(ServerHandler.USER) != null
								&& StringUtils.equals(((UserBean) expireSession.getAttribute(ServerHandler.USER)).getUsername(), username)) {
							// 标识断线重连的session
							((UserBean) expireSession.getAttribute(ServerHandler.USER)).setExpireSessionId(expireSessionId);
							// expireSession.close(true);
							expireSession.closeNow();
						} else {
							this.user.setExpireSessionId(expireSessionId);
							jmsTemplate.send(new MessageCreator() {
								@Override
								public javax.jms.Message createMessage(Session session) throws JMSException {
									ObjectMessage message = session.createObjectMessage();
									message.setStringProperty("IP", Server.IP);
									message.setObject(user);
									return message;
								}
							});
						}
					}
					this.session.setAttribute(ServerHandler.USER, this.user);
					// 添加缓存
					sessionCache.putSessionId(username, this.session.getId());

					result.setSuccess(true);
					result.setData("登录成功");
				}
				SendHandler.sendResult(this.session, result);

				// 登录失败, 标记登录次数, 超过登录阀值就踢出
				if (!result.isSuccess()) {
					Integer loginCount = (Integer) this.session.getAttribute(ServerHandler.LOGIN_COUNT, 1);
					if (loginCount > 2) {
						// this.session.close(false);
						this.session.closeOnFlush();
					} else {
						this.session.setAttribute(ServerHandler.LOGIN_COUNT, ++loginCount);
					}
				}
				// 登录成功, 如果存在未读消息, 则发送
				else if (StringUtils.equals(Message.getMessage("bridge.offline_data"), "1")) {
					List<TBridgeCommon> commons = commonService.getUnreadCommonListByTo(username);
					if (CollectionUtils.isNotEmpty(commons)) {
						for (TBridgeCommon common : commons) {
							if (common instanceof TBridgeChat) {
								SendHandler.sendChat(this.session, (TBridgeChat) common);
							} else if (common instanceof TBridgeFile) {
								SendHandler.sendFile(this.session, (TBridgeFile) common);
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
