package com.briup.pai.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.briup.pai.convert.LogConvert;
import com.briup.pai.dao.LogMapper;
import com.briup.pai.entity.dto.LogQueryDTO;
import com.briup.pai.entity.po.Log;
import com.briup.pai.entity.vo.LogPageVO;
import com.briup.pai.entity.vo.PageVO;
import com.briup.pai.service.ILogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements ILogService {

    @Autowired
    private LogConvert logConvert;
    @Override
    public PageVO<LogPageVO> getLogByPageAndCondition(Long pageNum, Long pageSize, LogQueryDTO logQueryDTO) {
        Page<Log> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Log> wrapper = Wrappers.<Log>lambdaQuery()
                .eq(ObjectUtil.isNotEmpty(logQueryDTO.getType()), Log::getType, logQueryDTO.getType())
                .eq(ObjectUtil.isNotEmpty(logQueryDTO.getIsSuccess()), Log::getIsSuccess, logQueryDTO.getIsSuccess())
                .ge(ObjectUtil.isNotEmpty(logQueryDTO.getStartTime()),Log::getOperateTime,logQueryDTO.getStartTime())
                .le(ObjectUtil.isNotEmpty(logQueryDTO.getEndTime()),Log::getOperateTime,logQueryDTO.getEndTime());
        page = this.page(page,wrapper);
        PageVO<LogPageVO> pageVO = new PageVO<>();
        pageVO.setTotal(page.getTotal());
        pageVO.setData( logConvert.po2LogQueryVOList(page.getRecords()));

        return pageVO;
    }

    @Override
    public void exportExcel(LogQueryDTO logQueryDTO, HttpServletResponse response) {

    }
}