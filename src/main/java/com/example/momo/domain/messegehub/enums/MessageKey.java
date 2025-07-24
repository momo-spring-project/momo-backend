package com.example.momo.domain.messegehub.enums;

public enum MessageKey {
	CREATE("message.create"),
	UPDATE("message.update"),
	DELETE("message.delete"),
	JOIN("message.join"),
	CANCEL("message.cancel");

	private final String key;

	MessageKey(String key) {
		this.key = key;
	}

	public String key() {
		return key;
	}
}
