/**
 * 
 */
package com.chamago.cometserver;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.chamago.cometserver.connection.CometConnection;
import com.chamago.cometserver.connection.CometConnectionManager;
import com.chamago.cometserver.connection.ConfirmManager;
import com.chamago.cometserver.connection.RedisClientFactory;
import com.chamago.cometserver.connection.RedisClientManager;
import com.chamago.cometserver.connection.StreamCometConnection;
import com.chamago.cometserver.connection.StreamMsgPullFactory;
import com.chamago.cometserver.sercret.AppKeyManager;
import com.chamago.cometserver.sercret.CopUtils;
import com.chamago.cometserver.util.Constants;
import com.chamago.cometserver.util.RequestParametersHolder;
import com.chamago.cometserver.util.TaobaoHashMap;

public class ConfirmServlet extends HttpServlet {

	private final static Log LOG = LogFactory
	.getLog(ConfirmServlet.class);
	
	private AppKeyManager akm;

	//private JedisPool pool; 

	/**
	 * 
	 */
	private static final long serialVersionUID = 7791301745498733988L;

	@Override
	public void init(ServletConfig config) {
		LOG.info("启动消息回执确认服务");
		this.akm = AppKeyManager.getInstance();
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			
			if(secretValidate(request,response)){
				postMessage(request, response);
			}else{
				LOG.warn("消息回执确认校验参数失败，请客户端检查");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	private void postMessage(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		String msgIds = request.getParameter("confirm_ids");
		String appkey = request.getParameter("app_key");
		LOG.info("收到客户端"+appkey+"确认消息ID:"+msgIds);
//		RedisClientManager rcm = RedisClientManager.getInstance();
//		rcm.batchDeleteMsg(appkey, msgIds);
		
	}
	
	@Override
	public void destroy() {
		//this.pool.destroy();
    }

	/**
	 * 安全验证
	 * @throws IOException 
	 */
	public boolean secretValidate(HttpServletRequest request, HttpServletResponse response) throws IOException{
		
		String timestamp = request.getParameter("timestamp");
		if(!checkTimestamp(timestamp)){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(StreamConstants.ERR_MSG_HEADER, URLEncoder.encode("timestamp误差不在服务器允许的15分钟范围内 ,而且不能超过服务器时间","UTF-8"));
			return false;
		}
		String sign = request.getParameter("sign");
		RequestParametersHolder requestHolder = this.praseReqParams(request);
		String appkey = request.getParameter("app_key");
		String app_secret = this.akm.findSecret(appkey);
		if(app_secret!=null){
			String serSign = CopUtils.signCopRequest(requestHolder,app_secret);
			if(!sign.equals(serSign)){
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.setHeader(StreamConstants.ERR_MSG_HEADER,URLEncoder.encode("签名无效，请检查appkey对应的密钥","UTF-8") );
				return false;
			}
		}else{
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(StreamConstants.ERR_MSG_HEADER, URLEncoder.encode("appkey["+appkey+"]不存在,请客户端检查","UTF-8"));
			return false;
		}
		return true;
		
	}
	
	public boolean checkTimestamp(String timestamp){
		long cts = Long.parseLong(timestamp);
		long now = System.currentTimeMillis();
		if(now-cts>Constants.VALID_TIMESTAMP){
			return false;
		}else if(cts-now>0){
			return false;
		}
		return true;
	}
	
	
	
	public RequestParametersHolder praseReqParams(HttpServletRequest request) throws IOException {
		TaobaoHashMap protocalMustParams = new TaobaoHashMap();
		Iterator<String> it = request.getParameterMap().keySet().iterator();
		while (it.hasNext()) {
			String name = it.next();
			if(!name.equals("sign")){
				String value = URLDecoder.decode(request.getParameter(name), "UTF-8");
				protocalMustParams.put(name, value);
			}
		}
		RequestParametersHolder requestHolder = new RequestParametersHolder();
		requestHolder.setProtocalMustParams(protocalMustParams);
		return requestHolder;

	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
