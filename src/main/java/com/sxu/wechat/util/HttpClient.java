package com.sxu.wechat.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blade.kit.StringKit;
import com.blade.kit.http.HttpRequest;
import com.blade.kit.json.JSONKit;
import com.blade.kit.json.JSONObject;
import com.sxu.wechat.core.WeChatMeta;
import com.sxu.wechat.exception.WeChatException;

public class HttpClient {

	public static Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);
	private WeChatMeta meta;

	public HttpClient(WeChatMeta meta) {
		super();
		this.meta = meta;
	}

	public JSONObject postJSON(String url, JSONObject body) {
		HttpRequest request = HttpRequest.post(url).contentType("application/json;charset=utf-8").header("Cookie", meta.getCookie())
				.send(body.toString());
		String res = request.body();
		request.disconnect();
		if (StringKit.isBlank(res)) {
			throw new WeChatException("请求微信接口异常");
		}

		JSONObject jsonObject = JSONKit.parseObject(res);
		JSONObject BaseResponse = jsonObject.get("BaseResponse").asJSONObject();
		if (null == BaseResponse || BaseResponse.getInt("Ret", -1) != 0) {
			LOGGER.warn("操作失败,{}", jsonObject);
		}
		return jsonObject;
	}

}
