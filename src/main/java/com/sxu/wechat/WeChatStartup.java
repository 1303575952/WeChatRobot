package com.sxu.wechat;

import java.awt.EventQueue;

import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blade.kit.json.JSONArray;
import com.blade.kit.json.JSONObject;
import com.sxu.wechat.config.Constant;
import com.sxu.wechat.plugin.WxLocalCache;
import com.sxu.wechat.util.Matchers;
import com.sxu.wechat.core.WeChatApiUtil;
import com.sxu.wechat.core.WeChatMeta;
import com.sxu.wechat.exception.WeChatException;
import com.sxu.wechat.plugin.QRCodeWindow;

public class WeChatStartup {
	protected Logger logger = LoggerFactory.getLogger(WeChatStartup.class);
	private QRCodeWindow qrCodeFrame;
	private WeChatMeta meta;

	private WeChatStartup() {
		System.setProperty("https.protocols", "TLSv1");
		System.setProperty("jsse.enableSNIExtension", "false");
	}

	public static WeChatMeta login() {
		try {
			WeChatStartup instance = new WeChatStartup();
			instance.doLogin();
			return instance.meta;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private void doLogin() throws Exception {
		String uuid = WeChatApiUtil.getUUID();
		showQrCode(uuid);
		while(true){
			String res = WeChatApiUtil.waitLogin(0, uuid);
			String code = Matchers.match("window.code=(\\d+);", res);
			if (!Constant.HTTP_OK.equals(code)) {
				Thread.sleep(2000);
				continue;
			}
			closeQrWindow();
			WeChatMeta meta = WeChatApiUtil.newWeChatMeta(res);
			WeChatApiUtil.login(meta);
			JSONObject wxInitObj = WeChatApiUtil.wxInit(meta);
			WeChatApiUtil.openStatusNotify(meta);
			this.meta = meta;
			JSONArray contactList = wxInitObj.get("ContactList").asArray();
			WxLocalCache.init(this.meta).setLatestContactList(contactList);
			break;
		}
	}
	
	private void showQrCode(String uuid) throws WeChatException{
		final String path = WeChatApiUtil.getQrCode(uuid);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
					qrCodeFrame = new QRCodeWindow(path);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	private void closeQrWindow() {
		qrCodeFrame.dispose();
	}

	public WeChatMeta getMeta() {
		return meta;
	}
}
