package com.chamago.cometserver.util;


public class RequestParametersHolder {
	private TaobaoHashMap protocalMustParams;
	private TaobaoHashMap protocalOptParams;
//	private TaobaoHashMap applicationMust;
//	private TaobaoHashMap applicationOpt;
	private TaobaoHashMap applicationParams;
	public TaobaoHashMap getProtocalMustParams() {
		return protocalMustParams;
	}
	public void setProtocalMustParams(TaobaoHashMap protocalMustParams) {
		this.protocalMustParams = protocalMustParams;
	}
	public TaobaoHashMap getProtocalOptParams() {
		return protocalOptParams;
	}
	public void setProtocalOptParams(TaobaoHashMap protocalOptParams) {
		this.protocalOptParams = protocalOptParams;
	}
	public TaobaoHashMap getApplicationParams() {
		return applicationParams;
	}
	public void setApplicationParams(TaobaoHashMap applicationParams) {
		this.applicationParams = applicationParams;
	}
	

	
}
