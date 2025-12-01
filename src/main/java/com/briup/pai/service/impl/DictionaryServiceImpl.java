package com.briup.pai.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.briup.pai.common.constant.DictionaryConstant;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.exception.BriupAssert;
import com.briup.pai.common.exception.CustomException;
import com.briup.pai.convert.DictionaryConvert;
import com.briup.pai.dao.DictionaryMapper;
import com.briup.pai.entity.dto.DictionarySaveDTO;
import com.briup.pai.entity.po.Dictionary;
import com.briup.pai.entity.vo.DictionaryEchoVO;
import com.briup.pai.entity.vo.DictionaryPageVO;
import com.briup.pai.entity.vo.DropDownVO;
import com.briup.pai.entity.vo.PageVO;
import com.briup.pai.service.IDictionaryService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DictionaryServiceImpl extends ServiceImpl<DictionaryMapper, Dictionary> implements IDictionaryService {
    @Autowired
    DictionaryConvert dictionaryConvert;

    @Override
    public DictionaryEchoVO addOrModifyDictionary(DictionarySaveDTO dto) {
        Integer dictId = dto.getDictId();
        Integer parentId = dto.getParentId();
        String dictCode = dto.getDictCode();
        if (dictId == null) { //新增
            // dictCode不能重复
            /*LambdaQueryWrapper<Dictionary> qw = new LambdaQueryWrapper<Dictionary>();
            qw.eq(Dictionary::getDictCode, dictCode);
            Dictionary dictionaryFromDB = this.getOne(qw);
            if (dictionaryFromDB != null) {
                throw new CustomException(ResultCodeEnum.DATA_ALREADY_EXIST);
            }*/
            //这里和上面的功能一样只不过基础代码里已经有相同功能的函数，直接调用即可
            BriupAssert.requireNull(this,
                    Dictionary::getDictCode,
                    dictCode,
                    ResultCodeEnum.DATA_ALREADY_EXIST);
            // parentId必须存在
                //一级字典 parentId = 0
                //二级字典 parentId = 1 7 14 数据表里id的值
                    //只有一级分类才能添加二级分类
            List<Integer> parentIdList = getPrentIdList();
            BriupAssert.requireIn(parentId,parentIdList,ResultCodeEnum.PARAM_VERIFY_ERROR);
            this.save(dictionaryConvert.dictionarySaveDTO2PO(dto));

        }else {

        }


        return null;
    }

    @NotNull
    private List<Integer> getPrentIdList() {
        LambdaQueryWrapper<Dictionary> qw = Wrappers.<Dictionary>lambdaQuery().eq(Dictionary::getParentId, DictionaryConstant.PARENT_DICTIONARY_ID);
        List<Integer> parentIdList = this.list(qw).stream().map(Dictionary::getId).collect(Collectors.toList());
        parentIdList.add(DictionaryConstant.PARENT_DICTIONARY_ID);
        System.out.println("parentIdList = " + parentIdList);
        return parentIdList;
    }

    @Override
    public DictionaryEchoVO getDictionaryById(Integer dictionaryId) {
        return null;
    }

    @Override
    public void removeDictionaryById(Integer dictionaryId) {

    }

    @Override
    public PageVO<DictionaryPageVO> getDictionaryByPage(Long pageNum, Long pageSize) {
        return null;
    }

    @Override
    public List<DropDownVO> getDropDownList(String dictCode) {
        return List.of();
    }

    @Override
    public String getDictionaryValueById(Integer dictionaryId) {
        return "";
    }
}