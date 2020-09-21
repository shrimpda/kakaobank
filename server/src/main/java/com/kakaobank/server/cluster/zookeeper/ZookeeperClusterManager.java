package com.kakaobank.server.cluster.zookeeper;

import com.kakaobank.server.node.NodeInfo;
import com.kakaobank.server.transport.TransportConnectionListener;
import com.kakaobank.server.transport.TransportService;
import com.kakaobank.server.transport.netty.NettyChannel;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperClusterManager {

	private final Logger logger = LoggerFactory.getLogger(ZookeeperClusterManager.class);

	private final ZookeeperClient client;
	private final TransportService transportService;
	private final String parentPath;
	private final Thread workerThread;

	private final BlockingQueue<Task> queue = new LinkedBlockingQueue<>(1);
	private final GetAndRegisterTask getAndRegisterTask = new GetAndRegisterTask();
	private final AtomicBoolean retryMode = new AtomicBoolean(false);

	public ZookeeperClusterManager(ZookeeperClient client, TransportService transportService) {
		this.client = client;
		this.transportService = transportService;
		this.parentPath = "/server/";
		workerThread = new Thread(new Worker());
	}

	public void start() {
		this.workerThread.start();
	}

	public void handleAndRegisterWatcher(String path) {
		if (parentPath.equals(path) || parentPath.equals(path + "/")) {
			final boolean offerSuccess = queue.offer(getAndRegisterTask);
			if (!offerSuccess) {
				logger.info("Message Queue is Full.");
			}
		} else {
			logger.info("Invalid Path {}.", path);
		}
	}

	private class Worker implements Runnable {

		@Override
		public void run() {
			while (true) {
				Task task = null;

				try {
					task = queue.poll(60000, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					logger.debug(e.getMessage(), e);
				}

				if (task == null) {
					if (retryMode.get()) {
						boolean success = getAndRegisterTask.handleAndRegisterWatcher0();
						if (success) {
							retryMode.compareAndSet(true, false);
						}
					}
				} else if (task instanceof GetAndRegisterTask) {
					boolean success = ((GetAndRegisterTask) task).handleAndRegisterWatcher0();
					if (!success) {
						retryMode.compareAndSet(false, true);
					}
				}
			}
		}
	}

	interface Task {

	}

	class GetAndRegisterTask implements Task {

		private boolean handleAndRegisterWatcher0() {
			boolean needNotRetry = false;
			try {
				client.createPath(parentPath);

				List<InetSocketAddress> targetList = getTargetAddressList(parentPath);
				List<InetSocketAddress> connectedList = transportService.getConnectedList();

				for (InetSocketAddress address : targetList) {
					if (!connectedList.contains(address)) {
						transportService.connectToNode(address);
					}
				}

				for (InetSocketAddress address : connectedList) {
					if (!targetList.contains(address)) {
						transportService.disConnectToNode(address);
					}
				}

				needNotRetry = true;
				return needNotRetry;
			} catch (Exception e) {
				if (!(e instanceof ConnectionLossException)) {
					needNotRetry = true;
				}
			}

			return needNotRetry;
		}

		private List<InetSocketAddress> getTargetAddressList(String parentPath) throws Exception {
			List<InetSocketAddress> result = new ArrayList<>();

			List<String> childNodeList = client.getChildNodeList(parentPath, true);

			try {
				for (String childNodeName : childNodeList) {
					String fullPath = ZKPaths.makePath(parentPath, childNodeName);
					byte[] data = client.getData(fullPath);
					String nodeContents = new String(data);

					String serverInfo = fullPath.substring(fullPath.lastIndexOf("/") + 1);
					String[] infos = serverInfo.split(":");

					logger.info("find server [{}], state [{}]", serverInfo, nodeContents);

					result.add(InetSocketAddress.createUnresolved(infos[0], Integer.parseInt(infos[1])));
				}
				return result;
			} catch (Exception e) {
				logger.warn("Failed to process getting detail address. message:{}", e.getMessage(), e);
			}

			return Collections.emptyList();
		}

		private List<String> createHostList(String[] hostAddresses, String representativeHostAddress) {
			List<String> hostAddressList = new ArrayList<>(hostAddresses.length);

			hostAddressList.add(representativeHostAddress);
			for (String hostAddress : hostAddresses) {
				if (hostAddressList.contains(hostAddress)) {
					continue;
				}
				hostAddressList.add(hostAddress);
			}

			return hostAddressList;
		}

	}
}
