package com.sxu.wechat.plugin.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.sxu.wechat.config.Constant;
import com.sxu.wechat.config.Enums.MsgType;
import com.sxu.wechat.core.WeChatMeta;

public class FilePipeLine implements MessagePipeLine {

	public void processs(WeChatMeta meta, String line, MsgType msgType) {

		File f = new File(Constant.configReader.get("app.msg_location"));
		if (!f.exists()) {
			f.mkdirs();
		}
		try {
			Files.write(Paths.get(Constant.configReader.get("app.msg_location")), line.toString().getBytes(), StandardOpenOption.CREATE,
					StandardOpenOption.WRITE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
