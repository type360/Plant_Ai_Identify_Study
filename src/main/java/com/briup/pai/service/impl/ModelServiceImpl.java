package com.briup.pai.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.briup.pai.common.constant.ModelConstant;
import com.briup.pai.common.enums.ModelStatusEnum;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.enums.TrainingStatusEnum;
import com.briup.pai.common.exception.BriupAssert;
import com.briup.pai.common.utils.SecurityUtil;
import com.briup.pai.convert.*;
import com.briup.pai.dao.ModelMapper;
import com.briup.pai.entity.dto.ModelSaveDTO;
import com.briup.pai.entity.po.*;
import com.briup.pai.entity.vo.*;
import com.briup.pai.service.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelServiceImpl extends ServiceImpl<ModelMapper, Model> implements IModelService {
    @Resource
    private ModelConvert modelConvert;

    @Override
    public PageVO<ModelPageVO> getModelByPageAndCondition(Integer pageNum, Integer modelType) {
        // 分页查询
        Page<Model> page = new Page<>(pageNum, ModelConstant.MODEL_PAGE_SIZE);
        LambdaQueryWrapper<Model> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(ObjectUtil.isNotEmpty(modelType), Model::getModelType, modelType)
                .orderByDesc(Model::getCreateTime);
        Page<Model> modelPage = this.page(page, wrapper);
        // 封装PageVO
        PageVO<ModelPageVO> pageVO = new PageVO<>();
        pageVO.setTotal(modelPage.getTotal());
        pageVO.setData(modelConvert.po2ModelPageVOList(modelPage.getRecords()));
        return pageVO;
    }

    @Override
    public ModelEchoVO getModelById(Integer modelId) {
        // 判断模型是否存在
        Model model = BriupAssert.requireNotNull(
                this,
                Model::getId,
                modelId,
                ResultCodeEnum.DATA_NOT_EXIST
        );
        // 对象转换并返回
        return modelConvert.po2ModelEchoVO(model);
    }

    @Override
    public ModelEchoVO addAndModifyModel(ModelSaveDTO modelSaveDTO) {
        Integer modelId = modelSaveDTO.getModelId();
        Model model = null;
        if (ObjectUtil.isEmpty(modelId)) {
            // 模型名称不能重复
            BriupAssert.requireNull(
                    this,
                    Model::getModelName,
                    modelSaveDTO.getModelName(),
                    ResultCodeEnum.DATA_ALREADY_EXIST
            );
            // 实体转换并保存
            model = modelConvert.modelSaveDTO2Po(modelSaveDTO);
            model.setModelStatus(ModelStatusEnum.Unpublished.getStatus()); // 状态未发布
            model.setLastModelVersion(0); // 训练版本 默认值为0
            model.setTrainingStatus(TrainingStatusEnum.NO_TRAINING.getStatus()); // 未训练
            model.setAccuracyRate(0.0); // 精确率为0.0
            model.setCreateBy(SecurityUtil.getUserId());
            this.save(model);
        } else {
            // 模型必须存在
            Model temp = BriupAssert.requireNotNull(
                    this,
                    Model::getId,
                    modelSaveDTO.getModelId(),
                    ResultCodeEnum.DATA_NOT_EXIST
            );
            // 模型名称不能重复
            BriupAssert.requireNull(
                    this,
                    Model::getModelName,
                    modelSaveDTO.getModelName(),
                    Model::getId, modelSaveDTO.getModelId(),
                    ResultCodeEnum.DATA_ALREADY_EXIST
            );
            // 模型类型不能修改
            BriupAssert.requireEqual(
                    temp.getModelType(),
                    modelSaveDTO.getModelType(),
                    ResultCodeEnum.PARAM_IS_ERROR
            );
            // 实体转换并修改
            model = modelConvert.modelSaveDTO2Po(modelSaveDTO);
            this.updateById(model);
        }
        return modelConvert.po2ModelEchoVO(model);
    }

    @Override
    public void removeModelById(Integer modelId) {
        // 模型必须存在
        Model model = BriupAssert.requireNotNull(
                this,
                Model::getId,
                modelId,
                ResultCodeEnum.DATA_NOT_EXIST
        );
        // 只有未训练的模型可以删除
        BriupAssert.requireEqual(
                model.getTrainingStatus(),
                TrainingStatusEnum.NO_TRAINING.getStatus(),
                ResultCodeEnum.DATA_CAN_NOT_DELETE
        );
        // 删除模型
        this.removeById(modelId);
    }

    @Resource
    private ITrainingService trainingService;
    @Resource
    private ITrainingDatasetService trainingDatasetService;
    @Resource
    private IDatasetService datasetService;
    @Resource
    private ITrainingLabelService trainingLabelService;
    @Resource
    private IEvaluateService evaluateService;
    @Resource
    private IEvaluateErrorService evaluateErrorService;
    @Resource
    private IEvaluateLabelService evaluateLabelService;
    @Resource
    private TrainingLabelConvert trainingLabelConvert;
    @Resource
    private EvaluateLabelConvert evaluateLabelConvert;
    @Resource
    private EvaluateErrorConvert evaluateErrorConvert;

    @Override
    public ModelDetailVO getModelDetailById(Integer modelId) {
        // 模型必须存在
        Model model = BriupAssert.requireNotNull(this, Model::getId, modelId, ResultCodeEnum.DATA_NOT_EXIST);
        // 封装模型信息
        ModelDetailVO modelDetailVO = modelConvert.po2ModelDetailVO(model);
        // 封装模型 训练历史
        // 根据模型查询训练历史 1:n
        LambdaQueryWrapper<Training> eq = Wrappers.<Training>lambdaQuery().eq(Training::getModelId, modelId);
        List<Training> list = trainingService.list(eq);
        List<ModelHistoryVO> trainintHistroy = list.stream().map(training -> {
            ModelHistoryVO modelHistoryVO = new ModelHistoryVO();
            modelHistoryVO.setModelVersion(training.getModelVersion());
            // 根据训练id -> training_dataset -> 数据id [1,2]
            List<Integer> datasetIds = trainingDatasetService.list(
                    Wrappers.<TrainingDataset>lambdaQuery().eq(TrainingDataset::getTrainingId, training.getId())
            ).stream().map(TrainingDataset::getDatasetId).toList();
            List<String> datasetNameList = datasetService.listByIds(datasetIds).stream().map(Dataset::getDatasetName).toList();
            modelHistoryVO.setDatasetNames(datasetNameList);
            modelHistoryVO.setAccuracyRate(training.getAccuracyRate());
            modelHistoryVO.setTrainDate(training.getCreateTime());
            // 训练结果指标 1个训练有多个指标 TODO 训练指标中有个标签名称未处理
            List<ModelOperationResultVO> trainResults = trainingLabelService.list(
                    Wrappers.<TrainingLabel>lambdaQuery().eq(TrainingLabel::getTrainingId, training.getId())
            ).stream().map(trainingLabel -> trainingLabelConvert.po2ModelOperationResultVO(trainingLabel)).toList();
            modelHistoryVO.setTrainResults(trainResults);
            // 评估结果
            List<Evaluate> evaluateList = evaluateService.list(Wrappers.<Evaluate>lambdaQuery().eq(Evaluate::getTrainingId, training.getId()));
            List<ModelEvaluateReportVO> evaluateReportList = evaluateList.stream().map(evaluate -> {
                ModelEvaluateReportVO modelEvaluateReportVO = new ModelEvaluateReportVO();
                // 评估只能用一个数据集
                modelEvaluateReportVO.setDatasetName(datasetService.getById(evaluate.getDatasetId()).getDatasetName());
                // 评估准确率
                modelEvaluateReportVO.setAccuracyRate(evaluate.getAccuracyRate());
                // 评估错误数量
                long errorCount = evaluateErrorService.count(Wrappers.<EvaluateError>lambdaQuery().eq(EvaluateError::getEvaluateId, evaluate.getId()));
                modelEvaluateReportVO.setErrorCount(errorCount);
                // 评估结果
                List<ModelOperationResultVO> modelOperationResultVOList = evaluateLabelService.list(Wrappers.<EvaluateLabel>lambdaQuery().eq(EvaluateLabel::getEvaluateId, evaluate.getId()))
                        .stream().map(evaluateLabel -> evaluateLabelConvert.po2ModelOperationResultVO(evaluateLabel)).toList();
                modelEvaluateReportVO.setEvaluateResults(modelOperationResultVOList);
                return modelEvaluateReportVO;
            }).toList();
            modelHistoryVO.setEvaluateReport(evaluateReportList);
            return modelHistoryVO;
        }).toList();
        modelDetailVO.setTrainingHistory(trainintHistroy);
        return modelDetailVO;
    }

    @Override
    public List<Integer> getDatasetIdsUsed() {
        return List.of();
    }
}