package com.briup.pai.listener;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.briup.pai.common.enums.ModelStatusEnum;
import com.briup.pai.common.enums.TrainingStatusEnum;
import com.briup.pai.convert.EvaluateErrorConvert;
import com.briup.pai.convert.EvaluateLabelConvert;
import com.briup.pai.convert.ReleaseConvert;
import com.briup.pai.convert.TrainingLabelConvert;
import com.briup.pai.entity.message.*;
import com.briup.pai.entity.po.*;
import com.briup.pai.service.*;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author 86151
 * @program: Plant_Ai_Identify_Study
 * @description
 * @create 2026/01/15 15:51
 **/
@Component
public class ModelTrainRabbitListener {
    @Resource
    private ITrainingService trainingService;
    @Resource
    private IModelService modelService;
    @Resource
    private ITrainingLabelService trainingLabelService;
    @Resource
    private TrainingLabelConvert trainingLabelConvert;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue("${mq.init.result-queue}"),
                    exchange = @Exchange("${mq.init.exchange}"),
                    key = "${mq.init.result-routing-key}"
            )
    })
    @Transactional(rollbackFor = Exception.class)
    public void receiveInitTraining(String message){
        ModelTrainResultMessage modelTrainResultMessage = JSON.parseObject(message, ModelTrainResultMessage.class);
        processTrainingResult(modelTrainResultMessage);
    }

    // 监听优化结果
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue("${mq.optimize.result-queue}"),
                    exchange = @Exchange("${mq.optimize.exchange}"),
                    key = "${mq.optimize.result-routing-key}"
            )
    })
    @Transactional(rollbackFor = Exception.class)
    public void receiveOptimizingTraining(String message){
        ModelTrainResultMessage modelTrainResultMessage = JSON.parseObject(message, ModelTrainResultMessage.class);
        processTrainingResult(modelTrainResultMessage);
    }

    // 更新训练数据
    // 新增训练结果
    // 更新模型状态
    @Transactional(rollbackFor = Exception.class)
    public void processTrainingResult(ModelTrainResultMessage resultMessage) {
        // 训练失败
        Integer modelId = resultMessage.getModelId();
        Model model = modelService.getById(modelId);
        if (ObjectUtil.equal(resultMessage.getIsSuccess(), 1)) {
            model.setTrainingStatus(TrainingStatusEnum.ERROR.getStatus());
            modelService.updateById(model);
            return;
        }
        // 保存训练结果
        List<TrainingLabel> trainingLabels = resultMessage.getLabelResults()
                .stream()
                .map(labelResultDTO -> {
                    TrainingLabel trainingLabel = trainingLabelConvert.labelResultMessage2Po(labelResultDTO);
                    trainingLabel.setTrainingId(resultMessage.getTrainingId());
                    return trainingLabel;
                })
                .toList();
        trainingLabelService.saveBatch(trainingLabels);
        // 修改训练准确率、模型地址
        Training training = trainingService.getById(resultMessage.getTrainingId());
        training.setAccuracyRate(resultMessage.getAccuracyRate());
        training.setModelFileAddr(resultMessage.getModelFileAddr());
        trainingService.updateById(training);
        // 修改模型状态、版本
        model.setLastModelVersion(model.getLastModelVersion() + 1);
        model.setAccuracyRate(resultMessage.getAccuracyRate());
        model.setTrainingStatus(TrainingStatusEnum.DONE.getStatus());
        modelService.updateById(model);
    }






    @Resource
    private IEvaluateService evaluateService;
    @Resource
    private IEvaluateLabelService evaluateLabelService;
    @Resource
    private IEvaluateErrorService evaluateErrorService;

    @Resource
    private EvaluateLabelConvert evaluateLabelConvert;
    @Resource
    private EvaluateErrorConvert evaluateErrorConvert;


    // 监听评估结果
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue("${mq.evaluate.result-queue}"),
                    exchange = @Exchange("${mq.evaluate.exchange}"),
                    key = "${mq.evaluate.result-routing-key}"
            )
    })
    @Transactional(rollbackFor = Exception.class)
    public void receiveEvaluate(String message){
        ModelEvaluateResultMessage modelEvaluateResultMessage = JSON.parseObject(message, ModelEvaluateResultMessage.class);
        Double accuracyRate = modelEvaluateResultMessage.getAccuracyRate();
        List<LabelResultMessage> labelResults = modelEvaluateResultMessage.getLabelResults();
        Integer modelId = modelEvaluateResultMessage.getModelId();
        Integer evaluateId = modelEvaluateResultMessage.getEvaluateId();
        List<EvaluateErrorMessage> evaluateErrors = modelEvaluateResultMessage.getEvaluateErrors();
        Evaluate evaluate = evaluateService.getById(evaluateId);
        evaluate.setAccuracyRate(accuracyRate);
        evaluateService.updateById(evaluate);
        // 添加评估指标
        List<EvaluateLabel> evaluateLabelList = labelResults.stream().map(labelResultMessage -> {
            EvaluateLabel evaluateLabel = evaluateLabelConvert.labelResultMessage2Po(labelResultMessage);
            evaluateLabel.setEvaluateId(evaluateId);
            return evaluateLabel;
        }).toList();
        evaluateLabelService.saveBatch(evaluateLabelList);
        // 添加评估错误
        List<EvaluateError> evaluateErrorList = evaluateErrors.stream().map(evaluateErrorMessage -> {
            EvaluateError evaluateError = evaluateErrorConvert.evaluateErrorMessage2Po(evaluateErrorMessage);
            evaluateError.setEvaluateId(evaluateId);
            return evaluateError;
        }).toList();
        evaluateErrorService.saveBatch(evaluateErrorList);
        // 更新模型的版本 准确率
        Model model = modelService.getById(modelId);
        model.setAccuracyRate(accuracyRate);
        model.setTrainingStatus(TrainingStatusEnum.DONE.getStatus());
        modelService.updateById(model);
    }




    @Resource
    private IReleaseService releaseService;
    @Resource
    private ReleaseConvert releaseConvert;

    // 监听发布结果
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue("${mq.release.result-queue}"),
                    exchange = @Exchange("${mq.release.exchange}"),
                    key = "${mq.release.result-routing-key}"
            )
    })
    @Transactional(rollbackFor = Exception.class)
    public void receiveRelese(String message){
        System.out.println("模型发布......");
        ModelReleaseResultMessage resultMessage = JSON.parseObject(message, ModelReleaseResultMessage.class);
        if (ObjectUtil.equal(resultMessage.getModelStatus(), ModelStatusEnum.Published.getStatus())) {
            // 保存模型发布记录
            Release release = releaseConvert.modelReleaseResultMessage2Po(resultMessage);
            release.setCreateTime(new Date());
            releaseService.save(release);
        } else {
            // 删除模型发布记录
            LambdaQueryWrapper<Release> releaseWrapper = new LambdaQueryWrapper<>();
            releaseWrapper.eq(Release::getModelId, resultMessage.getModelId());
            releaseService.remove(releaseWrapper);
        }
        // 修改模型的状态
        Model model = modelService.getById(resultMessage.getModelId());
        model.setModelStatus(resultMessage.getModelStatus());
        modelService.updateById(model);
    }
}
