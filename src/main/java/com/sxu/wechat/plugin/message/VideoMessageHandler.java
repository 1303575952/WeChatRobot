package com.sxu.wechat.plugin.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blade.kit.json.JSONObject;
import com.sxu.wechat.config.Enums.MsgType;
import com.sxu.wechat.core.WeChatMeta;

public class VideoMessageHandler extends AbstractMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(VideoMessageHandler.class);

	public VideoMessageHandler(WeChatMeta meta) {
		super(meta);
		this.meta = meta;
	}

	public void process(JSONObject msg) {
		LOGGER.info("开始处理视频消息");
		download(msg, MsgType.VIDEO);
	}



}
