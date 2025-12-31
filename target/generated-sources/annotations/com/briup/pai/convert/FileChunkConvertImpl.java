package com.briup.pai.convert;

import com.briup.pai.entity.dto.UploadChunkDTO;
import com.briup.pai.entity.po.FileChunk;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-16T14:38:30+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.8 (Oracle Corporation)"
)
@Component
public class FileChunkConvertImpl implements FileChunkConvert {

    @Override
    public FileChunk uploadChunkDTO2Po(UploadChunkDTO dto) {
        if ( dto == null ) {
            return null;
        }

        FileChunk fileChunk = new FileChunk();

        fileChunk.setFileHash( dto.getFileHash() );
        fileChunk.setChunkIndex( dto.getChunkIndex() );

        return fileChunk;
    }
}
