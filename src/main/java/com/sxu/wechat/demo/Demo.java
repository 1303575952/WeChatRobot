package com.sxu.wechat.demo;

import com.sxu.wechat.WeChatStartup;
import com.sxu.wechat.core.WeChatMeta;
import com.sxu.wechat.plugin.MessageListener;

public class Demo {
	public static void main(String[] args) {
		listenMessage();
	}
	//监听消息
	public static void listenMessage(){
		WeChatMeta meta = WeChatStartup.login();
		MessageListener listener = new MessageListener(meta);
		listener.listen();
	}
}
