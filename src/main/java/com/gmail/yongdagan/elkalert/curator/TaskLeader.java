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
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gmail.yongdagan.elkalert.constant.CuratorConstant;
import com.gmail.yongdagan.elkalert.curator.RecoveredAssignments.RecoveryCallback;

public class TaskLeader implements Closeable, LeaderLatchListener {
	
	private static Logger logger = LoggerFactory.getLogger(TaskLeader.class);
	
	private String clientId;
	private CuratorFramework client;
    private final LeaderLatch leaderLatch;
    private final PathChildrenCache handlersCache;
    private final PathChildrenCache tasksCache;
    private CountDownLatch recoveryLatch = new CountDownLatch(0);
    private Random rand = new Random(System.currentTimeMillis());
    
    public TaskLeader(String clientId, CuratorFramework client) {
    	this.clientId = clientId;
    	this.client = client;
    	this.leaderLatch = new LeaderLatch(this.client, CuratorConstant.ZK_LEADER_NODE, clientId);
        this.handlersCache = new PathChildrenCache(this.client, CuratorConstant.ZK_HANDLERS_NODE, true);
        this.tasksCache = new PathChildrenCache(this.client, CuratorConstant.ZK_TASKS_NODE, true);
	}
    
	@Override
	public void isLeader() {
		try {
			handlersCache.getListenable().addListener(handlersCacheListener);
			handlersCache.start();

			RecoveredAssignments recoveredAssignments = new RecoveredAssignments(client.getZookeeperClient().getZooKeeper());
			recoveredAssignments.recover(new RecoveryCallback() {
				public void recoveryComplete(int rc, List<String> tasks) {
					try {
						if (rc == RecoveryCallback.FAILED) {
							logger.warn("Recovery of assigned tasks failed.");
						} else {
							logger.info("Assigning recovered tasks");
							recoveryLatch = new CountDownLatch(tasks.size());
							for(String task : tasks) {
					    		assignTask(task, client.getData().forPath(CuratorConstant.ZK_TASKS_NODE + "/" + task));
					    	}
						}

						new Thread(new Runnable() {
							public void run() {
								try {
									recoveryLatch.await();
									tasksCache.getListenable().addListener(tasksCacheListener);
									tasksCache.start();
								} catch (Exception e) {
									logger.warn("Exception while assigning and getting tasks.", e);
								}
							}
						}).start();
					} catch (Exception e) {
						logger.error("Exception while executing the recovery callback", e);
					}
				}
			});
		} catch (Exception e) {
			logger.error("Exception when starting leadership", e);
		}
	}

	@Override
	public void notLeader() {
		logger.info("Lost leadership");
	}

	@Override
	public void close() throws IOException {
		logger.info("Closing");
		leaderLatch.close();
//		client.close();
	}
	
	public boolean isConnected() {
		return client.getZookeeperClient().isConnected();
	}
	
	public boolean hasLeaderShip() {
		return leaderLatch.hasLeadership();
	}
	
	public String getClientId() {
		return clientId;
	}
	
    public void bootstrap() {
    	// start curator client
    	client.start();
    	// bootstrap path
    	this.tryToCreatePath(CuratorConstant.ZK_HANDLERS_NODE, CreateMode.PERSISTENT);
    	this.tryToCreatePath(CuratorConstant.ZK_ASSIGN_NODE, CreateMode.PERSISTENT);
    	this.tryToCreatePath(CuratorConstant.ZK_TASKS_NODE, CreateMode.PERSISTENT);
    }
    
    private void tryToCreatePath(String path, CreateMode createMode) {
    	try {
    		client.create().withMode(createMode).forPath(path, new byte[0]);
		} catch (NodeExistsException e) {
			logger.info("node already exists, path=" + path);
		} catch (Exception e) {
			logger.info("fail to create path, path=" + path, e);
		}
    }
    
    public void runForMaster() throws Exception {
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
                    if(event.getPath().contains(CuratorConstant.ZK_ASSIGN_NODE)) {
                        for(String task : event.getChildren()){
                            String path = event.getPath() + "/" + task;
                            logger.info( "Deleting assignment: {}", path);
                            client.delete().inBackground().forPath(path);
                        }
                        
                        logger.info( "Deleting assignment: {}", event.getPath());
                        client.delete().inBackground().forPath(event.getPath());
                        for(String task : event.getChildren()) {
                    		assignTask(task, client.getData().forPath(CuratorConstant.ZK_TASKS_NODE + "/" + task));
                    	}
                    } else {
                    	logger.warn("Unexpected event: " + event.getPath());
                    }
                    break;
                case CREATE:
                    if(event.getPath().contains(CuratorConstant.ZK_ASSIGN_NODE)) {
                    	logger.info("Task assigned correctly: " + event.getName());
                        
                        String number = event.getPath().substring(event.getPath().lastIndexOf('-') + 1);
                        logger.info("Deleting task: {}", number);
                        client.delete().inBackground().forPath(CuratorConstant.ZK_TASKS_NODE + "/task-" + number);
                        recoveryLatch.countDown();
                    }
                    break;
                case DELETE:
                    if(event.getPath().contains(CuratorConstant.ZK_TASKS_NODE)) {
                    	logger.info("Result of delete operation: " + event.getResultCode() + ", " + event.getPath());
                    } else if(event.getPath().contains(CuratorConstant.ZK_ASSIGN_NODE)) {
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
	
	PathChildrenCacheListener handlersCacheListener = new PathChildrenCacheListener() {
		public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) {
			if (event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
				try {
					String handler = event.getData().getPath().replaceFirst(CuratorConstant.ZK_HANDLERS_NODE + "/", "");
					client.getChildren().inBackground().forPath(CuratorConstant.ZK_ASSIGN_NODE + "/" + handler);
				} catch (Exception e) {
					logger.error("Exception while trying to re-assign tasks", e);
				}
			}
		}
	};
	
	PathChildrenCacheListener tasksCacheListener = new PathChildrenCacheListener() {
		public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) {
			if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
				try {
					assignTask(event.getData().getPath().replaceFirst(CuratorConstant.ZK_TASKS_NODE + "/", ""), event.getData().getData());
				} catch (Exception e) {
					logger.error("Exception when assigning task.", e);
				}
			}
		}
	};
	
	private void assignTask(String task, byte[] data) throws Exception {
		List<ChildData> workersList = handlersCache.getCurrentData();

		logger.info("Assigning task {}, data {}", task, new String(data));

		String designatedWorker = workersList.get(rand.nextInt(workersList.size()))
				.getPath().replaceFirst(CuratorConstant.ZK_HANDLERS_NODE + "/", "");

		String path = CuratorConstant.ZK_ASSIGN_NODE + "/" + designatedWorker + "/" + task;
		client.create().withMode(CreateMode.PERSISTENT).inBackground().forPath(path, data);
	}

}
