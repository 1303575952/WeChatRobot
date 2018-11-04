package com.sxu.wechat.plugin.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blade.kit.json.JSONObject;
import com.sxu.wechat.core.WeChatMeta;

public class MpMessageHandler extends AbstractMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(VoiceMessageHandler.class);

	public MpMessageHandler(WeChatMeta meta) {
		super(meta);
		this.meta = meta;
	}

	public void process(JSONObject msg) {
		LOGGER.info("do nothing");
		webwxsendmsg("不知道你在说什么", msg.getString("FromUserName"));
	}

	

}
