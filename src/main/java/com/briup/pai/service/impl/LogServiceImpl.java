package com.briup.pai.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.exception.CustomException;
import com.briup.pai.convert.LogConvert;
import com.briup.pai.dao.LogMapper;
import com.briup.pai.entity.dto.LogExportDTO;
import com.briup.pai.entity.dto.LogQueryDTO;
import com.briup.pai.entity.po.Log;
import com.briup.pai.entity.vo.LogPageVO;
import com.briup.pai.entity.vo.PageVO;
import com.briup.pai.service.ILogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements ILogService {
    @Autowired
    private LogConvert logConvert;
    @Override
    public PageVO<LogPageVO> getLogByPageAndCondition(Long pageNum, Long pageSize, LogQueryDTO logQueryDTO) {
        Page<Log> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Log> wrapper = Wrappers.<Log>lambdaQuery()
                .eq(ObjectUtil.isNotNull(logQueryDTO.getType()),Log::getType, logQueryDTO.getType())
                .eq(ObjectUtil.isNotNull(logQueryDTO.getIsSuccess()),Log::getIsSuccess, logQueryDTO.getIsSuccess())
                .ge(ObjectUtil.isNotNull(logQueryDTO.getStartTime()),Log::getOperateTime, logQueryDTO.getStartTime())
                .le(ObjectUtil.isNotNull(logQueryDTO.getEndTime()),Log::getOperateTime, logQueryDTO.getEndTime());
        page = this.page(page,wrapper);

        PageVO<LogPageVO> pageVO = new PageVO<>();
        pageVO.setTotal(page.getTotal());
        pageVO.setData(logConvert.po2LogQueryVOList(page.getRecords()));
        return pageVO;
    }

    @Override
    public void exportExcel(LogQueryDTO logQueryDTO, HttpServletResponse response) {
        LambdaQueryWrapper<Log> wrapper = Wrappers.<Log>lambdaQuery()
                .eq(ObjectUtil.isNotNull(logQueryDTO.getType()),Log::getType, logQueryDTO.getType())
                .eq(ObjectUtil.isNotNull(logQueryDTO.getIsSuccess()),Log::getIsSuccess, logQueryDTO.getIsSuccess())
                .ge(ObjectUtil.isNotNull(logQueryDTO.getStartTime()),Log::getOperateTime, logQueryDTO.getStartTime())
                .le(ObjectUtil.isNotNull(logQueryDTO.getEndTime()),Log::getOperateTime, logQueryDTO.getEndTime());
        List<Log> list = this.list(wrapper);
        // 将List<Log> -> List<LogExportDTO>
        List<LogExportDTO> logExportDTOS = logConvert.po2LogExportDTOList(list);
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("日志数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        try {
            EasyExcel.write(response.getOutputStream(), LogExportDTO.class)
                    .sheet()
                    .doWrite(logExportDTOS);
        } catch (IOException e) {
            throw new CustomException(ResultCodeEnum.FILE_EXPORT_ERROR);
        }
    }
}