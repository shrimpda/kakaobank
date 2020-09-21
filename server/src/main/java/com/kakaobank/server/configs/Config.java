package com.kakaobank.server.configs;

import java.util.function.Function;

public class Config<T> {

	private final String key;
	private final Function<Configs, String> defaultValue;
	private final Function<String, T> parser;

	public Config(String key, Function<Configs, String> defaultValue, Function<String, T> parser) {
		this.key = key;
		this.defaultValue = defaultValue;
		this.parser = parser;
	}

	public T get(Configs configs) {
		String value = configs.get(key, defaultValue.apply(configs));
		T parsed = parser.apply(value);

		return parsed;
	}
}
