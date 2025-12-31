package com.briup.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.briup.bean.UserVO;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UserListener extends AnalysisEventListener<UserVO> {

	/**
	 * 单次缓存的数据量，达到该值后存储一次数据库，然后清理 list,方便内存回收
	 */
	private static final int BATCH_COUNT = 5;

	/**
	 * 缓存的数据列表
	 */
	private final List<UserVO> userList = new ArrayList<>();

	@Override
	public void invoke(UserVO data, AnalysisContext context) {
		log.info("解析一行数据:{}", data);
		userList.add(data);
		// 数据量达到设置的阈值，处理一次数据，然后清空列表，防止内存中数据量过大导致OOM
		if (userList.size() >= BATCH_COUNT) {
			// 处理数据,如输出数据
			log.info("--------开始处理数据--------");
			for (UserVO user : userList) {
				log.info("处理数据:{}",user);
			}
			// 清空数据列表
			userList.clear();
		}
	}

	@Override
	public void doAfterAllAnalysed(AnalysisContext context) {
		// 处理剩余的没达到阈值的数据
		if (userList.size() > 0) {
			// 处理数据,如输出数据
			log.info("--------开始处理数据--------");
			for (UserVO user : userList) {
				log.info("处理数据:{}",user);
			}
			// 清空数据列表
			userList.clear();
		}
		log.info("解析完成");
	}

	/**
	 * 出现异常回调
	 *
	 * @param exception 存在的异常
	 * @param context 分析上下文
	 * @throws Exception 抛出的异常
	 */
	@Override
	public void onException(Exception exception, AnalysisContext context) throws Exception {
		// 对格式转化异常进行处理
		if (exception instanceof ExcelDataConvertException) {
			//从0开始计算
			int columnIndex =
					((ExcelDataConvertException) exception).getColumnIndex() + 1;
			int rowIndex =
					((ExcelDataConvertException) exception).getRowIndex() + 1;
			//封装具体异常信息
			String message = "第" + rowIndex + "行，第" +
					columnIndex + "列" + "数据格式有误，请核实";

			exception.printStackTrace();
			throw new RuntimeException(message);
		} else if (exception instanceof RuntimeException) {
			throw exception;
		} else {
			super.onException(exception, context);
		}
	}
}
