package com.kakaobank.server;

import com.kakaobank.server.cluster.zookeeper.Executor;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Test {

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		String hostPort = "localhost:2181";
		String znode = "/znode";
		String filename = "test";
		String exec[] = new String[args.length];
		System.arraycopy(args, 0, exec, 0, exec.length);
		try {
			new Executor(hostPort, znode, filename).run();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
