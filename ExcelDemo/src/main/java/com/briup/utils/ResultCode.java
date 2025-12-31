package com.briup.utils;


public enum ResultCode {
	/* 成功与失败状态码 */
	SUCCESS(200, "success"),
	ERROR(500,"服务器内部异常");


	private final Integer code;
	private final String msg;

	ResultCode(Integer code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public Integer code() {
		return this.code;
	}


	public String msg() {
		return this.msg;
	}

}
