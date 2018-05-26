package com.gmail.yongdagan.elkalert.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "curator")
public class CuratorConfig {
	
	/**
	 * list of servers to connect to
	 */
	private String connectString;
	
	/**
	 * client id
	 */
	private String clientId;
	
	/**
	 * retry base sleep time
	 */
	private int retryBaseSleepTime;
	
	/**
	 * retry times
	 */
	private int retryTimes;
	
	@Bean
	public CuratorFramework curatorFramework() {
		CuratorFramework client = CuratorFrameworkFactory.builder()
				.connectString(connectString)
				.retryPolicy(new ExponentialBackoffRetry(retryBaseSleepTime, retryTimes))
				.build();
		return client;
	}

	public String getConnectString() {
		return connectString;
	}

	public void setConnectString(String connectString) {
		this.connectString = connectString;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public int getRetryBaseSleepTime() {
		return retryBaseSleepTime;
	}

	public void setRetryBaseSleepTime(int retryBaseSleepTime) {
		this.retryBaseSleepTime = retryBaseSleepTime;
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

}
