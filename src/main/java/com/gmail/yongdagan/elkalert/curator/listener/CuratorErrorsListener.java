package com.gmail.yongdagan.elkalert.curator.listener;

import org.apache.curator.framework.api.UnhandledErrorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gmail.yongdagan.elkalert.curator.TaskLeader;

/**
 * curator错误处理
 * @author yongdagan@gmail.com
 * @date 2018年5月26日 下午10:09:41
 *
 */
public class CuratorErrorsListener implements UnhandledErrorListener {
	
	private static Logger logger = LoggerFactory.getLogger(TaskLeader.class);

	@Override
	public void unhandledError(String message, Throwable e) {
		logger.error("Unrecoverable error: " + message, e);
//		try {
//			close();
//		} catch (IOException ioe) {
//			logger.warn("Exception when closing.", ioe);
//		}
	}

}
