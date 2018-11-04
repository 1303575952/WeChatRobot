package com.sxu.wechat.plugin.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blade.kit.json.JSONObject;
import com.sxu.wechat.config.Enums;
import com.sxu.wechat.core.WeChatMeta;

public class VoiceMessageHandler extends AbstractMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(VoiceMessageHandler.class);

	public VoiceMessageHandler(WeChatMeta meta) {
		super(meta);
		this.meta = meta;
	}

	public void process(JSONObject msg) {
		LOGGER.info("开始处理语音消息");
		download(msg, Enums.MsgType.VOICE);

	}

}
