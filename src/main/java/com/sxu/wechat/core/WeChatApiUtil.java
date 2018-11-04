package com.sxu.wechat.core;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blade.kit.DateKit;
import com.blade.kit.StringKit;
import com.blade.kit.http.HttpRequest;
import com.blade.kit.json.JSONArray;
import com.blade.kit.json.JSONKit;
import com.blade.kit.json.JSONObject;
import com.sxu.wechat.config.Constant;
import com.sxu.wechat.exception.WeChatException;
import com.sxu.wechat.plugin.Storage;
import com.sxu.wechat.util.CookieUtil;
import com.sxu.wechat.util.HttpClient;
import com.sxu.wechat.util.Matchers;

public class WeChatApiUtil {
	static final Logger LOGGER = LoggerFactory.getLogger(WeChatApiUtil.class);

	public static String getUUID() throws WeChatException {
		HttpRequest request = HttpRequest.get(Constant.JS_LOGIN_URL, true, "appid", "wx782c26e4c19acffb", "fun", "new", "lang", "zh_CN", "_",
				DateKit.getCurrentUnixTime());
		String res = request.body();
		request.disconnect();
		if (StringKit.isBlank(res)) {
			throw new WeChatException("获取UUID失败");
		}
		String code = Matchers.match("window.QRLogin.code = (\\d+);", res);
		if (null == code || !code.equals("200")) {
			throw new WeChatException("获取UUID失败，" + code);
		}
		String uuid = Matchers.match("window.QRLogin.uuid = \"(.*)\";", res);
		return uuid;
	}

	public static String getQrCode(String uuid) throws WeChatException {

		if (StringKit.isBlank(uuid)) {
			throw new WeChatException("请先获取UUID");
		}
		String url = Constant.QRCODE_URL + uuid;
		String folder=System.getProperty("java.io.tmpdir");
		final File output = new File(folder+File.separator+"wechat4j_qrcode.jpg");
		HttpRequest.post(url, true, "t", "webwx", "_", DateKit.getCurrentUnixTime()).receive(output);

		if (null == output || !output.exists() || !output.isFile()) {
			throw new WeChatException("获取登陆二维码失败");
		}
		return output.getPath();

	}

	public static String waitLogin(int tip, String uuid) {
		String url = "https://login.weixin.qq.com/cgi-bin/mmwebwx-bin/login";
		HttpRequest request = HttpRequest.get(url, true, "tip", tip, "uuid", uuid, "_", DateKit.getCurrentUnixTime());
		LOGGER.warn("等待登陆");
		String res = request.body();
		request.disconnect();
		return res;

	}

	public static WeChatMeta newWeChatMeta(String waitloginRes) {
		String pm = Matchers.match("window.redirect_uri=\"(\\S+?)\";", waitloginRes);
		String redirectUrl = pm + "&fun=new";
		String baseUrl = redirectUrl.substring(0, redirectUrl.lastIndexOf("/"));
		WeChatMeta meta = new WeChatMeta();
		meta.setRedirect_uri(redirectUrl);
		meta.setBase_uri(baseUrl);
		meta.setLoginDate(new Date());
		return meta;
	}

	public static void login(WeChatMeta meta) throws WeChatException {

		if (StringKit.isBlank(meta.getRedirect_uri())) {
			throw new WeChatException("redirect_url不能为空");
		}

		HttpRequest request = HttpRequest.get(meta.getRedirect_uri());
		String res = request.body();
		meta.setCookie(CookieUtil.getCookie(request));
		request.disconnect();
		if (StringKit.isBlank(res)) {
			throw new WeChatException("登录失败");
		}
		meta.setSkey(Matchers.match("<skey>(\\S+)</skey>", res));
		meta.setWxsid(Matchers.match("<wxsid>(\\S+)</wxsid>", res));
		meta.setWxuin(Matchers.match("<wxuin>(\\S+)</wxuin>", res));
		meta.setPass_ticket(Matchers.match("<pass_ticket>(\\S+)</pass_ticket>", res));

		JSONObject baseRequest = new JSONObject();
		baseRequest.put("Uin", meta.getWxuin());
		baseRequest.put("Sid", meta.getWxsid());
		baseRequest.put("Skey", meta.getSkey());
		baseRequest.put("DeviceID", meta.getDeviceId());
		meta.setBaseRequest(baseRequest);
	}

