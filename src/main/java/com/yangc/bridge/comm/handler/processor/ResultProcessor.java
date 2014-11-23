package com.yangc.bridge.comm.handler.processor;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yangc.bridge.bean.ResultBean;
import com.yangc.bridge.bean.UserBean;
import com.yangc.bridge.comm.handler.ServerHandler;
import com.yangc.bridge.service.ChatService;

@Service
public class ResultProcessor {

	@Autowired
	private ChatService chatService;

	private ThreadPoolExecutor threadPool;

	public ResultProcessor() {
		// 初始化线程池
		this.threadPool = new ThreadPoolExecutor(5, 10, 1, TimeUnit.HOURS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.DiscardOldestPolicy());
	}

	public void process(IoSession session, ResultBean result) {
		String username = ((UserBean) session.getAttribute(ServerHandler.USER)).getUsername();
		this.threadPool.execute(new Task(result, username));
	}

	class Task implements Runnable {
		private ResultBean result;
		private String username;

		private Task(ResultBean result, String username) {
			this.result = result;
			this.username = username;
		}

		@Override
		public void run() {
			try {
				if (this.result.isSuccess()) {
					String uuid = this.result.getUuid();
					chatService.updateChatStatusByUuid(uuid);
					File file = new File(FileUtils.getTempDirectoryPath() + "/com.yangc.bridge/" + this.username + "/" + uuid);
					if (file.exists()) {
						file.delete();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
