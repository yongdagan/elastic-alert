//package com.gmail.yongdagan.elkalert.curator.listener;
//
//import java.io.IOException;
//
//import org.apache.curator.framework.CuratorFramework;
//import org.apache.curator.framework.api.CuratorEvent;
//import org.apache.curator.framework.api.CuratorListener;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.gmail.yongdagan.elkalert.constant.CuratorConstant;
//import com.gmail.yongdagan.elkalert.curator.TaskLeader;
//
///**
// * 任务协调监听
// * @author yongdagan@gmail.com
// * @date 2018年5月26日 下午10:08:04
// *
// */
//public class TaskCoordinatorListener implements CuratorListener {
//	
//	private static Logger logger = LoggerFactory.getLogger(TaskLeader.class);
//
//	@Override
//	public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
//        try{
//        	logger.info("Event path: " + event.getPath());
//            switch (event.getType()) { 
//            case CHILDREN:
//                if(event.getPath().contains(CuratorConstant.ZK_ASSIGN_NODE)) {
//                    for(String task : event.getChildren()){
//                        String path = event.getPath() + "/" + task;
//                        logger.info( "Deleting assignment: {}", path);
//                        client.delete().inBackground().forPath(path);
//                    }
//                    
//                    logger.info( "Deleting assignment: {}", event.getPath());
//                    client.delete().inBackground().forPath(event.getPath());
//                    for(String task : event.getChildren()) {
//                		assignTask(task, client.getData().forPath(CuratorConstant.ZK_TASKS_NODE + "/" + task));
//                	}
//                } else {
//                	logger.warn("Unexpected event: " + event.getPath());
//                }
//                break;
//            case CREATE:
//                if(event.getPath().contains(CuratorConstant.ZK_ASSIGN_NODE)) {
//                	logger.info("Task assigned correctly: " + event.getName());
//                    
//                    String number = event.getPath().substring(event.getPath().lastIndexOf('-') + 1);
//                    logger.info("Deleting task: {}", number);
//                    client.delete().inBackground().forPath(CuratorConstant.ZK_TASKS_NODE + "/task-" + number);
//                    recoveryLatch.countDown();
//                }
//                break;
//            case DELETE:
//                if(event.getPath().contains(CuratorConstant.ZK_TASKS_NODE)) {
//                	logger.info("Result of delete operation: " + event.getResultCode() + ", " + event.getPath());
//                } else if(event.getPath().contains(CuratorConstant.ZK_ASSIGN_NODE)) {
//                	logger.info("Task correctly deleted: " + event.getPath());
//                }
//                break;
//            case WATCHED:
//                break;
//            default:
//            	logger.error("Default case: " + event.getType());
//            }
//        } catch (Exception e) {
//        	logger.error("Exception while processing event.", e);
//            try{
//                close();
//            } catch (IOException ioe) {
//            	logger.error("IOException while closing.", ioe);
//            }
//        }
//	}
//	
//
//}
