package com.gmail.yongdagan.elkalert.util;

import com.gmail.yongdagan.elkalert.constant.Constant;

public class CuratorUtil {
	
	private static final String LEADER_NODE = "leader";
	private static final String HANDLERS_NODE = "handlers";
	private static final String TASKS_NODE = "tasks";
	private static final String ASSIGN_NODE = "assign";
	
	public static String leaderPath() {
		return String.format("/%s/%s", Constant.APPLICATION_NAME, LEADER_NODE);
	}
	
	public static String hanldersPath() {
		return String.format("/%s/%s", Constant.APPLICATION_NAME, HANDLERS_NODE);
	}
	
	public static String tasksPath() {
		return String.format("/%s/%s", Constant.APPLICATION_NAME, TASKS_NODE);
	}
	
	public static String assignPath() {
		return String.format("/%s/%s", Constant.APPLICATION_NAME, ASSIGN_NODE);
	}
	
	public static String handlerTasksPath(String handler) {
		return String.format("/%s/%s/%s", Constant.APPLICATION_NAME, ASSIGN_NODE, handler);
	}
	
	public static String handlerNode(String handler) {
		return String.format("/%s/%s/%s", Constant.APPLICATION_NAME, HANDLERS_NODE, handler);
	}
	
	public static String handlerTaskNode(String handler, String task) {
		return String.format("/%s/%s/%s/%s", Constant.APPLICATION_NAME, ASSIGN_NODE, handler, task);
	}

}