	public static JSONObject wxInit(WeChatMeta meta) throws WeChatException {
		String url = meta.getBase_uri() + "/webwxinit?r=" + DateKit.getCurrentUnixTime() + "&lang=zh_CN";
		JSONObject body = new JSONObject();
		body.put("BaseRequest", meta.getBaseRequest());
		HttpRequest request = HttpRequest.post(url).contentType("application/json;charset=utf-8").header("Cookie", meta.getCookie())
				.send(body.toString());
		String res = request.body();
		request.disconnect();

		JSONObject jsonObject = null;
		if (StringKit.isBlank(res) || (jsonObject = JSONKit.parseObject(res)) == null) {
			throw new WeChatException("微信初始化失败");
		}

		JSONObject BaseResponse = jsonObject.get("BaseResponse").asJSONObject();
		if (null == BaseResponse || BaseResponse.getInt("Ret", -1) != 0) {
			throw new WeChatException("微信初始化失败," + BaseResponse);
		}

		meta.setSyncKey(jsonObject.get("SyncKey").asJSONObject());
		meta.setUser(jsonObject.get("User").asJSONObject());

		StringBuffer synckey = new StringBuffer();
		JSONArray list = meta.getSyncKey().get("List").asArray();
		for (int i = 0, len = list.size(); i < len; i++) {
			JSONObject item = list.get(i).asJSONObject();
			synckey.append("|" + item.getInt("Key", 0) + "_" + item.getInt("Val", 0));
		}
		meta.setSynckey(synckey.substring(1));
		return jsonObject;
	}

	public static void openStatusNotify(WeChatMeta meta) throws WeChatException {
		String url = meta.getBase_uri() + "/webwxstatusnotify?lang=zh_CN&pass_ticket=" + meta.getPass_ticket();

		JSONObject body = new JSONObject();
		body.put("BaseRequest", meta.getBaseRequest());
		body.put("Code", 3);
		body.put("FromUserName", meta.getUser().getString("UserName"));
		body.put("ToUserName", meta.getUser().getString("UserName"));
		body.put("ClientMsgId", DateKit.getCurrentUnixTime());

		HttpRequest request = HttpRequest.post(url).contentType("application/json;charset=utf-8").header("Cookie", meta.getCookie())
				.send(body.toString());
		String res = request.body();
		request.disconnect();

		if (StringKit.isBlank(res)) {
			throw new WeChatException("状态通知开启失败");
		}

		JSONObject jsonObject = JSONKit.parseObject(res);
		JSONObject BaseResponse = jsonObject.get("BaseResponse").asJSONObject();
		if (null == BaseResponse || BaseResponse.getInt("Ret", -1) != 0) {
			throw new WeChatException("状态通知开启失败");
		}
		LOGGER.debug("状态通知已开启");
	}

	public static int[] syncCheck(WeChatMeta meta, String url) {
		int[] arr = new int[] { -1, -1 };
		try {
			url = url == null ? meta.getWebpush_url() + "/synccheck" : "https://" + url + "/cgi-bin/mmwebwx-bin/synccheck";
			JSONObject body = new JSONObject();
			body.put("BaseRequest", meta.getBaseRequest());
			HttpRequest request = HttpRequest
					.get(url, true, "r", DateKit.getCurrentUnixTime() + StringKit.getRandomNumber(5), "skey", meta.getSkey(), "uin", meta.getWxuin(),
							"sid", meta.getWxsid(), "deviceid", meta.getDeviceId(), "synckey", meta.getSynckey(), "_", System.currentTimeMillis())
					.header("Cookie", meta.getCookie());
			String res = request.body();
			request.disconnect();
			if (StringKit.isBlank(res)) {
				return arr;
			}

			String retcode = Matchers.match("retcode:\"(\\d+)\",", res);
			String selector = Matchers.match("selector:\"(\\d+)\"}", res);
			if (null != retcode && null != selector) {
				arr[0] = Integer.parseInt(retcode);
				arr[1] = Integer.parseInt(selector);
				return arr;
			}
			return arr;
		} catch (Exception ex) {
			return arr;
		}
	}

