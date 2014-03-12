package com.chamago.cometserver.util;

/**
 * 公用常量类
 * 
 * @author carver.gu
 * @since 1.0, Sep 12, 2009
 */
public abstract class Constants {

	public static final String HTTP_INVOKE = "HTTP";
	
	public static final String RBC_INVOKE = "RBC";
	
	public static final String TEST_INVOKE = "TEST";
	
	public static final String EHUB_SERVICE_URL = "SERVER_URL";
	//public static final String EHUB_SERVICE_URL = "http://localhost:8080/copserver/ehub";
	
	public static int connectTimeout = 3000;//3秒
	
	public static int readTimeout = 30000;//15秒
	
	public static int VALID_TIMESTAMP = 15*60*1000;//15分钟
	
	/** TOP默认时间格式 **/
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	/** TOP Date默认时区 **/
	public static final String DATE_TIMEZONE = "GMT+8";

	/** UTF-8字符�?**/
	public static final String CHARSET_UTF8 = "UTF-8";

	/** GBK字符�?**/
	public static final String CHARSET_GBK = "GBK";

	/** TOP JSON 应格�?*/
	public static final String FORMAT_JSON = "json";
	/** TOP XML 应格�?*/
	public static final String FORMAT_XML = "xml";

	/** MD5签名方式 */
	public static final String SIGN_METHOD_MD5 = "md5";
	/** HMAC签名方式 */
	public static final String SIGN_METHOD_HMAC = "hmac";
	/** TQL分隔�?*/
	public static final String TQL_SEPERATOR = "top_tql_seperator";
	
	/** SDK版本号 */
	public static final String SDK_VERSION = "top-sdk-java-20130416";
	
	/**
	 * 返回的错误码
	 */
	public static final String ERROR_RESPONSE = "error_response";
	public static final String ERROR_CODE = "code";
	public static final String ERROR_MSG = "msg";
	public static final String ERROR_SUB_CODE = "sub_code";
	public static final String ERROR_SUB_MSG = "sub_msg";

}
