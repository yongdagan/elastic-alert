package com.gmail.yongdagan.elkalert.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	
	private static Logger logger = LoggerFactory.getLogger(TestController.class);
	
	@RequestMapping(value = "/iptest", method=RequestMethod.GET)
	public String iptest() {
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			logger.info("请求获取本机IP：{}", ip);
			return ip;
		} catch (UnknownHostException e) {
			logger.error("请求获取本机IP失败", e);
			return "error exists";
		}
	}

}