	public static int[] syncCheck(WeChatMeta meta) throws WeChatException {
		return syncCheck(meta, null);
	}

	public static void choiceSyncLine(WeChatMeta meta) throws WeChatException {
		boolean enabled = false;
		for (String syncUrl : Constant.SYNC_HOST) {
			int[] res = syncCheck(meta, syncUrl);
			if (res[0] == 0) {
				String url = "https://" + syncUrl + "/cgi-bin/mmwebwx-bin";
				meta.setWebpush_url(url);
				LOGGER.info("选择线路：[{}]", syncUrl);
				enabled = true;
				break;
			}
		}
		if (!enabled) {
			throw new WeChatException("同步线路不通畅");
		}
	}

	public static JSONObject webwxsync(WeChatMeta meta) throws WeChatException {
		String url = meta.getBase_uri() + "/webwxsync?skey=" + meta.getSkey() + "&sid=" + meta.getWxsid();

		JSONObject body = new JSONObject();
		body.put("BaseRequest", meta.getBaseRequest());
		body.put("SyncKey", meta.getSyncKey());
		body.put("rr", DateKit.getCurrentUnixTime());

		HttpClient httpRequsetUtil = new HttpClient(meta);
		JSONObject jsonObject = httpRequsetUtil.postJSON(url, body);
		JSONObject BaseResponse = jsonObject.get("BaseResponse").asJSONObject();
		if (null != BaseResponse) {
			int ret = BaseResponse.getInt("Ret", -1);
			if (ret == 0) {
				meta.setSyncKey(jsonObject.get("SyncKey").asJSONObject());
				StringBuffer synckey = new StringBuffer();
				JSONArray list = meta.getSyncKey().get("List").asArray();
				for (int i = 0, len = list.size(); i < len; i++) {
					JSONObject item = list.get(i).asJSONObject();
					synckey.append("|" + item.getInt("Key", 0) + "_" + item.getInt("Val", 0));
				}
				meta.setSynckey(synckey.substring(1));
				return jsonObject;
			}
		}
		return null;
	}

	public static String webwxsendmsg(WeChatMeta meta, String content, JSONObject to) {
		String url = meta.getBase_uri() + "/webwxsendmsg?lang=zh_CN&pass_ticket=" + meta.getPass_ticket();
		final JSONObject body = new JSONObject();
		String clientMsgId = DateKit.getCurrentUnixTime() + StringKit.getRandomNumber(5);
		JSONObject Msg = new JSONObject();

		Msg.put("Type", 1);
		Msg.put("Content", content);
		Msg.put("FromUserName", meta.getUser().getString("UserName"));
		Msg.put("ToUserName", to.get("UserName"));
		Msg.put("LocalID", clientMsgId);
		Msg.put("ClientMsgId", clientMsgId);

		body.put("BaseRequest", meta.getBaseRequest());
		body.put("Msg", Msg);
		final HttpRequest request = HttpRequest.post(url).contentType("application/json;charset=utf-8").header("Cookie", meta.getCookie());
		request.send(body.toString());
		String msgResult = request.body();
		request.disconnect();
		return msgResult;

	}

