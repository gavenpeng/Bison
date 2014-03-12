/**
 * 
 */
package com.chamago.cometserver.sercret;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.chamago.cometserver.util.RequestParametersHolder;
import com.chamago.cometserver.util.StringUtils;
import com.chamago.cometserver.util.TaobaoHashMap;

/**
 * @author Gavin.peng
 * 
 *         2013-7-2 下午02:24:49 × cop
 */
public abstract class CopUtils {

	/**
	 * 给TOP请求签名。
	 * 
	 * @param requestHolder
	 *            所有字符型的TOP请求参数
	 * @param secret
	 *            签名密钥
	 * @return 签名
	 * @throws IOException
	 */
	public static String signCopRequest(RequestParametersHolder requestHolder,
			String secret) throws IOException {
		// 第一步：把字典按Key的字母顺序排序
		Map<String, String> sortedParams = new TreeMap<String, String>();
		TaobaoHashMap appParams = requestHolder.getApplicationParams();
		if (appParams != null && appParams.size() > 0) {
			sortedParams.putAll(appParams);
		}
		TaobaoHashMap protocalMustParams = requestHolder
				.getProtocalMustParams();
		if (protocalMustParams != null && protocalMustParams.size() > 0) {
			sortedParams.putAll(protocalMustParams);
		}
		TaobaoHashMap protocalOptParams = requestHolder.getProtocalOptParams();
		if (protocalOptParams != null && protocalOptParams.size() > 0) {
			sortedParams.putAll(protocalOptParams);
		}

		Set<Entry<String, String>> paramSet = sortedParams.entrySet();

		// 第二步：把所有参数名和参数值串在一起
		StringBuilder query = new StringBuilder(secret);
		for (Entry<String, String> param : paramSet) {
			if (StringUtils.areNotEmpty(param.getKey(), param.getValue())) {
				query.append(param.getKey()).append(param.getValue());
			}
		}
		query.append(secret);
		System.out.println(query.toString());
		// 第三步：使用MD5加密
		byte[] bytes = encryptMD5(query.toString());

		// 第四步：把二进制转化为大写的十六进制
		return byte2hex(bytes);
	}

	/**
	 * 给TOP请求签名。
	 * 
	 * @param requestHolder
	 *            所有字符型的TOP请求参数
	 * @param secret
	 *            签名密钥
	 * @param isHmac
	 *            是否为HMAC方式加密
	 * @return 签名
	 * @throws IOException
	 */
	public static String signCopRequestNew(
			RequestParametersHolder requestHolder, String secret, boolean isHmac)
			throws IOException {
		// 第一步：把字典按Key的字母顺序排序
		Map<String, String> sortedParams = new TreeMap<String, String>();
		TaobaoHashMap appParams = requestHolder.getApplicationParams();
		if (appParams != null && appParams.size() > 0) {
			sortedParams.putAll(appParams);
		}
		TaobaoHashMap protocalMustParams = requestHolder
				.getProtocalMustParams();
		if (protocalMustParams != null && protocalMustParams.size() > 0) {
			sortedParams.putAll(protocalMustParams);
		}
		TaobaoHashMap protocalOptParams = requestHolder.getProtocalOptParams();
		if (protocalOptParams != null && protocalOptParams.size() > 0) {
			sortedParams.putAll(protocalOptParams);
		}

		Set<Entry<String, String>> paramSet = sortedParams.entrySet();

		// 第二步：把所有参数名和参数值串在一起
		StringBuilder query = new StringBuilder();
		if (!isHmac) {
			query.append(secret);
		}
		for (Entry<String, String> param : paramSet) {
			if (StringUtils.areNotEmpty(param.getKey(), param.getValue())) {
				query.append(param.getKey()).append(param.getValue());
			}
		}

		// 第三步：使用MD5/HMAC加密
		byte[] bytes;
		if (isHmac) {
			bytes = encryptHMAC(query.toString(), secret);
		} else {
			query.append(secret);
			System.out.println(query.toString());
			bytes = encryptMD5(query.toString());
		}

		// 第四步：把二进制转化为大写的十六进制
		return byte2hex(bytes);
	}

	private static byte[] encryptHMAC(String data, String secret)
			throws IOException {
		byte[] bytes = null;
		try {
			SecretKey secretKey = new SecretKeySpec(
					secret.getBytes("UTF-8"), "HmacMD5");
			Mac mac = Mac.getInstance(secretKey.getAlgorithm());
			mac.init(secretKey);
			bytes = mac.doFinal(data.getBytes("UTF-8"));
		} catch (GeneralSecurityException gse) {
			String msg = getStringFromException(gse);
			throw new IOException(msg);
		}
		return bytes;
	}

	private static String getStringFromException(Throwable e) {
		String result = "";
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bos);
		e.printStackTrace(ps);
		try {
			result = bos.toString("UTF-8");
		} catch (IOException ioe) {
		}
		return result;
	}

	public static byte[] encryptMD5(String data) throws IOException {
		byte[] bytes = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			bytes = md.digest(data.getBytes("UTF-8"));
		} catch (GeneralSecurityException gse) {
			String msg = getStringFromException(gse);
			throw new IOException(msg);
		}
		return bytes;
	}

	public static String byte2hex(byte[] bytes) {
		StringBuilder sign = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				sign.append("0");
			}
			sign.append(hex.toUpperCase());
		}
		return sign.toString();
	}

	
}
