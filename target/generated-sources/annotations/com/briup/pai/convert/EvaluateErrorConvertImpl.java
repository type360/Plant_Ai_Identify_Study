package com.briup.pai.convert;

import com.briup.pai.entity.message.EvaluateErrorMessage;
import com.briup.pai.entity.po.EvaluateError;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-16T14:38:30+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.8 (Oracle Corporation)"
)
@Component
public class EvaluateErrorConvertImpl implements EvaluateErrorConvert {

    @Override
    public EvaluateError evaluateErrorMessage2Po(EvaluateErrorMessage evaluateErrorMessage) {
        if ( evaluateErrorMessage == null ) {
            return null;
        }

        EvaluateError evaluateError = new EvaluateError();

        evaluateError.setPicAddress( evaluateErrorMessage.getPicAddress() );
        evaluateError.setOldLabel( evaluateErrorMessage.getOldLabel() );
        evaluateError.setNewLabel( evaluateErrorMessage.getNewLabel() );

        return evaluateError;
    }
}
