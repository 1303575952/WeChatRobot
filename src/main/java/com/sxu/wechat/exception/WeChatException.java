package com.sxu.wechat.exception;

public class WeChatException extends RuntimeException {

	private static final long serialVersionUID = 209248116271894410L;

	public WeChatException() {
		super();
	}

	public WeChatException(String message) {
		super(message);
	}

	public WeChatException(Throwable cause) {
		super(cause);
	}

}
