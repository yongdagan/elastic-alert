package com.gmail.yongdagan.elkalert.curator;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gmail.yongdagan.elkalert.util.CuratorUtil;

public class TaskCoordinator implements Closeable, LeaderLatchListener {
	
	private static Logger logger = LoggerFactory.getLogger(TaskCoordinator.class);
	
	private String clientId;
	private CuratorFramework client;
    private final LeaderLatch leaderLatch;
    private final PathChildrenCache handlersCache;
    private final PathChildrenCache tasksCache;
    private final PathChildrenCache personalTasksCache;
    private CountDownLatch recoveryLatch = new CountDownLatch(0);
    private Random rand = new Random(System.currentTimeMillis());
    
    public TaskCoordinator(String clientId, CuratorFramework client) {
    	this.clientId = clientId;
    	this.client = client;
    	this.leaderLatch = new LeaderLatch(this.client, CuratorUtil.leaderPath(), clientId);
        this.handlersCache = new PathChildrenCache(this.client, CuratorUtil.hanldersPath(), true);
        this.tasksCache = new PathChildrenCache(this.client, CuratorUtil.tasksPath(), true);
        this.personalTasksCache = new PathChildrenCache(this.client, CuratorUtil.handlerTasksPath(clientId), true);
	}
    
    /**
     * bootstrap curator and register
     * @throws Exception
     */
    public void bootstrap() throws Exception{
    	// start curator client
    	client.start();
    	// bootstrap path
    	this.tryToCreatePath(CuratorUtil.hanldersPath(), CreateMode.PERSISTENT);
    	this.tryToCreatePath(CuratorUtil.assignPath(), CreateMode.PERSISTENT);
    	this.tryToCreatePath(CuratorUtil.tasksPath(), CreateMode.PERSISTENT);
    	// register
    	client.create().withMode(CreateMode.EPHEMERAL).forPath(CuratorUtil.handlerNode(clientId));
    	client.create().withMode(CreateMode.PERSISTENT).forPath(CuratorUtil.handlerTasksPath(clientId));
    }
    
    private void tryToCreatePath(String path, CreateMode createMode) {
    	try {
    		client.create().creatingParentsIfNeeded().withMode(createMode).forPath(path);
		} catch (NodeExistsException e) {
			logger.info("node already exists, path=" + path);
		} catch (Exception e) {
			logger.info("fail to create path, path=" + path, e);
		}
    }
    
    /**
     * try to be leader
     * @throws Exception
     */
    public void runForLeader() throws Exception {
    	// register listeners
        client.getCuratorListenable().addListener(leaderListener);
        client.getUnhandledErrorListenable().addListener(errorsListener);
        
        logger.info("start master selection: " + clientId);
        leaderLatch.addListener(this);
        leaderLatch.start();
    }
    
    CuratorListener leaderListener = new CuratorListener() {
        public void eventReceived(CuratorFramework client, CuratorEvent event){
            try{
            	logger.info("Event path: " + event.getPath());
                switch (event.getType()) { 
                case CHILDREN:
                    if(event.getPath().contains(CuratorUtil.assignPath())) {
                    	// 分配任务给handlers
                        for(String task : event.getChildren()){
                            String path = event.getPath() + "/" + task;
                            logger.info( "Deleting assignment: {}", path);
                            client.delete().inBackground().forPath(path);
                        }
                        
                        logger.info( "Deleting assignment: {}", event.getPath());
                        client.delete().inBackground().forPath(event.getPath());
                        for(String task : event.getChildren()) {
                    		assignTask(task, client.getData().forPath(CuratorUtil.tasksPath() + "/" + task));
                    	}
                    } else {
                    	logger.warn("Unexpected event: " + event.getPath());
                    }
                    break;
                case CREATE:
                    if(event.getPath().contains(CuratorUtil.assignPath())) {
                    	// 分配任务成功，删除任务节点 TODO 不删除，只改变任务状态为已分配
                    	logger.info("Task assigned correctly: " + event.getName());
                        
                        String number = event.getPath().substring(event.getPath().lastIndexOf('-') + 1);
                        logger.info("Deleting task: {}", number);
                        client.delete().inBackground().forPath(CuratorUtil.tasksPath() + "/task-" + number);
                        recoveryLatch.countDown();
                    }
                    break;
                case DELETE:
                	// TODO 目前来看，除了删除已失效handler任务外，没有其他删除操作
                    if(event.getPath().contains(CuratorUtil.tasksPath())) {
                    	logger.info("Result of delete operation: " + event.getResultCode() + ", " + event.getPath());
                    } else if(event.getPath().contains(CuratorUtil.assignPath())) {
                    	logger.info("Task correctly deleted: " + event.getPath());
                    }
                    break;
                case WATCHED:
                    break;
                default:
                	logger.error("Default case: " + event.getType());
                }
            } catch (Exception e) {
            	logger.error("Exception while processing event.", e);
                try{
                    close();
                } catch (IOException ioe) {
                	logger.error("IOException while closing.", ioe);
                }
            }
        };
    };
    
	private void assignTask(String task, byte[] data) throws Exception {
		List<ChildData> workersList = handlersCache.getCurrentData();

		logger.info("Assigning task {}, data {}", task, new String(data));

		String designatedWorker = workersList.get(rand.nextInt(workersList.size()))
				.getPath().replaceFirst(CuratorUtil.hanldersPath() + "/", "");

		String path = CuratorUtil.assignPath() + "/" + designatedWorker + "/" + task;
		client.create().withMode(CreateMode.PERSISTENT).inBackground().forPath(path, data);
	}
	
	UnhandledErrorListener errorsListener = new UnhandledErrorListener() {
		public void unhandledError(String message, Throwable e) {
			logger.error("Unrecoverable error: " + message, e);
			try {
				close();
			} catch (IOException ioe) {
				logger.warn("Exception when closing.", ioe);
			}
		}
	};

	@Override
	public void isLeader() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notLeader() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws IOException {
		logger.info("Closing");
		leaderLatch.close();
		client.close();
	}

}
