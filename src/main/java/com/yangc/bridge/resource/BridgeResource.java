package com.yangc.bridge.resource;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yangc.bean.ResultBean;
import com.yangc.bridge.bean.ClientStatus;
import com.yangc.bridge.bean.ServerStatus;
import com.yangc.bridge.service.BridgeService;
import com.yangc.exception.WebApplicationException;
import com.yangc.system.bean.Permission;

@Controller
@RequestMapping("/bridge")
public class BridgeResource {

	private static final Logger logger = Logger.getLogger(BridgeResource.class);

	@Autowired
	private BridgeService bridgeService;

	@RequestMapping(value = "restartServer", method = RequestMethod.POST)
	@ResponseBody
	@RequiresPermissions("bridge:" + Permission.SEL)
	public ResultBean restartServer() {
		logger.info("restartServer");
		try {
			this.bridgeService.restartServer();
			return new ResultBean(true, "重启mina服务成功");
		} catch (Exception e) {
			e.printStackTrace();
			return WebApplicationException.build();
		}
	}

	@RequestMapping(value = "getServerStatus", method = RequestMethod.POST)
	@ResponseBody
	@RequiresPermissions("bridge:" + Permission.SEL)
	public ServerStatus getServerStatus() {
		logger.info("getServerStatus");
		return this.bridgeService.getServerStatus();
	}

	@RequestMapping(value = "getClientStatusList", method = RequestMethod.POST)
	@ResponseBody
	@RequiresPermissions("bridge:" + Permission.SEL)
	public List<ClientStatus> getClientStatusList() {
		logger.info("getClientStatusList");
		return this.bridgeService.getClientStatusList();
	}

}
