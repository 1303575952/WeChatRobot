package com.sxu.wechat.plugin.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blade.kit.json.JSONObject;
import com.sxu.wechat.config.Enums.MsgType;
import com.sxu.wechat.core.WeChatMeta;
import com.sxu.wechat.plugin.storage.PipeLineManager;

public class TxtMessageHandler extends AbstractMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TxtMessageHandler.class);

	// private AccountConfigDao accountConfigDao = new AccountConfigDao();

	public TxtMessageHandler(WeChatMeta meta) {
		super(meta);
		this.meta = meta;
	}

	public void process(JSONObject msg) {
		String content = msg.getString("Content");
		LOGGER.info("content:{}", content);
		if (!preHandle(msg)) {
			return;
		}

		String fromUserName = msg.getString("FromUserName");
		String fromNickName = getUserRemarkName(fromUserName);
		String selftName = getSelfNickName();
		String[] contentArray = content.split(":");
		if (contentArray.length == 0 || contentArray.length == 1) {
		} else {
			content = contentArray[1].replace("<br/>", "\n");
		}
		PipeLineManager.instance().process(meta, fromNickName + "-" + selftName + ":" + content, MsgType.TXT);
		// String autoReplay = accountConfigDao.selectOne(meta.getMobile(),
		// AccountConfig.NAME_AUTO_REPLAY);
		// if (!AccountConfig.ON.equals(autoReplay)) {
		// LOGGER.warn("auto reply setting was off,you can switch on it");
		// return;
		// }
		String ans = reply(fromUserName, content);
		//webwxsendmsg(ans, msg.getString("FromUserName"));
		PipeLineManager.instance().process(meta, selftName + "-" + fromNickName + "：" + ans, MsgType.TXT);
	}

}
