package com.kakaobank.server.encrypt;

import com.kakaobank.server.configs.Config;
import java.util.function.Function;

public class Encrypt {

	public static final Config<String> PASSWORD = new Config<>("encrypt.password", configs -> "password",
		Function.identity());
}
