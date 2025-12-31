package com.briup.pai.convert;

import com.briup.pai.entity.message.LabelResultMessage;
import com.briup.pai.entity.po.EvaluateLabel;
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
public class EvaluateLabelConvertImpl implements EvaluateLabelConvert {

    @Override
    public EvaluateLabel labelResultMessage2Po(LabelResultMessage labelResultMessage) {
        if ( labelResultMessage == null ) {
            return null;
        }

        EvaluateLabel evaluateLabel = new EvaluateLabel();

        evaluateLabel.setLabelName( labelResultMessage.getLabelName() );
        if ( labelResultMessage.getF1Score() != null ) {
            evaluateLabel.setF1Score( labelResultMessage.getF1Score().doubleValue() );
        }
        if ( labelResultMessage.getGScore() != null ) {
            evaluateLabel.setGScore( labelResultMessage.getGScore().doubleValue() );
        }
        if ( labelResultMessage.getPrecisionRate() != null ) {
            evaluateLabel.setPrecisionRate( labelResultMessage.getPrecisionRate().doubleValue() );
        }
        if ( labelResultMessage.getRecallRate() != null ) {
            evaluateLabel.setRecallRate( labelResultMessage.getRecallRate().doubleValue() );
        }

        return evaluateLabel;
    }

    @Override
    public ModelOperationResultVO po2ModelOperationResultVO(EvaluateLabel evaluate) {
        if ( evaluate == null ) {
            return null;
        }

        ModelOperationResultVO modelOperationResultVO = new ModelOperationResultVO();

        modelOperationResultVO.setLabelName( evaluate.getLabelName() );
        if ( evaluate.getF1Score() != null ) {
            modelOperationResultVO.setF1Score( BigDecimal.valueOf( evaluate.getF1Score() ) );
        }
        if ( evaluate.getGScore() != null ) {
            modelOperationResultVO.setGScore( BigDecimal.valueOf( evaluate.getGScore() ) );
        }
        if ( evaluate.getPrecisionRate() != null ) {
            modelOperationResultVO.setPrecisionRate( BigDecimal.valueOf( evaluate.getPrecisionRate() ) );
        }
        if ( evaluate.getRecallRate() != null ) {
            modelOperationResultVO.setRecallRate( BigDecimal.valueOf( evaluate.getRecallRate() ) );
        }

        return modelOperationResultVO;
    }
}
