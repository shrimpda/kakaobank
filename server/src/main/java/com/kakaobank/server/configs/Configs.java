package com.kakaobank.server.configs;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class Configs {

	private final Map<String, Object> configMap;

	private Configs(Map<String, Object> configMap) {
		this.configMap = Collections.unmodifiableMap(new TreeMap<>(configMap));
	}

	public String get(String config, String defaultValue) {
		String returnValue = get(config);
		return returnValue == null ? defaultValue : returnValue;
	}

	public String get(String config) {
		return toString(configMap.get(config));
	}

	private static String toString(Object o) {
		return o == null ? null : o.toString();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Map<String, Object> configMap = new TreeMap<>();

		public Builder putAll(Map<String, Object> configMap) {
			this.configMap.putAll(configMap);

			return this;
		}

		public Configs build() {
			return new Configs(configMap);
		}
	}
}
