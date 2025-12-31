package com.briup.exception;

import com.briup.utils.ResultCode;


public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private ResultCode resultCode;

	public ServiceException(ResultCode resultCode) {
		this(resultCode.msg());
		this.resultCode = resultCode;
	}

	private ServiceException(String message) {
		super(message);
	}

	//获取枚举响应对象
	public ResultCode getResultCode() {
		return resultCode;
	}
}
