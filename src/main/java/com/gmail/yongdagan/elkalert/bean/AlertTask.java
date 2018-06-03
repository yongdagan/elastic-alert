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
	 * 任务ID
	 */
	private String taskId;

	/**
	 * 任务名称
	 */
	private String taskName;
	
	/**
	 * 索引类型
	 */
	private String esIndex;
	
	/**
	 * 文档类型
	 */
	private String docType;

	/**
	 * 查询过滤器
	 */
	private String queryFilter;

	/**
	 * 告警条件
	 */
	private String alertCondition;
	
	/**
	 * 告警内容
	 */
	private String alertMessage;
	
	/**
	 * 告警动作
	 */
	private String alertAction;

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getEsIndex() {
		return esIndex;
	}

	public void setEsIndex(String esIndex) {
		this.esIndex = esIndex;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public String getQueryFilter() {
		return queryFilter;
	}

	public void setQueryFilter(String queryFilter) {
		this.queryFilter = queryFilter;
	}

	public String getAlertCondition() {
		return alertCondition;
	}

	public void setAlertCondition(String alertCondition) {
		this.alertCondition = alertCondition;
	}

	public String getAlertMessage() {
		return alertMessage;
	}

	public void setAlertMessage(String alertMessage) {
		this.alertMessage = alertMessage;
	}

	public String getAlertAction() {
		return alertAction;
	}

	public void setAlertAction(String alertAction) {
		this.alertAction = alertAction;
	}
	
}
