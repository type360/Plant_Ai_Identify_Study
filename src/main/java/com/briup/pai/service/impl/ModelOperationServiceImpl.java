package com.briup.pai.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.briup.pai.common.constant.CommonConstant;
import com.briup.pai.common.constant.ModelConstant;
import com.briup.pai.common.enums.DatasetStatusEnum;
import com.briup.pai.common.enums.ModelOperationTypeEnum;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.enums.TrainingStatusEnum;
import com.briup.pai.common.exception.BriupAssert;
import com.briup.pai.common.utils.SecurityUtil;
import com.briup.pai.convert.DatasetConvert;
import com.briup.pai.convert.ModelConfigConvert;
import com.briup.pai.convert.ModelConvert;
import com.briup.pai.convert.ReleaseConvert;
import com.briup.pai.entity.dto.ModelOperationDTO;
import com.briup.pai.entity.message.ModelEvaluateMessage;
import com.briup.pai.entity.message.ModelInitTrainMessage;
import com.briup.pai.entity.message.ModelOptimizeTrainMessage;
import com.briup.pai.entity.message.ModelReleaseMessage;
import com.briup.pai.entity.po.*;
import com.briup.pai.entity.vo.ClassifyInDatasetVO;
import com.briup.pai.entity.vo.EntityInClassifyVO;
import com.briup.pai.entity.vo.ReleaseModelVO;
import com.briup.pai.entity.vo.TrainingDatasetQueryVO;
import com.briup.pai.service.*;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class ModelOperationServiceImpl implements IModelOperationService {
    @Resource
    private IModelService modelService;
    @Resource
    private IDatasetService datasetService;
    @Resource
    private IModelConfigService modelConfigService;

    @Value("${upload.nginx-file-path}")
    private String nginxFilePath;
    @Value("${mq.init.exchange}")
    private String initExchange;
    @Value("${mq.init.send-routing-key}")
    private String initSendRoutingKey;
    @Value("${mq.optimize.exchange}")
    private String optimizeExchange;
    @Value("${mq.optimize.send-routing-key}")
    private String optimizeSendRoutingKey;
    @Value("${mq.evaluate.exchange}")
    private String evaluateExchange;
    @Value("${mq.evaluate.send-routing-key}")
    private String evaluateSendRoutingKey;
    @Value("${mq.release.exchange}")
    private String releaseExchange;
    @Value("${mq.release.send-routing-key}")
    private String releaseSendRoutingKey;

    @Resource
    private IClassifyService classifyService;
    @Resource
    private IEntityService entityService;
    @Resource
    private ITrainingService trainingService;
    @Resource
    private ITrainingDatasetService trainingDatasetService;

    @Resource
    private ModelConvert modelConvert;
    @Resource
    private ModelConfigConvert modelConfigConvert;

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private DatasetConvert datasetConvert;
    @Autowired
    private EvaluateServiceImpl evaluateServiceImpl;
    @Autowired
    private ReleaseServiceImpl releaseServiceImpl;
    @Autowired
    private ReleaseConvert releaseConvert;

    @Override
    public List<TrainingDatasetQueryVO> getTrainingDatasets(Integer modelId, Integer datasetUsage) {
        // 模型必须存在
        Model model = BriupAssert.requireNotNull(
                modelService,
                Model::getId,
                modelId,
                ResultCodeEnum.DATA_NOT_EXIST
        );
        // 查询所有已上传图片并且类型和模型一致的数据集
        LambdaQueryWrapper<Dataset> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(Dataset::getDatasetStatus, DatasetStatusEnum.DONE.getStatus())
                .eq(Dataset::getDatasetType, model.getModelType())
                .eq(Dataset::getDatasetUsage, datasetUsage);
        List<Dataset> datasetList = datasetService.list(wrapper);
        // 转换为训练数据集查询对象
        return datasetConvert.po2TrainingDatasetQueryVOList(datasetList);
    }

    /**
     * 优化训练时 选中初始化新增的模型配置
     * @param modelId
     * @return
     */
    @Override
    public ModelConfig getModelConfig(Integer modelId) {
        // 模型必须存在
        Model model = BriupAssert.requireNotNull(
                modelService,
                Model::getId,
                modelId,
                ResultCodeEnum.DATA_NOT_EXIST
        );
        // 模型训练状态必须为完成
        BriupAssert.requireEqual(
                model.getTrainingStatus(),
                TrainingStatusEnum.DONE.getStatus(),
                ResultCodeEnum.PARAM_IS_ERROR
        );
        // 查询模型配置信息
        LambdaQueryWrapper<ModelConfig> modelConfigWrapper = new LambdaQueryWrapper<>();
        modelConfigWrapper.eq(ModelConfig::getModelId, modelId);
        // 返回数据
        return modelConfigService.getOne(modelConfigWrapper);
    }

    /**
     * 模型训练
     * @param modelOperationDTO
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operationModel(ModelOperationDTO modelOperationDTO) {
        Integer modelId = modelOperationDTO.getModelId();
        Integer operationType = modelOperationDTO.getOperationType();
        List<Integer> datasetIds = modelOperationDTO.getDatasetIds();
        // 模型必须存在
        Model model = BriupAssert.requireNotNull(
                modelService,
                Model::getId,
                modelId,
                ResultCodeEnum.DATA_NOT_EXIST
        );
        Integer lastModelVersion = model.getLastModelVersion(); // 初始化是0
        LambdaQueryWrapper<Training> wrapper = Wrappers.<Training>lambdaQuery().eq(Training::getModelId, modelId)
                .eq(Training::getModelVersion, lastModelVersion);
        Training training = trainingService.getOne(wrapper);

        if(Objects.equals(operationType, ModelOperationTypeEnum.INIT.getType())){  // 初始化训练
            // 模型是未训练状态
            BriupAssert.requireEqual(model.getModelStatus(),TrainingStatusEnum.NO_TRAINING.getStatus(), ResultCodeEnum.PARAM_IS_ERROR);
            // 模型没有训练记录 model:1 -> training:n 根据模型id和模型版本定义唯一的traning
            BriupAssert.requireNull(training,ResultCodeEnum.DATA_ALREADY_EXIST);
            // 保存模型配置
            ModelConfig modelConfig = modelConfigConvert.modelOperationDTO2Po(modelOperationDTO);
            modelConfigService.save(modelConfig);
            // 保存训练信息 保存训练所使用的数据集
            int trainingId = this.saveTrainingAndDataset(modelId,lastModelVersion,datasetIds);
            // 取出数据集的分类和图片 并且合并 key是分类名称,value是所有图片的路径
            Map<String, List<String>> data = mergeDataset(datasetIds);
            // 数据和算子信息封装发给rabbitmq
            ModelInitTrainMessage modelInitTrainMessage = modelConvert.modelOperationDTO2InitMessage(modelOperationDTO);
            modelInitTrainMessage.setModelVersion(lastModelVersion+1);
            modelInitTrainMessage.setTrainingId(trainingId);
            modelInitTrainMessage.setModelId(modelId);
            modelInitTrainMessage.setData(data);
            // 发送数据后要监听数据的返回
            rabbitTemplate.convertAndSend(initExchange,initSendRoutingKey, JSON.toJSONString(modelInitTrainMessage));
            // 修改模型状态为训练中
            model.setTrainingStatus(TrainingStatusEnum.TRAINING.getStatus());
            modelService.updateById(model);
        }else if(Objects.equals(operationType, ModelOperationTypeEnum.OPTIMIZE.getType())){  // 优化训练
            // 模型必须处于训练完成状态
            BriupAssert.requireEqual(model.getTrainingStatus(), TrainingStatusEnum.DONE.getStatus(), ResultCodeEnum.PARAM_IS_ERROR);
            // 保存训练信息 保存训练所使用的数据集
            int trainingId = this.saveTrainingAndDataset(modelId,lastModelVersion,datasetIds);
            // 取出数据集的分类和图片 并且合并 key是分类名称,value是所有图片的路径
            Map<String, List<String>> data = mergeDataset(datasetIds);
            // 数据和算子信息封装发给rabbitmq
            ModelOptimizeTrainMessage modelOptimizeTrainMessage = modelConvert.modelOperationDTO2OptimizeMessage(modelOperationDTO);
            modelOptimizeTrainMessage.setOldModelPath(training.getModelFileAddr());
            modelOptimizeTrainMessage.setModelVersion(lastModelVersion+1);
            modelOptimizeTrainMessage.setTrainingId(trainingId);
            modelOptimizeTrainMessage.setModelId(modelId);
            modelOptimizeTrainMessage.setData(data);
            rabbitTemplate.convertAndSend(optimizeExchange,optimizeSendRoutingKey, JSON.toJSONString(modelOptimizeTrainMessage));
            // 跟新训练状态
            model.setTrainingStatus(TrainingStatusEnum.OPTIMIZING.getStatus());
            modelService.updateById(model);

        }else if(Objects.equals(operationType, ModelOperationTypeEnum.EVALUATE.getType())){  // 评估
            // 模型训练状态必须为完成
            BriupAssert.requireEqual(model.getTrainingStatus(), TrainingStatusEnum.DONE.getStatus(), ResultCodeEnum.PARAM_IS_ERROR);
            // 一次评估只能选择一个数据集 不能使用相同的数据集进行评估 [评估最新训练信息]
            Integer currentDatasetId = datasetIds.get(0);
            Integer trainingId = training.getId();
            LambdaQueryWrapper<Evaluate> wrapper2 = Wrappers.<Evaluate>lambdaQuery().eq(Evaluate::getTrainingId, trainingId);
            // 旧的数据集id
            List<Integer> oldDatasetIds = evaluateServiceImpl.list(wrapper2).stream().map(Evaluate::getDatasetId).toList();
            BriupAssert.requireNotIn(currentDatasetId,oldDatasetIds,ResultCodeEnum.MODEL_CAN_NOT_EVALUATE);
            // 保存评估相关记录
            Evaluate evaluate = new Evaluate();
            evaluate.setTrainingId(trainingId);
            evaluate.setDatasetId(currentDatasetId);
//            evaluate.setAccuracyRate(); 等python给出结果
            evaluateServiceImpl.save(evaluate);
            // 取出数据集的分类和图片 并且合并 key是分类名称,value是所有图片的路径
            Map<String, List<String>> data = mergeDataset(datasetIds);
            // 发送到消息队列的消息对应的实体类为：`ModelEvaluateMessage`
            ModelEvaluateMessage modelEvaluateMessage = new ModelEvaluateMessage();
            modelEvaluateMessage.setEvaluateId(evaluate.getId());
            modelEvaluateMessage.setModelFileAddr(training.getModelFileAddr());
            modelEvaluateMessage.setModelId(modelId);
            modelEvaluateMessage.setData(data);
            rabbitTemplate.convertAndSend(evaluateExchange,evaluateSendRoutingKey,JSON.toJSONString(modelEvaluateMessage));
            // 修改模型状态
            model.setTrainingStatus(TrainingStatusEnum.EVALUATING.getStatus());
            modelService.updateById(model);
        }
    }
    // 合并数据集 key为分类名字,value为图片的路径
    private Map<String, List<String>> mergeDataset(List<Integer> datasetIds) {
        Map<String, List<String>> data = new HashMap<>();
        datasetIds.forEach(datasetId -> {
            // 取出每个数据集下的分类
            List<ClassifyInDatasetVO> classifies = classifyService.getClassifiesByDatasetId(datasetId);
            // 取出每个分类下的图片
            classifies.forEach(classify -> {
                String classifyName = classify.getClassifyName();
                List<String> entityList = entityService.getEntityByClassifyId(classify.getClassifyId())
                        .stream().peek(entityInClassifyVO -> {
                            entityInClassifyVO.setEntityUrl(CommonConstant.createEntityPath(this.nginxFilePath, datasetId, classifyName, entityInClassifyVO.getEntityUrl()));
                        })
                        .map(EntityInClassifyVO::getEntityUrl).toList();
                // 存的是图片的id
                if(data.containsKey(classifyName)){
                    data.get(classifyName).addAll(entityList);
                }else{
                    data.put(classifyName,entityList);
                }
            });
        });

        return data;
    }

    // 保存训练信息和训练所用数据集
    @Transactional(rollbackFor = Exception.class)
    public int saveTrainingAndDataset(Integer modelId, Integer lastModelVersion, List<Integer> datasetIds) {
        // 保存训练信息
        Training training = new Training();
        training.setModelId(modelId);
//        training.setModelFileAddr(); // python给出的结果
//        training.setAccuracyRate(); // python给出的结果
        training.setModelVersion(lastModelVersion+1);
        training.setCreateBy(SecurityUtil.getUserId());
        training.setCreateTime(new Date());
        trainingService.save(training);
        List<TrainingDataset> trainingDatasetList = datasetIds.stream().map(id -> {
            TrainingDataset trainingDataset = new TrainingDataset();
            trainingDataset.setTrainingId(training.getId());
            trainingDataset.setDatasetId(id);

            return trainingDataset;
        }).toList();
        trainingDatasetService.saveBatch(trainingDatasetList);

        return training.getId();
    }

    /**
     * @param modelId
     * @param modelStatus 0 发布 1取消
     */
    @Override
    public void releaseModelOrNot(Integer modelId, Integer modelStatus) {
        // 模型必须存在
        Model model = BriupAssert.requireNotNull(modelService, Model::getId, modelId, ResultCodeEnum.DATA_NOT_EXIST);
        // 状态一致
        BriupAssert.requireEqual(model.getModelStatus(), modelStatus,ResultCodeEnum.DATASET_STATUS_ERROR);
        // 模型的准确率不能低于阈值95%
        BriupAssert.requireTrue(model.getAccuracyRate() > ModelConstant.MODEL_RELEASE_THRESHOLD, ResultCodeEnum.MODEL_CAN_NOT_RELEASE);
        // 将数据发布给rabbimq ModelReleaseMessage
        // 查找最新的训练信息 根据模型以及版本查找
        LambdaQueryWrapper<Training> wrapper = Wrappers.<Training>lambdaQuery().eq(Training::getModelId, modelId)
                .eq(Training::getModelVersion, model.getLastModelVersion());
        Training training = trainingService.getOne(wrapper);
        ModelReleaseMessage modelReleaseMessage = new ModelReleaseMessage();
        modelReleaseMessage.setUserId(SecurityUtil.getUserId());
        modelReleaseMessage.setModelId(modelId);
        modelReleaseMessage.setModelStatus(modelStatus);
        modelReleaseMessage.setModelFileAddr(training.getModelFileAddr());

        rabbitTemplate.convertAndSend(releaseExchange,releaseSendRoutingKey,JSON.toJSONString(modelReleaseMessage));
    }

    @Override
    public List<ReleaseModelVO> getReleaseModel() {
        return releaseServiceImpl.list().stream().map(releaseModelVO -> {
            Model model = modelService.getById(releaseModelVO.getModelId());
            return modelConvert.po2ReleaseModelVO(model);
        }).toList();
    }

    /**
     * 通过Http远程调用方案[RestTemplate] 调用python的识别接口
     * @param modelId
     * @param file
     * @return
     */
    @Resource
    private RestTemplate restTemplate;
    @SneakyThrows
    @Override
    public String identify(Integer modelId, MultipartFile file) {
        Release release = releaseServiceImpl.getOne(Wrappers.<Release>lambdaQuery().eq(Release::getModelId, modelId));
        // 请求路径
        String url = release.getModelUrl();
        // 请求头 请求格式
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        // 设置请求参数 [请求体数据是文件信息]
        MultiValueMap<String, Object> fileMap = new LinkedMultiValueMap<>();
        fileMap.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(fileMap, httpHeaders);

        ResponseEntity<String> response = restTemplate.postForEntity(url, httpEntity, String.class);
        return response.getBody();
    }
}