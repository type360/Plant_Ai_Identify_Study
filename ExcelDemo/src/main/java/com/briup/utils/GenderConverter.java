package com.briup.utils;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

//性别转换器：实现Java类属性(Integer) <--> Excel表格数据(String)
public class GenderConverter implements Converter<Integer> {

	/**
	 * Java 字段的数据类型-Integer
	 */
	@Override
	public Class<?> supportJavaTypeKey() {
		return Integer.class;
	}

	/**
	 * Excel文件中单元格的数据类型-String
	 */
	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return CellDataTypeEnum.STRING;
	}

	/**
	 * 将单元格里的数据转为java对象,也就是 男 转为1,女 转成 0,用于导入excel时对性别字段进行转换
	 * @param cellData            数据对象
	 * @param contentProperty     单元格内容属性
	 * @param globalConfiguration 全局配置对象
	 * @return Integer
	 */
	@Override
	public Integer convertToJavaData(ReadCellData<?> cellData,
	                                 ExcelContentProperty contentProperty,
	                                 GlobalConfiguration globalConfiguration) {
		return cellData.getStringValue().equals("男") ? 1 : 0;
	}

	/**
	 * 在导出时,将性别字段中的 0 转换为 女 1 转换为 男
	 * @param value               性别字段的值 1为男 0为女
	 * @param contentProperty     单元格内容属性
	 * @param globalConfiguration 全局配置对象
	 */
	@Override
	public WriteCellData<?> convertToExcelData(Integer value,
	                                           ExcelContentProperty contentProperty,
	                                           GlobalConfiguration globalConfiguration) {
		return new WriteCellData<Integer>(value.equals(1) ? "男" : "女");
	}
}
