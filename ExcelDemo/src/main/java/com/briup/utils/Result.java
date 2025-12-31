package com.briup.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

	private Integer code;
	private String msg;
	private T data;

	/**
	 * 设置响应消息
	 * @param resultCode 结果枚举
	 */
	private void setResultCode(ResultCode resultCode) {
		this.code = resultCode.code();
		this.msg = resultCode.msg();
	}

	/**
	 * 成功响应,无数据
	 * @param <T> 响应数据的类型
	 * @return
	 */
	public static <T> Result<T> success() {
		Result<T> result = new Result<>();
		result.setResultCode(ResultCode.SUCCESS);
		return result;
	}

	/**
	 * 成功响应,有数据
	 * @param data 响应数据
	 * @param <T> 响应数据的类型
	 * @return
	 */
	public static <T> Result<T> success(T data) {
		Result<T> result = new Result<>();
		result.setResultCode(ResultCode.SUCCESS);
		result.setData(data);
		return result;
	}

	/**
	 * 失败响应,默认失败状态码为 0
	 * @param msg 消息
	 * @param <T> 返回的类型
	 * @return
	 */
	public static <T> Result<T> error(String msg) {
		return new Result<>(0, msg, null);
	}

	/**
	 * 失败响应, 自定义失败状态码及失败响应消息
	 * @param code 状态码
	 * @param msg 消息
	 * @param <T> 返回的类型
	 * @return
	 */
	public static <T> Result<T> error(Integer code, String msg) {
		return new Result<>(code, msg, null);
	}

	/**
	 * 失败响应, 从响应枚举中获取失败 响应状态码及响应消息
	 * @param resultCode 结果枚举
	 * @param <T> 返回的类型
	 * @return
	 */
	public static <T> Result<T> error(ResultCode resultCode) {
		Result<T> result = new Result<>();
		result.setResultCode(resultCode);
		return result;
	}
}