package com.kakaobank.server.cluster.zookeeper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class Executor
	implements Watcher, Runnable, DataMonitor.DataMonitorListener
{
	String znode;

	DataMonitor dm;

	ZooKeeper zk;

	String filename;

	Process child;

	public Executor(String hostPort, String znode, String filename) throws KeeperException, IOException {
		this.filename = filename;
		zk = new ZooKeeper(hostPort, 3000, this);
		dm = new DataMonitor(zk, znode, null, this);
	}

	public void process(WatchedEvent event) {
		dm.process(event);
	}

	public void run() {
		try {
			synchronized (this) {
				while (!dm.dead) {
					wait();
				}
			}
		} catch (InterruptedException e) {
		}
	}

	public void closing(int rc) {
		synchronized (this) {
			notifyAll();
		}
	}

	static class StreamWriter extends Thread {
		OutputStream os;

		InputStream is;

		StreamWriter(InputStream is, OutputStream os) {
			this.is = is;
			this.os = os;
			start();
		}

		public void run() {
			byte b[] = new byte[80];
			int rc;
			try {
				while ((rc = is.read(b)) > 0) {
					os.write(b, 0, rc);
				}
			} catch (IOException e) {
			}

		}
	}

	public void exists(byte[] data) {
		if (data == null) {
			if (child != null) {
				System.out.println("Killing process");
				child.destroy();
				try {
					child.waitFor();
				} catch (InterruptedException e) {
				}
			}
			child = null;
		} else {
			if (child != null) {
				System.out.println("Stopping child");
				child.destroy();
				try {
					child.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				FileOutputStream fos = new FileOutputStream(filename);
				fos.write(data);
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				String dataStr = new String(data);
				System.out.println("node getData : " + dataStr);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}