package com.kakaobank.server.cluster.zookeeper;

import com.kakaobank.server.configs.Configs;
import com.kakaobank.server.node.NodeInfo;
import com.kakaobank.server.transport.TransportConnectionListener;
import com.kakaobank.server.transport.TransportService;
import com.kakaobank.server.transport.netty.NettyChannel;
import com.kakaobank.server.transport.netty.NettyTransport;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperConnectionManager {

	private static final int DEFAULT_CONNECTION_TIMEOUT = 3000;

	private static final Logger logger = LoggerFactory.getLogger(ZookeeperConnectionManager.class);

	private final Configs configs;
	private final CuratorFramework curatorFramework;
	private final ZookeeperConnectionStateListener connectionStateListener;
	private final LeaderSelectorListener leaderSelectorListener;
	private final AtomicBoolean isLeader = new AtomicBoolean(false);

	public ZookeeperConnectionManager(Configs configs, TransportService transportService, String hostPort, int sessionTimeout, ZookeeperEventWatcher zookeeperEventWatcher) {
		this.configs = configs;
		CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
		builder.connectString(hostPort);
		builder.retryPolicy(new RetryPolicy() {
			@Override
			public boolean allowRetry(int retryCount, long elapsedTimeMs, RetrySleeper sleeper) {
				return false;
			}
		});

		if (sessionTimeout < DEFAULT_CONNECTION_TIMEOUT) {
			builder.connectionTimeoutMs(sessionTimeout);
		} else {
			builder.connectionTimeoutMs(DEFAULT_CONNECTION_TIMEOUT);
		}

		builder.sessionTimeoutMs(sessionTimeout);

		this.curatorFramework = builder.build();
//		this.leaderSelectorListener = new LeaderSelectorListener(curatorFramework, "/elect");
		this.leaderSelectorListener = null;

		LeaderLatch leaderLatch = new LeaderLatch(curatorFramework, "/elect", "test", LeaderLatch.CloseMode.NOTIFY_LEADER);
		try {
			leaderLatch.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		leaderLatch.addListener(new LeaderLatchListener() {
			@Override
			public void isLeader() {
				logger.info("Elect as a leader");
				isLeader.compareAndSet(false, true);
			}

			@Override
			public void notLeader() {
				logger.info("Not a leader anymore");
				isLeader.compareAndSet(true, false);
			}
		});

		this.connectionStateListener = new ZookeeperConnectionStateListener(zookeeperEventWatcher);
		curatorFramework.getConnectionStateListenable().addListener(connectionStateListener);

		transportService.addConnectionListener(new TransportConnectionListener() {
			@Override
			public void onNodeDisconnected(NodeInfo nodeInfo, NettyChannel channel) {
				if (isLeader.get()) {
					String path = "/server/" + nodeInfo.getAddress().getHostString()
						+ ":" + nodeInfo.getAddress().getPort();
					try {
						curatorFramework.setData().forPath(path, "stopped".getBytes());
					} catch (Exception e) {
						logger.info(e.getMessage(), e);
					}
				}
			}

			@Override
			public void onNodeConnected(NodeInfo nodeInfo, NettyChannel channel) {
				if (isLeader.get()) {
					String path = "/server/" + nodeInfo.getAddress().getHostString()
						+ ":" + nodeInfo.getAddress().getPort();
					try {
						curatorFramework.setData().forPath(path, "started".getBytes());
					} catch (Exception e) {
						logger.info(e.getMessage(), e);
					}
				}
			}
		});
	}

	public void start() {
		try {
			curatorFramework.start();
//			leaderSelectorListener.start();
			boolean connected = curatorFramework.blockUntilConnected(3000, TimeUnit.MILLISECONDS);
			if (!connected) {
				logger.info("failed while to connect(). it will be retried automatically");
			}

			CuratorCache cache = null;
			try {
				if(!curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut()){
					CloseableUtils.closeQuietly(curatorFramework);
					logger.info("[Listener] Connection failed");

					return;
				}

				String host = NettyTransport.HOST.get(configs);
				int port = NettyTransport.PORT.get(configs);
				String serverInfo = host + ":" + port;

//				curatorFramework.delete().deletingChildrenIfNeeded().forPath("/server");

				if (curatorFramework.checkExists().forPath("/server") == null) {
					curatorFramework.create().creatingParentsIfNeeded().forPath("/server", serverInfo.getBytes());
				}

				String serverPath = "/server/" + serverInfo;

				if (curatorFramework.checkExists().forPath(serverPath) == null) {
					curatorFramework.create().creatingParentsIfNeeded().forPath(serverPath, "start".getBytes());
				}

				cache = CuratorCache.build(curatorFramework, "/server");
				cache.start();
				cache.listenable().addListener(getCacheListener(cache));
			} catch (Exception e) {

			}
		} catch (Exception e) {
			if (curatorFramework != null) {
				curatorFramework.close();
			}

			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private CuratorCacheListener getCacheListener(CuratorCache cache) {
		CuratorCacheListener listener = new CuratorCacheListener() {
			@Override
			public void event(Type type, ChildData oldData, ChildData data) {
				switch (type) {
					case NODE_CREATED: {
						logger.info("[Node Create Event] path [{}], data [{}]", data.getPath(), new String(data.getData()));
						break;
					}
					case NODE_CHANGED: {
						logger.info("[Node Changed Event] old_path [{}], old_data [{}], new_path [{}], new_data [{}]",
							oldData.getPath(), new String(oldData.getData()), data.getPath(), new String(data.getData()));
						getCuratorInfo();
						break;
					}
					case NODE_DELETED: {
						logger.info("[Node Deleted Event]");

						break;
					}
					default:
						logger.info("[{}]",type.name());

				}
			}
		};
		return listener;
	}

	private void getCuratorInfo() {
		List<String> key;
		try {
			if(curatorFramework !=null){
				key = curatorFramework.getChildren().watched().forPath("/server");
				for (String string : key) {
					String val = new String(curatorFramework.getData().forPath("/server/" + string));
					logger.info("[Watch][{}][{}]", string, val);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public boolean isConnected() {
		return curatorFramework.getZookeeperClient().isConnected();
	}

	public CuratorFramework getClient() {
		return curatorFramework;
	}
}
