package com.briup.pai.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.briup.pai.common.enums.DatasetStatusEnum;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.exception.BriupAssert;
import com.briup.pai.convert.DatasetConvert;
import com.briup.pai.dao.DatasetMapper;
import com.briup.pai.entity.dto.DatasetSaveDTO;
import com.briup.pai.entity.po.Dataset;
import com.briup.pai.entity.vo.*;
import com.briup.pai.service.IClassifyService;
import com.briup.pai.service.IDatasetService;
import com.briup.pai.service.IEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DatasetServiceImpl extends ServiceImpl<DatasetMapper, Dataset> implements IDatasetService {
    @Value("${upload.nginx-file-path}")
    private String nginxFilePath;
    @Autowired
    private DatasetConvert datasetConvert;
    @Autowired
    private IClassifyService classifyService;
    @Autowired
    private IEntityService entityService;
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
        Integer datasetId = dto.getDatasetId();
        Dataset dataset = null;
        if (ObjectUtil.isNull(datasetId)) {
            // 数据集名称必须唯一
            BriupAssert.requireNull(
                    this,
                    Dataset::getDatasetName,
                    dto.getDatasetName(),
                    ResultCodeEnum.DATA_ALREADY_EXIST
            );
            // 转换成po对象
            dataset = datasetConvert.datasetSaveDTO2Po(dto);
            // 数据集状态设置为初始化
            dataset.setDatasetStatus(DatasetStatusEnum.INIT.getStatus());
            // 保存
            this.save(dataset);
        } else {
            // 数据集必须存在
            Dataset temp = BriupAssert.requireNotNull(
                    this,
                    Dataset::getId,
                    datasetId,
                    ResultCodeEnum.DATA_NOT_EXIST
            );
            // 数据集名称必须唯一
            BriupAssert.requireNull(
                    this,
                    Dataset::getDatasetName,
                    dto.getDatasetName(),
                    Dataset::getId,
                    datasetId,
                    ResultCodeEnum.DATA_ALREADY_EXIST
            );
            // 数据集类型不能修改
            BriupAssert.requireEqual(
                    dto.getDatasetType(),
                    temp.getDatasetType(),
                    ResultCodeEnum.PARAM_IS_ERROR
            );
            // 修改
            dataset = datasetConvert.datasetSaveDTO2Po(dto);
            this.updateById(dataset);
        }
        return datasetConvert.po2DatasetEchoVO(dataset);
    }

    //回显数据集
    @Override
    public DatasetEchoVO modifyDatasetFeedback(Integer datasetId) {
        // 数据集必须存在
        Dataset dataset = BriupAssert.requireNotNull(
                this,
                Dataset::getId,
                datasetId,
                ResultCodeEnum.DATA_NOT_EXIST
        );
        // 转换VO返回
        return datasetConvert.po2DatasetEchoVO(dataset);
    }

    @Override
    public void removeDatasetById(Integer datasetId) {
        // todo 正在使用[训练 评估 优化]的数据集不能删除 学了模型之后再来处理
        //删除数据集分类以及分类下的图片
        BriupAssert.requireNotNull(this, Dataset::getId, datasetId, ResultCodeEnum.DATA_NOT_EXIST);
        List<ClassifyInDatasetVO> classifies = classifyService.getClassifiesByDatasetId(datasetId);
        int[] classifyIds = classifies.stream().mapToInt(ClassifyInDatasetVO::getClassifyId).toArray();
        System.out.println(Arrays.toString(classifyIds));
        for (int classifyId : classifyIds) {
            List<EntityInClassifyVO> entityInClassifyVOS = entityService.getEntityByClassifyId(classifyId);
            int[] entityIds = entityInClassifyVOS.stream().mapToInt(EntityInClassifyVO::getEntityId).toArray();
            entityService.removeBatchByIds(Arrays.asList(entityIds));

        }
        //删除分类
        classifyService.removeBatchByIds(Arrays.asList(classifyIds));
        //删除数据集
        this.removeById(datasetId);
        //删除数据集文件 E:/pai-file-nginx/html/1
        FileUtil.del(nginxFilePath+"/"+datasetId);
    }

    @Override
    public DatasetDetailVO getDatasetDetail(Integer datasetId) {
        Dataset dataset = BriupAssert.requireNotNull(this, Dataset::getId, datasetId, ResultCodeEnum.DATA_NOT_EXIST);
        DatasetDetailVO datasetDetailVO = datasetConvert.po2DatasetDetailVO(dataset);
        List<ClassifyInDatasetVO> classifies = classifyService.getClassifiesByDatasetId(datasetId);
        datasetDetailVO.setClassifies(classifies);
        datasetDetailVO.setClassifyNum((long) classifies.size());

        int total = 0;
        for (ClassifyInDatasetVO classify : classifies) {
            total += classify.getEntityNum();
        }
        datasetDetailVO.setEntityNum((long) total);
        return datasetDetailVO;
    }
}