package com.briup.pai.convert;

import com.briup.pai.entity.dto.ModelOperationDTO;
import com.briup.pai.entity.dto.ModelSaveDTO;
import com.briup.pai.entity.message.ModelInitTrainMessage;
import com.briup.pai.entity.message.ModelOptimizeTrainMessage;
import com.briup.pai.entity.po.Model;
import com.briup.pai.entity.vo.ModelDetailVO;
import com.briup.pai.entity.vo.ModelEchoVO;
import com.briup.pai.entity.vo.ModelPageVO;
import com.briup.pai.entity.vo.ReleaseModelVO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-16T14:38:30+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.8 (Oracle Corporation)"
)
@Component
public class ModelConvertImpl implements ModelConvert {

    @Override
    public ModelPageVO po2ModelPageVO(Model model) {
        if ( model == null ) {
            return null;
        }

        ModelPageVO modelPageVO = new ModelPageVO();

        modelPageVO.setModelId( model.getId() );
        modelPageVO.setModelStatus( getModelStatus( model.getModelStatus() ) );
        modelPageVO.setTrainingStatus( getTrainingStatus( model.getTrainingStatus() ) );
        modelPageVO.setModelName( model.getModelName() );
        if ( model.getLastModelVersion() != null ) {
            modelPageVO.setLastModelVersion( String.valueOf( model.getLastModelVersion() ) );
        }
        modelPageVO.setAccuracyRate( model.getAccuracyRate() );

        modelPageVO.setModelType( BaseConvert.INSTANCE.getDictionaryValue(model.getModelType()) );

        return modelPageVO;
    }

    @Override
    public List<ModelPageVO> po2ModelPageVOList(List<Model> records) {
        if ( records == null ) {
            return null;
        }

        List<ModelPageVO> list = new ArrayList<ModelPageVO>( records.size() );
        for ( Model model : records ) {
            list.add( po2ModelPageVO( model ) );
        }

        return list;
    }

    @Override
    public ModelEchoVO po2ModelEchoVO(Model model) {
        if ( model == null ) {
            return null;
        }

        ModelEchoVO modelEchoVO = new ModelEchoVO();

        modelEchoVO.setModelId( model.getId() );
        modelEchoVO.setModelName( model.getModelName() );
        modelEchoVO.setModelType( model.getModelType() );
        modelEchoVO.setModelDesc( model.getModelDesc() );

        return modelEchoVO;
    }

    @Override
    public Model modelSaveDTO2Po(ModelSaveDTO modelSaveDTO) {
        if ( modelSaveDTO == null ) {
            return null;
        }

        Model model = new Model();

        model.setId( modelSaveDTO.getModelId() );
        model.setModelName( modelSaveDTO.getModelName() );
        model.setModelDesc( modelSaveDTO.getModelDesc() );
        model.setModelType( modelSaveDTO.getModelType() );

        return model;
    }

    @Override
    public ModelInitTrainMessage modelOperationDTO2InitMessage(ModelOperationDTO modelOperationDTO) {
        if ( modelOperationDTO == null ) {
            return null;
        }

        ModelInitTrainMessage modelInitTrainMessage = new ModelInitTrainMessage();

        modelInitTrainMessage.setModelId( modelOperationDTO.getModelId() );
        modelInitTrainMessage.setResolution( modelOperationDTO.getResolution() );
        modelInitTrainMessage.setIterateTimes( modelOperationDTO.getIterateTimes() );
        modelInitTrainMessage.setNetworkStructure( modelOperationDTO.getNetworkStructure() );
        modelInitTrainMessage.setOptimizer( modelOperationDTO.getOptimizer() );
        modelInitTrainMessage.setLossValue( modelOperationDTO.getLossValue() );

        return modelInitTrainMessage;
    }

    @Override
    public ModelOptimizeTrainMessage modelOperationDTO2OptimizeMessage(ModelOperationDTO modelOperationDTO) {
        if ( modelOperationDTO == null ) {
            return null;
        }

        ModelOptimizeTrainMessage modelOptimizeTrainMessage = new ModelOptimizeTrainMessage();

        modelOptimizeTrainMessage.setModelId( modelOperationDTO.getModelId() );
        modelOptimizeTrainMessage.setResolution( modelOperationDTO.getResolution() );
        modelOptimizeTrainMessage.setIterateTimes( modelOperationDTO.getIterateTimes() );
        modelOptimizeTrainMessage.setNetworkStructure( modelOperationDTO.getNetworkStructure() );
        modelOptimizeTrainMessage.setOptimizer( modelOperationDTO.getOptimizer() );
        modelOptimizeTrainMessage.setLossValue( modelOperationDTO.getLossValue() );

        return modelOptimizeTrainMessage;
    }

    @Override
    public ModelDetailVO po2ModelDetailVO(Model model) {
        if ( model == null ) {
            return null;
        }

        ModelDetailVO modelDetailVO = new ModelDetailVO();

        if ( model.getId() != null ) {
            modelDetailVO.setModelId( String.valueOf( model.getId() ) );
        }
        modelDetailVO.setModelStatus( getModelStatus( model.getModelStatus() ) );
        modelDetailVO.setTrainingStatus( getTrainingStatus( model.getTrainingStatus() ) );
        modelDetailVO.setModelName( model.getModelName() );
        modelDetailVO.setModelDesc( model.getModelDesc() );
        modelDetailVO.setLastModelVersion( model.getLastModelVersion() );
        modelDetailVO.setAccuracyRate( model.getAccuracyRate() );

        modelDetailVO.setModelType( BaseConvert.INSTANCE.getDictionaryValue(model.getModelType()) );

        return modelDetailVO;
    }

    @Override
    public ReleaseModelVO po2ReleaseModelVO(Model model) {
        if ( model == null ) {
            return null;
        }

        ReleaseModelVO releaseModelVO = new ReleaseModelVO();

        releaseModelVO.setModelId( model.getId() );
        releaseModelVO.setModelName( model.getModelName() );
        releaseModelVO.setModelDesc( model.getModelDesc() );

        return releaseModelVO;
    }
}
