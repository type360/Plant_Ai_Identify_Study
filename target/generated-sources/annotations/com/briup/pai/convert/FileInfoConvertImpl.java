package com.briup.pai.convert;

import com.briup.pai.entity.dto.UploadVerifyFileDTO;
import com.briup.pai.entity.po.FileInfo;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-16T14:38:30+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.8 (Oracle Corporation)"
)
@Component
public class FileInfoConvertImpl implements FileInfoConvert {

    @Override
    public FileInfo uploadVerifyFileDTO2Po(UploadVerifyFileDTO dto) {
        if ( dto == null ) {
            return null;
        }

        FileInfo fileInfo = new FileInfo();

        fileInfo.setFileHash( dto.getFileHash() );
        fileInfo.setFileName( dto.getFileName() );
        fileInfo.setFileSize( dto.getFileSize() );

        return fileInfo;
    }
}
