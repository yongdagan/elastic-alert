package com.gmail.yongdagan.elkalert.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.gmail.yongdagan.elkalert.config.CuratorConfig;
import com.gmail.yongdagan.elkalert.curator.TaskLeader;

@RestController
public class TestController {
	
	private static Logger logger = LoggerFactory.getLogger(TestController.class);
	
	@Autowired
	private CuratorConfig curatorConfig;
	
	private List<TaskLeader> leaderList = new ArrayList<>();

	@RequestMapping(value = "/iptest", method=RequestMethod.GET)
	public String iptest() {
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			logger.info("get server ip：{}", ip);
			return ip;
		} catch (UnknownHostException e) {
			logger.error("get server ip error", e);
			return "error exists";
		}
	}
	
	@RequestMapping(value = "/addTaskLeader", method=RequestMethod.GET)
	public void addTaskLeader(String clientId) {
		try {
			CuratorFramework client = CuratorFrameworkFactory.builder()
					.connectString(curatorConfig.getConnectString())
					.retryPolicy(new ExponentialBackoffRetry(
							curatorConfig.getRetryBaseSleepTime(), curatorConfig.getRetryTimes()))
					.build();
			TaskLeader taskLeader = new TaskLeader(clientId, client);
			taskLeader.bootstrap();
			taskLeader.runForMaster();
			leaderList.add(taskLeader);
		} catch (Exception e) {
			logger.error("taskLeader", e);
		}
	}
	
	@RequestMapping(value = "/isLeader", method=RequestMethod.GET)
	public void isLeader(String clientId) {
		try {
			if(!leaderList.isEmpty()) {
				for(TaskLeader leader : leaderList) {
					if(leader.getClientId().equals(clientId)) {
						logger.info("clientId={}, hasLeaderShip?{}", leader.getClientId(), leader.hasLeaderShip());
						return;
					}
				}
			}
			logger.info("invalid clientId={}", clientId);
		} catch (Exception e) {
			logger.error("taskLeader", e);
		}
	}
	
	@RequestMapping(value = "/showTaskLeaderList", method=RequestMethod.GET)
	public void showTaskLeaderList() {
		try {
			if(!leaderList.isEmpty()) {
				for(TaskLeader leader : leaderList) {
					logger.info("clientId={}, hasLeaderShip?{}", leader.getClientId(), leader.hasLeaderShip());
				}
			}
		} catch (Exception e) {
			logger.error("taskLeader", e);
		}
	}
	
	@RequestMapping(value = "/closeLeader", method=RequestMethod.GET)
	public void closeLeader(String clientId) {
		try {
			if(!leaderList.isEmpty()) {
				for(int i = 0; i < leaderList.size(); i ++) {
					if(leaderList.get(i).getClientId().equals(clientId)) {
						leaderList.get(i).close();
						leaderList.remove(i);
						return;
					}
				}
			}
			logger.info("invalid clientId={}", clientId);
		} catch (Exception e) {
			logger.error("taskLeader", e);
		}
	}

}
