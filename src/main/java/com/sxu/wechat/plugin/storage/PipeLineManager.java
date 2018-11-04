package com.sxu.wechat.plugin.storage;

import java.util.ArrayList;
import java.util.List;

import com.sxu.wechat.config.Enums.MsgType;
import com.sxu.wechat.core.WeChatMeta;

public class PipeLineManager {
	private List<MessagePipeLine> pipelines;

	static class Holder {
		static PipeLineManager instance = new PipeLineManager();
	}

	public static PipeLineManager instance() {
		return Holder.instance;
	}

	private PipeLineManager() {
		pipelines = new ArrayList<MessagePipeLine>();
	}

	public void process(WeChatMeta meta, String line, MsgType msgType) {
		for (MessagePipeLine pipeLine : pipelines) {
			if (pipeLine != null) {
				pipeLine.processs(meta, line, msgType);
			}

		}
	}
}
