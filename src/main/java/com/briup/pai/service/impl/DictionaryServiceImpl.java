package com.briup.pai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.briup.pai.common.constant.DictionaryConstant;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.exception.BriupAssert;
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
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// 继承service的方法
@CacheConfig(cacheNames = DictionaryConstant.DICTIONARY_CACHE_PREFIX)
@Service
public class DictionaryServiceImpl extends ServiceImpl<DictionaryMapper, Dictionary> implements IDictionaryService {
    //    @Autowired
//    private DictionaryMapper dictionaryMapper;
    @Autowired
    private DictionaryConvert dictionaryConvert;

    @CachePut(key = "#result.dictId")
    @Override
    public DictionaryEchoVO addOrModifyDictionary(DictionarySaveDTO dto) {
        Integer dictId = dto.getDictId();
        Integer parentId = dto.getParentId();
        String dictCode = dto.getDictCode(); // gender
        Dictionary dictionaryV = null; // 新增和修改都要返回
        if (dictId == null) {  // 新增
            // dictCode不能重复 select * from sys_dictionary where dict_code = #{}
//            LambdaQueryWrapper<Dictionary> qw = new LambdaQueryWrapper<Dictionary>();
//            qw.eq(Dictionary::getDictCode,dictCode);
//            Dictionary dictionaryFromDB = this.getOne(qw);
//            if(dictionaryFromDB != null){
//                throw new CustomException(ResultCodeEnum.DATA_ALREADY_EXIST);
//            }
            BriupAssert.requireNull(this, Dictionary::getDictCode, dictCode, ResultCodeEnum.DATA_ALREADY_EXIST);
            // parentId必须存在
            // 一级字典 parentId = 0
            // 二级词典 parentId = 1 7 14 数据表里id的值
            BriupAssert.requireIn(parentId, getParentIdList(), ResultCodeEnum.PARAM_VERIFY_ERROR);
            dictionaryV = dictionaryConvert.dictionarySaveDTO2PO(dto);
            this.save(dictionaryV);
        } else {// 修改
            // id必须存在
            Dictionary dictionary = BriupAssert.requireNotNull(this, Dictionary::getId, dictId, ResultCodeEnum.DATA_NOT_EXIST);
            // 数据字典key不能更改[前端传来的 和 数据库已经存在的要一致]
            BriupAssert.requireEqual(dictCode,dictionary.getDictCode(),ResultCodeEnum.PARAM_IS_ERROR);
            dictionaryV = dictionaryConvert.dictionarySaveDTO2PO(dto);
            this.updateById(dictionaryV);

        }

        return dictionaryConvert.po2DictionaryEchoVO(dictionaryV);
    }

    private @NotNull List<Integer> getParentIdList() {
        LambdaQueryWrapper<Dictionary> qw = Wrappers.<Dictionary>lambdaQuery().eq(Dictionary::getParentId, DictionaryConstant.PARENT_DICTIONARY_ID);
        List<Integer> parentIdList = this.list(qw).stream().map(Dictionary::getId).collect(Collectors.toList());
        parentIdList.add(DictionaryConstant.PARENT_DICTIONARY_ID);
        return parentIdList;
    }
    // dto
    // po
    // vo
//    @Autowired
//    private RedisTemplate redisTemplate;

    @Cacheable(key = "#dictionaryId")
    @Override
    public DictionaryEchoVO getDictionaryById(Integer dictionaryId) { // 24
        Dictionary dictionary = BriupAssert.requireNotNull(this, Dictionary::getId, dictionaryId, ResultCodeEnum.DATA_NOT_EXIST);
        DictionaryEchoVO dictionaryEchoVO = dictionaryConvert.po2DictionaryEchoVO(dictionary);
        return dictionaryEchoVO;
    }

    @Transactional(rollbackFor = Exception.class) // 要删除redis的值和mysql的值
    @CacheEvict(key = "#dictionaryId") // 删除所有Dictonary开头的值
    @Override
    public void removeDictionaryById(Integer dictionaryId) { // 1
        // id必须存在
        BriupAssert.requireNotNull(this, Dictionary::getId, dictionaryId, ResultCodeEnum.DATA_NOT_EXIST);
        // 先删除二级字典,再删除一级字典[逻辑删除]
        // delete from dictionary where parentId = 1
        this.remove(Wrappers.<Dictionary>lambdaQuery().eq(Dictionary::getParentId, dictionaryId));
        // dellete from dictionary where id = 1
        this.removeById(dictionaryId);
    }

    @Override
    public PageVO<DictionaryPageVO> getDictionaryByPage(Long pageNum, Long pageSize) {
        PageVO<DictionaryPageVO> vo = new PageVO<>();
        // 先查询一级字典 parentId = 0
        IPage<Dictionary> page = new Page<>(pageNum,pageSize);
        page = this.page(page, Wrappers.
                <Dictionary>lambdaQuery()
                .eq(Dictionary::getParentId, DictionaryConstant.PARENT_DICTIONARY_ID)
        );
        // 每个一级分类 分别设置二级字典 parentId = 1 4 7[id]
        // select * from dictionary where parent = [1 4 7 21]
        List<DictionaryPageVO> list = dictionaryConvert
                .po2DictionaryPageVOList(page.getRecords())
                .stream()
                .peek(dictionaryPageVO -> { // peek类似foreach方法 消费每个元素,但是返回stream流
                    List<Dictionary> dictionaryList = this.list(Wrappers
                            .<Dictionary>lambdaQuery()
                            .eq(Dictionary::getParentId, dictionaryPageVO.getDictId()));
                    List<DictionaryPageVO> children = dictionaryConvert.po2DictionaryPageVOList(dictionaryList) ;
                    dictionaryPageVO.setChildren(children);
                }).toList();
        vo.setTotal(page.getTotal());
        // 此时只封装了一级分类
        vo.setData(list);  // Page<DictionaryPageVO>
        return vo;
    }
    /*
        查询数据集类型下的二级字典的信息
        Dictionary:Dropdown:#{dictCode}
     */
    @Cacheable(
            key = "T(com.briup.pai.common.constant.CommonConstant).DROPDOWN_CACHE_PREFIX + ':' + #dictCode",
            unless = "#result == null")
    @Override
    public List<DropDownVO> getDropDownList(String dictCode) {
        // 根据[dictCode 一级字典编码]查询二级字典数据
        Dictionary dictionary = BriupAssert.requireNotNull(this, Dictionary::getDictCode, dictCode, ResultCodeEnum.DATA_NOT_EXIST);
        LambdaQueryWrapper<Dictionary> qw = Wrappers.<Dictionary>lambdaQuery();
        qw.eq(Dictionary::getParentId, dictionary.getId());
        List<Dictionary> second = this.list(qw);
        // List<Dictionary> ->  List<DropDownVO>
        return dictionaryConvert.po2DictionaryDropDownVOList(second);
    }

    @Override
    public String getDictionaryValueById(Integer dictionaryId) {
        return "";
    }
}