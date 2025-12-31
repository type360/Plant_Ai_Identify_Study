package com.briup.pai.convert;

import com.briup.pai.entity.dto.ModelOperationDTO;
import com.briup.pai.entity.po.ModelConfig;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-16T14:38:30+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.8 (Oracle Corporation)"
)
@Component
public class ModelConfigConvertImpl implements ModelConfigConvert {

    @Override
    public ModelConfig modelOperationDTO2Po(ModelOperationDTO modelOperationDTO) {
        if ( modelOperationDTO == null ) {
            return null;
        }

        ModelConfig modelConfig = new ModelConfig();

        modelConfig.setModelId( modelOperationDTO.getModelId() );
        modelConfig.setResolution( modelOperationDTO.getResolution() );
        modelConfig.setIterateTimes( modelOperationDTO.getIterateTimes() );
        modelConfig.setNetworkStructure( modelOperationDTO.getNetworkStructure() );
        modelConfig.setOptimizer( modelOperationDTO.getOptimizer() );
        modelConfig.setLossValue( modelOperationDTO.getLossValue() );

        return modelConfig;
    }
}
