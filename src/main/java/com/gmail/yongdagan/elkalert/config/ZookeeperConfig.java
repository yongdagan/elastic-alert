package com.gmail.yongdagan.elkalert.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZookeeperConfig {
	
	@Value("${zookeeper.hostPorts}")
	private String hostPorts;
	
	@Value("${zookeeper.sessionTimeout:15000}")
	private String sessionTimeout;

	public String getHostPorts() {
		return hostPorts;
	}

	public void setHostPorts(String hostPorts) {
		this.hostPorts = hostPorts;
	}

	public int getSessionTimeout() {
		return Integer.parseInt(sessionTimeout);
	}

	public void setSessionTimeout(String sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}
}
