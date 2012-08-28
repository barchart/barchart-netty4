package com.barchart.netty.host.impl;

public enum OperatingSystem {

	UNKNOWN("unknown"), //

	WINDOWS("windows"), //

	LINUX("linux"), //

	SUN_OS("sunos"), //

	MAC_OS("mac"), //

	;

	private final String code;

	private OperatingSystem(final String code) {
		this.code = code;
	}

	private static final OperatingSystem[] ENUM_VALS = values();

	public static OperatingSystem fromCode(final String code) {
		if (code == null) {
			return UNKNOWN;
		}
		for (final OperatingSystem os : ENUM_VALS) {
			if (code.toLowerCase().contains(os.code)) {
				return os;
			}
		}
		return UNKNOWN;
	}

	public static OperatingSystem detect() {
		return fromCode(System.getProperty("os.name"));
	}

	public static final OperatingSystem CURRENT = detect();

}
