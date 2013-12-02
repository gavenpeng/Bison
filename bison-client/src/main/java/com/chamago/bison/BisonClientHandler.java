/**
 * 
 */
package com.chamago.bison;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.chamago.bison.node.MinaNode;
import com.chamago.bison.util.ByteUtil;

/**
 * @author Gavin.peng
 * 
 * 2013-10-16 下午03:25:11
 × bison-client
 */
public class BisonClientHandler extends IoHandlerAdapter {
	private BisonContext bison;
	
	public BisonClientHandler(BisonContext bison)
    {
		this.bison = bison;
    }

    public void sessionOpened(IoSession session)
      throws Exception
    {
      session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
    }

    public void sessionClosed(IoSession session) throws Exception {
      MinaNode objNode = (MinaNode)session.getAttribute("bison.conetxt.sesionn.key");
      if (objNode != null) {
        objNode.setSession(null);
        objNode.setConnected(false);

        this.bison.connectQueue.offer(objNode);
        this.bison.startProcessor();

        this.bison.logger.info("连接关闭事件 " + objNode.toString());
      }
      session.removeAttribute("bison.conetxt.sesionn.key");
    }

    public void exceptionCaught(IoSession session, Throwable cause) {
      cause.printStackTrace();
      session.close(true);
    }

    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
      byte[] buf = new byte[8];
      ByteUtil.write(buf, 0, 0);
      ByteUtil.write(buf, 4, 1);
      session.write(buf);
    }

    public void messageReceived(IoSession session, Object message) {
      if (!this.bison.recvQueue.offer(message))
        System.out.println("接收消息入队列失败 ");
    }

}
