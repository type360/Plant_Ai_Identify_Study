package com.briup.exception;

import com.briup.utils.Result;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public Object handleException(Exception e) {
		Result<String> result = null;
		if (e instanceof ServiceException) {
			result = Result.error(((ServiceException) e).getResultCode());
		} else if (e instanceof DuplicateKeyException) {
			result = Result.error("数据有误,该数据已经存在");
		} else {
			result = Result.error(500, "服务器意外错误：" + e.getMessage());
		}
		return result;
	}
}
