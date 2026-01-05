package com.briup.pai.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.briup.pai.common.constant.OperatorConstant;
import com.briup.pai.common.enums.OperatorCategoryEnum;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.exception.BriupAssert;
import com.briup.pai.common.exception.CustomException;
import com.briup.pai.convert.OperatorConvert;
import com.briup.pai.dao.OperatorMapper;
import com.briup.pai.entity.dto.OperatorImportDTO;
import com.briup.pai.entity.dto.OperatorUpdateDTO;
import com.briup.pai.entity.po.Operator;
import com.briup.pai.entity.vo.DropDownVO;
import com.briup.pai.entity.vo.OperatorEchoVO;
import com.briup.pai.entity.vo.OperatorPageVO;
import com.briup.pai.entity.vo.PageVO;
import com.briup.pai.service.IOperatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CacheConfig(cacheNames = OperatorConstant.OPERATOR_CACHE_PREFIX)
@Service
public class OperatorServiceImpl extends ServiceImpl<OperatorMapper, Operator> implements IOperatorService {
    @Autowired
    private OperatorConvert operatorConvert;

    @Cacheable(key = "T(com.briup.pai.common.constant.CommonConstant).DROPDOWN_CACHE_PREFIX")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importOperator(MultipartFile file) {
        // 校验文件
        BriupAssert.requireExcel(file);
        // 数据库存在的算子
        List<Operator> dbList = this.list();
        List<String> nameList = dbList.stream().map(Operator::getOperatorName).toList();
        List<OperatorImportDTO> newList = new ArrayList<>();
        // 数据库存在的算子忽略掉
        try {
            List<OperatorImportDTO> list = EasyExcel.read(file.getInputStream())
                    .head(OperatorImportDTO.class)
                    .doReadAllSync();
            for (int i = 0; i < list.size(); i++) {
                // 读到3条数据
                OperatorImportDTO operatorImportDTO = list.get(i);
                if (!nameList.contains(operatorImportDTO.getOperatorName())) {
                    newList.add(operatorImportDTO);
                }
            }

            List<Operator> finalList = operatorConvert.operatorImportDto2PoList(newList)
                    .stream()
                    .filter(operator -> operator.getOperatorType() != -1 && operator.getOperatorCategory() != -1)
                    .toList();
            // importOperator(事务) -> mybatisplus.saveBatch(事务)
            this.saveBatch(finalList);
        } catch (IOException e) {
            throw new CustomException(ResultCodeEnum.FILE_IMPORT_ERROR);
        }

    }

    @Override
    public PageVO<OperatorPageVO> getOperatorByPageAndCondition(Long pageNum, Long pageSize, Integer operatorType, Integer operatorCategory) {
        IPage<Operator> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<Operator> wrapper = Wrappers.<Operator>lambdaQuery()
                .eq(operatorType!=-1,Operator::getOperatorType, operatorType)
                .eq(operatorCategory!=-1,Operator::getOperatorCategory, operatorCategory);
        page = this.page(page,wrapper);

        PageVO<OperatorPageVO> pageVO = new PageVO<>();
        List<OperatorPageVO> list = operatorConvert.po2OperatorPageVOList(page.getRecords());
        pageVO.setData(list);
        pageVO.setTotal(page.getTotal());
        return pageVO;
    }

    @Cacheable(key = "#operatorId")
    @Override
    public OperatorEchoVO getOperatorById(Integer operatorId) {
        Operator operator = BriupAssert.requireNotNull(this, Operator::getId, operatorId, ResultCodeEnum.DATA_NOT_EXIST);
        return operatorConvert.po2OperatorEchoVO(operator);
    }

    @CachePut(key = "result.operatorId")
    @CacheEvict(key = "T(com.briup.pai.common.constant.CommonConstant).DROPDOWN_CACHE_PREFIX")
    @Override
    public OperatorEchoVO modifyOperatorById(OperatorUpdateDTO dto) {
        // 算子存在
        // 算子必须存在
        Operator operatorI = BriupAssert.requireNotNull(
                this,
                Operator::getId,
                dto.getOperatorId(),
                ResultCodeEnum.DATA_NOT_EXIST
        );
        // 算子名称不能重复 可以不修改旧的名字
        String operatorName = dto.getOperatorName();
        LambdaQueryWrapper<Operator> wrapper = Wrappers
                .<Operator>lambdaQuery().eq(Operator::getOperatorName, operatorName);
        Operator operatorN  = this.getOne(wrapper);
        // operatorN != null 必须修改了名字并且重复了
        if(operatorN != null && !operatorN.getOperatorName().equals(operatorI.getOperatorName())){ // 名字重复了
            throw new CustomException(ResultCodeEnum.DATA_ALREADY_EXIST);
        }
//        BriupAssert.requireNull(
//                this,
//                Operator::getOperatorName,
//                dto.getOperatorName(), Operator::getId,
//                dto.getOperatorId(),
//                ResultCodeEnum.DATA_ALREADY_EXIST
//        );
        this.updateById(operatorConvert.operatorUpdateDTO2po(dto));
        return  operatorConvert.po2OperatorEchoVO(operatorConvert.operatorUpdateDTO2po(dto));
    }

    @Caching(evict = {
            @CacheEvict(key = "#operatorId"),
            @CacheEvict(key = "T(com.briup.pai.common.constant.CommonConstant).DROPDOWN_CACHE_PREFIX")
    })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeOperatorById(Integer operatorId) {
        //这里是逻辑删除，在数据库中改为1
        BriupAssert.requireNotNull(this, Operator::getId, operatorId, ResultCodeEnum.DATA_NOT_EXIST);
        this.removeById(operatorId);

    }

    @CacheEvict(allEntries = true)//删除算子所有缓存
    @Override
    public void removeOperatorByIds(List<Integer> ids) {
        //逻辑删除
        this.removeBatchByIds(ids);

    }

    @Cacheable(key = "T(com.briup.pai.common.constant.CommonConstant).DROPDOWN_CACHE_PREFIX")
    @Override
    public Map<Integer, List<DropDownVO>> getOperatorDropDownList() {
        Map<Integer,List<DropDownVO>> map = new HashMap<>();
        OperatorCategoryEnum.categoryList()
                .stream()
                .forEach(categoryId -> {
                    List<Operator> list = this.list(Wrappers.<Operator>lambdaQuery().eq(Operator::getOperatorCategory, categoryId));
                    map.put(categoryId,operatorConvert.po2DropDownList(list));
                });
        return map;
    }
}