package com.briup.pai.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.briup.pai.convert.DatasetConvert;
import com.briup.pai.dao.DatasetMapper;
import com.briup.pai.entity.dto.DatasetSaveDTO;
import com.briup.pai.entity.po.Dataset;
import com.briup.pai.entity.vo.*;
import com.briup.pai.service.IClassifyService;
import com.briup.pai.service.IDatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatasetServiceImpl extends ServiceImpl<DatasetMapper, Dataset> implements IDatasetService {

    @Autowired
    private DatasetConvert datasetConvert;
    @Autowired
    private IClassifyService classifyService;
    @Override
    public PageVO<DatasetPageVO> getDatasetByPageAndCondition(Long pageNum, Long pageSize, String datasetName, Integer datasetType) {
        Page<Dataset> page = new Page<>(pageNum, pageSize);
        Wrapper<Dataset> wrapper = Wrappers.<Dataset>lambdaQuery()
                .eq(StringUtils.isNotEmpty(datasetName),Dataset::getDatasetName, datasetName)
                .eq(ObjectUtil.isNotEmpty(datasetType),Dataset::getDatasetType,datasetType);
        page = this.page(page,wrapper);
        List<Dataset> records = page.getRecords();
        //还需要设置每个数据集的分类数量以及图片数量，这里涉及到另外两张表
        List<DatasetPageVO> datasetPageVOS = datasetConvert.po2DatasetPageVOList(records)
                .stream().peek(datasetPageVO -> {
                    List<ClassifyInDatasetVO> classifies = classifyService.getClassifiesByDatasetId(datasetPageVO.getDatasetId());
                    //设置每一个数据集的分类数量
                    datasetPageVO.setClassifyNum((long) classifies.size());
                    //设置每个数据集的分类下的图片总和
                    long total = 0;
                    for (ClassifyInDatasetVO classify : classifies) {
                        total += classify.getEntityNum();
                    }
                    datasetPageVO.setEntityNum(total);
                }).toList();
        PageVO<DatasetPageVO> pageVO = new PageVO<>();
        pageVO.setTotal(page.getTotal());
        pageVO.setData(datasetPageVOS);

        return pageVO;
    }

    @Override
    public DatasetEchoVO addOrModifyDataset(DatasetSaveDTO dto) {
        return null;
    }

    @Override
    public DatasetEchoVO modifyDatasetFeedback(Integer datasetId) {
        return null;
    }

    @Override
    public void removeDatasetById(Integer datasetId) {

    }

    @Override
    public DatasetDetailVO getDatasetDetail(Integer datasetId) {
        return null;
    }
}