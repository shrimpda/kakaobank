package com.kakaobank.common.util;

public class StringUtil {

	private StringUtil() {

	}

	public static String formatDecimals(double value, String suffix) {
		String p = String.valueOf(value);
		int ix = p.indexOf('.') + 1;
		int ex = p.indexOf('E');
		char fraction = p.charAt(ix);
		if (fraction == '0') {
			if (ex != -1) {
				return p.substring(0, ix - 1) + p.substring(ex) + suffix;
			} else {
				return p.substring(0, ix - 1) + suffix;
			}
		} else {
			if (ex != -1) {
				return p.substring(0, ix) + fraction + p.substring(ex) + suffix;
			} else {
				return p.substring(0, ix) + fraction + suffix;
			}
		}
	}
}
