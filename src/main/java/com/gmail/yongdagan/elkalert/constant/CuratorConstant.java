package com.gmail.yongdagan.elkalert.constant;

public interface CuratorConstant {
	
	String ZK_LEADER_NODE = String.format("/%s/%s", Constant.APPLICATION_NAME, "leader");
	
	String ZK_HANDLERS_NODE = String.format("/%s/%s", Constant.APPLICATION_NAME, "handlers");
	
	String ZK_TASKS_NODE = String.format("/%s/%s", Constant.APPLICATION_NAME, "tasks");
	
	String ZK_ASSIGN_NODE = String.format("/%s/%s", Constant.APPLICATION_NAME, "assign");
	
	String ZK_STATUS_NODE = String.format("/%s/%s", Constant.APPLICATION_NAME, "status");

}
