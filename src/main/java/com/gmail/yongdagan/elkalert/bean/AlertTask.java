package com.gmail.yongdagan.elkalert.bean;

import java.io.Serializable;

/**
 * 告警任务
 * @author yongdagan@gmail.com
 * @date 2018年5月26日 下午9:47:43
 *
 */
public class AlertTask implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1157756556694612459L;

	/**
	 * unique id
	 */
	private String taskName;
	
	/**
	 * TODO 占位，表明ZK需要保存告警任务的内容
	 */
	private String taskContent;

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskContent() {
		return taskContent;
	}

	public void setTaskContent(String taskContent) {
		this.taskContent = taskContent;
	}

}
