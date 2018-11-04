package com.sxu.wechat.plugin.storage;

import com.sxu.wechat.config.Enums.MsgType;
import com.sxu.wechat.core.WeChatMeta;

public interface MessagePipeLine {
	public void processs(WeChatMeta meata, String line, MsgType msgType);

}
