package com.briup.pai.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
        Dictionary dictionaryV = null;//新增和修改都要返回
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
            dictionaryV = dictionaryConvert.dictionarySaveDTO2PO(dto);
            this.save(dictionaryV);

        }else {//修改
            //id必须存在
            Dictionary dictionary = BriupAssert.requireNotNull(this, Dictionary::getId, dictId, ResultCodeEnum.DATA_NOT_EXIST);
            //数据字典key不能更改[前端传过来的和数据库已经存在的要一致]
            BriupAssert.requireEqual(dictCode,dictionary.getDictCode(),ResultCodeEnum.PARAM_IS_ERROR);
            dictionaryV = dictionaryConvert.dictionarySaveDTO2PO(dto);
            this.updateById(dictionaryV);

        }


        return dictionaryConvert.po2DictionaryEchoVO(dictionaryV);
    }

    @NotNull
    private List<Integer> getPrentIdList() {
        LambdaQueryWrapper<Dictionary> qw = Wrappers.<Dictionary>lambdaQuery().eq(Dictionary::getParentId, DictionaryConstant.PARENT_DICTIONARY_ID);
        List<Integer> parentIdList = this.list(qw).stream().map(Dictionary::getId).collect(Collectors.toList());
        parentIdList.add(DictionaryConstant.PARENT_DICTIONARY_ID);
        System.out.println("parentIdList = " + parentIdList);
        return parentIdList;
    }

    //dto  后端接收数据用的
    // po
    //vo  后端响应前端数据用的
    @Override
    public DictionaryEchoVO getDictionaryById(Integer dictionaryId) {
        //根据id查询数字字典 如果查询不到，抛出异常
        Dictionary byId = this.getById(dictionaryId);
        Dictionary dictionary = BriupAssert.requireNotNull(this, Dictionary::getId, dictionaryId, ResultCodeEnum.DATA_NOT_EXIST);

        return dictionaryConvert.po2DictionaryEchoVO(dictionary);
    }

    @Override
    public void removeDictionaryById(Integer dictionaryId) {
        //id必须存在
        BriupAssert.requireNotNull(this, Dictionary::getId, dictionaryId, ResultCodeEnum.DATA_NOT_EXIST);
        //先删除二级字典，再删除一级字典[逻辑删除]
        // delete from dictionary where parentId = 0
        this.remove(Wrappers.<Dictionary>lambdaQuery().eq(Dictionary::getParentId, dictionaryId));
        // delete from dictionary where id = 0
        this.removeById(dictionaryId);

    }

    @Override
    public PageVO<DictionaryPageVO> getDictionaryByPage(Long pageNum, Long pageSize) {
        PageVO<DictionaryPageVO> vo = new PageVO<>();
        //先查询一级字典
        Page<Dictionary> page = new Page<>(pageNum, pageSize);
        page = this.page(page,Wrappers
                .<Dictionary>query().lambda()
                .eq(Dictionary::getParentId, DictionaryConstant.PARENT_DICTIONARY_ID)
        );
        List<DictionaryPageVO> list = dictionaryConvert.po2DictionaryPageVOList(page.getRecords())
                .stream()
                .peek(dictionaryPageVO -> {
                    List<Dictionary> dictionaryList = this.list(Wrappers
                            .<Dictionary>lambdaQuery()
                            .eq(Dictionary::getParentId, dictionaryPageVO.getDictId()));
                    List<DictionaryPageVO> children = dictionaryConvert.po2DictionaryPageVOList(dictionaryList);
                    dictionaryPageVO.setChildren(children);
                })//peek是一个中间态方法，不影响最后结果的生成，同时也因为这里要做二级分类的封装所以采用这个方法
                .toList();


        // 每个一级分类 分别设置二级字典 parentId = 1 4 7[id]
        // select * from dictionary where parent = [1 4 7 21]

        //每个一级字典 分别设置二级字典
        vo.setTotal(page.getTotal());
        //此时只封装了一级分类
        vo.setData(list);
        return vo;
    }

    /*
    * 查询数据集类型下的二级字典的信息*/
    @Override
    public List<DropDownVO> getDropDownList(String dictCode) {
        //根据【dictCode 一级字典编码】查询二级字典数据
        Dictionary dictionary = BriupAssert.requireNotNull(this, Dictionary::getDictCode, dictCode, ResultCodeEnum.DATA_NOT_EXIST);
        LambdaQueryWrapper<Dictionary> qw = Wrappers.<Dictionary>lambdaQuery();
        qw.eq(Dictionary::getParentId, dictionary.getId());
        List<Dictionary> second = this.list(qw);
        // List<Dictionary> -> List<DropDownVO>
        return dictionaryConvert.po2DictionaryDropDownVOList(second);
    }

    @Override
    public String getDictionaryValueById(Integer dictionaryId) {
        return "";
    }
}