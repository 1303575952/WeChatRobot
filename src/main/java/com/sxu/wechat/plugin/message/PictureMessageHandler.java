package com.sxu.wechat.plugin.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blade.kit.json.JSONObject;
import com.sxu.wechat.config.Enums.MsgType;
import com.sxu.wechat.core.WeChatMeta;

public class PictureMessageHandler extends AbstractMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(PictureMessageHandler.class);

	public PictureMessageHandler(WeChatMeta meta) {
		super(meta);
		this.meta = meta;
	}

	public void process(JSONObject msg) {
		LOGGER.info("开始处理图片消息");
		download(msg, MsgType.PICTURE);

	}
}
