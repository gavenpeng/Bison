/**
 * 
 */
package com.chamago.cometserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.chamago.cometserver.connection.CometConnection;
import com.chamago.cometserver.connection.CometConnectionManager;
import com.chamago.cometserver.connection.RedisClientManager;
import com.chamago.cometserver.connection.StreamCometConnection;

public class ReceMsgServlet extends HttpServlet {

	private final static Log LOG = LogFactory
	.getLog(ReceMsgServlet.class);
	
	private CometConnectionManager connectionManager;

	/**
	 * 
	 */
	private static final long serialVersionUID = 7791301745498733988L;

	@Override
	public void init(ServletConfig config) {
		LOG.info("启动接收消息服务");
		Object obj = config.getServletContext().getAttribute("cometserver.connect.manager");
		if(obj!=null){
			connectionManager = (CometConnectionManager)obj;
		}else{
			LOG.info("启动错误，主服务初始化失败");
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			
			String appkey = request.getParameter(StreamConstants.PARAM_APPKEY);
			String subject = request.getParameter(StreamConstants.PARAM_SUBJECT);
			String[] contents = request.getParameterValues(StreamConstants.PARAM_CONTENT);
			int code = 0;
			String err = "success";
			if(contents!=null&&contents.length>0){
				CometConnection connect = this.connectionManager.findCometConnection(appkey);
				if(connect!=null){
					List<PullEvent> events = new ArrayList<PullEvent>();
					for(String msg:contents){
						PullEvent pe = new PullEvent(appkey,StreamConstants.NEW_MESSAGE,subject,msg);
						events.add(pe);
					}
					((StreamCometConnection)connect).batchPullEvent(events);
					events.clear();
					events = null;
				}else{
					if(CometServer.NO_DISCARD){
						RedisClientManager rcm = RedisClientManager.getInstance();
						for(String msg:contents){
							PullEvent pe = new PullEvent(appkey,StreamConstants.NEW_MESSAGE,subject,msg);
							rcm.saveMsg(pe.getAppkey(), pe.getId(), pe.toString());
						}
					}else{
						code = 1;
						err = "客户端appkey["+appkey+"]没有和服务端建立连接，忽略掉该appkey的消息";
						LOG.warn(err);
					}
				}
			}
			this.sendMsg(response, code, err);
		} catch (Exception e) {
			try {
				this.sendMsg(response, 2,e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally{
			try {
				response.getWriter().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void destroy() {
    }

	
	public void sendMsg(HttpServletResponse response,int code,String msg) throws IOException{
		response.setCharacterEncoding("UTF-8");
		StringBuilder xmlC = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		xmlC.append("<response>");
		xmlC.append("<code>").append(code).append("</code>");
		xmlC.append("<error>").append(msg).append("</error>");
		xmlC.append("</response>");
		response.getWriter().write(xmlC.toString());
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
