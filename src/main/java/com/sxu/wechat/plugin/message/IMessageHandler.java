package com.sxu.wechat.plugin.message;

import com.blade.kit.json.JSONObject;

public interface IMessageHandler {
	public void process(JSONObject msg);


}