	/**
	 * 添加好友
	 * 
	 * @param userName
	 * @param status
	 *            2,default=2
	 * @param verifyContent
	 * @param autoUpdate
	 */
	public static JSONObject addFriend(WeChatMeta meta, String userName, String verifyContent) {
		String url = "{0}/webwxverifyuser?r={1}&pass_ticket={2}";
		url = MessageFormat.format(url, meta.getBase_uri(), DateKit.getCurrentUnixTime(), meta.getPass_ticket());

		JSONObject body = new JSONObject();
		body.put("BaseRequest", meta.getBaseRequest());
		body.put("Opcode", 2);
		body.put("VerifyUserListSize", 1);
		JSONArray VerifyUserList = new JSONArray();
		JSONObject verifyItem = new JSONObject();
		verifyItem.put("Value", userName);
		verifyItem.put("VerifyUserTicket", "");
		VerifyUserList.add(verifyItem);
		body.put("VerifyUserList", VerifyUserList);
		body.put("VerifyContent", verifyContent);
		body.put("SceneListCount", 1);
		JSONArray sceneList = new JSONArray();
		sceneList.add(33);
		body.put("SceneList", sceneList);
		body.put("skey", meta.getSkey());

		HttpClient httpRequsetUtil = new HttpClient(meta);
		JSONObject resp = httpRequsetUtil.postJSON(url, body);
		return resp;

	}

	/**
	 * 获取通讯录列表
	 */
	public static JSONArray getContactList(WeChatMeta meta) {
		String url = meta.getBase_uri() + "/webwxgetcontact?&seq=0&pass_ticket=" + meta.getPass_ticket() + "&skey=" + meta.getSkey() + "&r="
				+ DateKit.getCurrentUnixTime();
		JSONObject body = new JSONObject();
		body.put("BaseRequest", meta.getBaseRequest());
		HttpClient httpRequsetUtil = new HttpClient(meta);
		JSONObject response = httpRequsetUtil.postJSON(url, body);
		JSONArray memberList = response.get("MemberList").asArray();
		return memberList;

	}

	/***
	 * 初始化群(群名称需要从最新联系人或通过录里筛选)
	 * 
	 * @param meta
	 * @param chatRommNames
	 */
	public static void initLatestChatroom(WeChatMeta meta, List<String> chatRommNames) {
		JSONArray groupArray = new JSONArray();
		// for (String groupUserName :
		// Storage.instance().getLatestChatRoomUserNameList()) {
		for (String groupUserName : chatRommNames) {
			JSONObject groupItem = new JSONObject();
			groupItem.put("UserName", groupUserName);
			groupItem.put("ChatRoomId", "");
			groupArray.add(groupItem);
		}
		JSONArray latestChatRoomList = getMemberListByChatroom(meta, groupArray);
		Storage.instance().setLatestChatRoomList(latestChatRoomList);

	}

	/**
	 * 获取取成员列表
	 * @param meta
	 * @param chatRoomList
	 * @return
	 */
	public static JSONArray getMemberListByChatroom(WeChatMeta meta, JSONArray chatRoomList) {
		String url = meta.getBase_uri() + "/webwxbatchgetcontact?type=ex&r=" + DateKit.getCurrentUnixTime() + "&lang=zh_CN";
		JSONObject body = new JSONObject();
		body.put("BaseRequest", meta.getBaseRequest());
		body.put("Count", chatRoomList.size());
		body.put("List", chatRoomList);
		HttpRequest request = HttpRequest.post(url).contentType("application/json;charset=utf-8").header("Cookie", meta.getCookie())
				.send(body.toString());
		String res = request.body();
		request.disconnect();
		if (StringKit.isBlank(res)) {
			throw new WeChatException("获取群成员列表失败");
		}
		JSONObject jsonObject = JSONKit.parseObject(res);
		JSONObject baseResponse = jsonObject.get("BaseResponse").asJSONObject();
		if (null == baseResponse || baseResponse.getInt("Ret", -1) != 0) {
			LOGGER.warn("获取群列表失败,{}", baseResponse);
			return null;
		}
		JSONArray memberList = jsonObject.get("ContactList").asArray();
		// LOGGER.info("获取群成员:{}", memberList);
		return memberList;

	}
}
