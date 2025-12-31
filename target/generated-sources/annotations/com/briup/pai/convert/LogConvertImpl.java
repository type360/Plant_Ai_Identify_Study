package com.briup.pai.convert;

import com.briup.pai.entity.dto.LogExportDTO;
import com.briup.pai.entity.po.Log;
import com.briup.pai.entity.vo.LogPageVO;
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
public class LogConvertImpl implements LogConvert {

    @Override
    public LogPageVO po2LogQueryVO(Log log) {
        if ( log == null ) {
            return null;
        }

        LogPageVO logPageVO = new LogPageVO();

        logPageVO.setType( getLogType( log.getType() ) );
        logPageVO.setIsSuccess( getRequestStatus( log.getIsSuccess() ) );
        logPageVO.setId( log.getId() );
        logPageVO.setRequestUri( log.getRequestUri() );
        logPageVO.setRequestMethod( log.getRequestMethod() );
        logPageVO.setRequestIp( log.getRequestIp() );
        logPageVO.setRequestParams( log.getRequestParams() );
        logPageVO.setMethodName( log.getMethodName() );
        logPageVO.setRequestTime( log.getRequestTime() );
        logPageVO.setResponseData( log.getResponseData() );
        logPageVO.setOperateTime( log.getOperateTime() );
        logPageVO.setOperateUser( log.getOperateUser() );

        return logPageVO;
    }

    @Override
    public List<LogPageVO> po2LogQueryVOList(List<Log> logs) {
        if ( logs == null ) {
            return null;
        }

        List<LogPageVO> list = new ArrayList<LogPageVO>( logs.size() );
        for ( Log log : logs ) {
            list.add( po2LogQueryVO( log ) );
        }

        return list;
    }

    @Override
    public LogExportDTO po2LogExportDTO(Log log) {
        if ( log == null ) {
            return null;
        }

        LogExportDTO logExportDTO = new LogExportDTO();

        logExportDTO.setType( getLogType( log.getType() ) );
        logExportDTO.setIsSuccess( getRequestStatus( log.getIsSuccess() ) );
        logExportDTO.setId( log.getId() );
        logExportDTO.setRequestUri( log.getRequestUri() );
        logExportDTO.setRequestMethod( log.getRequestMethod() );
        logExportDTO.setRequestIp( log.getRequestIp() );
        logExportDTO.setRequestParams( log.getRequestParams() );
        logExportDTO.setMethodName( log.getMethodName() );
        logExportDTO.setRequestTime( log.getRequestTime() );
        logExportDTO.setResponseData( log.getResponseData() );
        logExportDTO.setOperateTime( log.getOperateTime() );
        logExportDTO.setOperateUser( log.getOperateUser() );

        return logExportDTO;
    }

    @Override
    public List<LogExportDTO> po2LogExportDTOList(List<Log> logList) {
        if ( logList == null ) {
            return null;
        }

        List<LogExportDTO> list = new ArrayList<LogExportDTO>( logList.size() );
        for ( Log log : logList ) {
            list.add( po2LogExportDTO( log ) );
        }

        return list;
    }
}
