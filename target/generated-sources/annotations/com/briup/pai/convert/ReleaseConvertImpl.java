package com.briup.pai.convert;

import com.briup.pai.entity.message.ModelReleaseResultMessage;
import com.briup.pai.entity.po.Release;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-16T14:38:30+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.8 (Oracle Corporation)"
)
@Component
public class ReleaseConvertImpl implements ReleaseConvert {

    @Override
    public Release modelReleaseResultMessage2Po(ModelReleaseResultMessage modelReleaseResultMessage) {
        if ( modelReleaseResultMessage == null ) {
            return null;
        }

        Release release = new Release();

        release.setCreateBy( modelReleaseResultMessage.getUserId() );
        release.setModelId( modelReleaseResultMessage.getModelId() );
        release.setModelUrl( modelReleaseResultMessage.getModelUrl() );

        return release;
    }
}
