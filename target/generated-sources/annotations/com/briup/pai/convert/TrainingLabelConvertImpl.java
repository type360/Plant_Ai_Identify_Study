package com.briup.pai.convert;

import com.briup.pai.entity.message.LabelResultMessage;
import com.briup.pai.entity.po.TrainingLabel;
import com.briup.pai.entity.vo.ModelOperationResultVO;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-16T14:38:30+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.8 (Oracle Corporation)"
)
@Component
public class TrainingLabelConvertImpl implements TrainingLabelConvert {

    @Override
    public TrainingLabel labelResultMessage2Po(LabelResultMessage labelResultMessage) {
        if ( labelResultMessage == null ) {
            return null;
        }

        TrainingLabel trainingLabel = new TrainingLabel();

        trainingLabel.setLabelName( labelResultMessage.getLabelName() );
        if ( labelResultMessage.getF1Score() != null ) {
            trainingLabel.setF1Score( labelResultMessage.getF1Score().doubleValue() );
        }
        if ( labelResultMessage.getGScore() != null ) {
            trainingLabel.setGScore( labelResultMessage.getGScore().doubleValue() );
        }
        if ( labelResultMessage.getPrecisionRate() != null ) {
            trainingLabel.setPrecisionRate( labelResultMessage.getPrecisionRate().doubleValue() );
        }
        if ( labelResultMessage.getRecallRate() != null ) {
            trainingLabel.setRecallRate( labelResultMessage.getRecallRate().doubleValue() );
        }

        return trainingLabel;
    }

    @Override
    public ModelOperationResultVO po2ModelOperationResultVO(TrainingLabel trainingLabel) {
        if ( trainingLabel == null ) {
            return null;
        }

        ModelOperationResultVO modelOperationResultVO = new ModelOperationResultVO();

        modelOperationResultVO.setLabelName( trainingLabel.getLabelName() );
        if ( trainingLabel.getF1Score() != null ) {
            modelOperationResultVO.setF1Score( BigDecimal.valueOf( trainingLabel.getF1Score() ) );
        }
        if ( trainingLabel.getGScore() != null ) {
            modelOperationResultVO.setGScore( BigDecimal.valueOf( trainingLabel.getGScore() ) );
        }
        if ( trainingLabel.getPrecisionRate() != null ) {
            modelOperationResultVO.setPrecisionRate( BigDecimal.valueOf( trainingLabel.getPrecisionRate() ) );
        }
        if ( trainingLabel.getRecallRate() != null ) {
            modelOperationResultVO.setRecallRate( BigDecimal.valueOf( trainingLabel.getRecallRate() ) );
        }

        return modelOperationResultVO;
    }
}
