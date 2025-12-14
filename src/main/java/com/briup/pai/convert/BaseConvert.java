package com.briup.pai.convert;

import com.briup.pai.common.utils.SecurityUtil;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
//这是一个工具类，叫什么都可以

@Mapper
public interface BaseConvert {

    BaseConvert INSTANCE = Mappers.getMapper(BaseConvert.class);

    // 获取创建用户名
    default String getCreateUser(Integer userId) {
        return "";
    }

    // 获取创建用户Id
    default Integer getCreateUserId() {
        return SecurityUtil.getUserId();
    }

    // 获取数据字典值
    default String getDictionaryValue(Integer dictionaryId) {
        return "";
    }
}
