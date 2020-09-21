package com.kakaobank.server.node;

import com.kakaobank.common.io.StreamInput;
import com.kakaobank.common.io.StreamOutput;
import com.kakaobank.common.io.Writeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;

public class NodeInfo implements Writeable {

	private String name;
	private InetSocketAddress address;

	public NodeInfo(StreamInput input) throws IOException {
		this.name = input.readString();
		String host = input.readString();
		int port = input.readInt();
		this.address = InetSocketAddress.createUnresolved(host, port);
	}

	public NodeInfo(String name, InetSocketAddress address) {
		this.name = name;
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		NodeInfo nodeInfo = (NodeInfo) o;
		return Objects.equals(name, nodeInfo.name) &&
			Objects.equals(address, nodeInfo.address);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, address);
	}

	@Override
	public void writeTo(StreamOutput streamOutput) throws IOException {
		streamOutput.writeString(name);
		streamOutput.writeString(address.getHostString());
		streamOutput.writeInt(address.getPort());
	}
}
