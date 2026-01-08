package com.briup.pai.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.briup.pai.common.constant.CommonConstant;
import com.briup.pai.common.constant.DatasetConstant;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.exception.BriupAssert;
import com.briup.pai.convert.EntityConvert;
import com.briup.pai.dao.EntityMapper;
import com.briup.pai.entity.po.Classify;
import com.briup.pai.entity.po.Entity;
import com.briup.pai.entity.vo.EntityInClassifyVO;
import com.briup.pai.entity.vo.EntityPageVO;
import com.briup.pai.entity.vo.PageVO;
import com.briup.pai.service.IEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EntityServiceImpl extends ServiceImpl<EntityMapper, Entity> implements IEntityService {
    @Value("${upload.nginx-file-path}")
    private String nginxFilePath;
    @Value("${upload.nginx-server}")
    private String nginxServer;

    @Autowired
    private EntityConvert entityConvert;
    @Autowired
    private ClassifyServiceImpl classifyServiceImpl;

    @Override
    public List<EntityInClassifyVO> getEntityByClassifyId(Integer classifyId) {
        List<Entity> list = this.list(Wrappers.<Entity>lambdaQuery().eq(Entity::getClassifyId, classifyId));
        return entityConvert.po2EntityInClassifyVOList(list);
    }

    @Override
    public PageVO<EntityPageVO> getEntityByPage(Integer classifyId, Long pageNum) {
        BriupAssert.requireNotNull(classifyServiceImpl,Classify::getId,classifyId, ResultCodeEnum.DATA_NOT_EXIST);
        IPage<Entity> page = new Page<>(pageNum, DatasetConstant.ENTITY_PAGE_SIZE);
        Wrapper<Entity> wrapper = Wrappers.<Entity>lambdaQuery().eq(Entity::getClassifyId, classifyId);
        page = this.page(page, wrapper);
        List<EntityPageVO> list = entityConvert.po2EntityPageVOList(page.getRecords())
                .stream()
                .peek(entityPageVO -> {
                    String oldUrl = entityPageVO.getEntityUrl();
                    Classify classify = classifyServiceImpl.getById(classifyId);
                    //这里应该是localhost:89/1/褐锈病/叶枯病/文件名.png
                    entityPageVO.setEntityUrl(nginxServer + "/" + classify.getDatasetId() + "/" + classify.getClassifyName() + "/" + oldUrl);
                }).toList();

        PageVO<EntityPageVO> pageVO = new PageVO<>();
        pageVO.setTotal(page.getTotal());
        pageVO.setData(list);
        return pageVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeEntityByBatch(Integer datasetId, Integer classifyId, List<Integer> entityIds) {
        Wrapper<Classify> wrapper = Wrappers.<Classify>lambdaQuery()
                .eq(Classify::getId, classifyId)
                .eq(Classify::getDatasetId, datasetId);
        Classify classify = classifyServiceImpl.getOne(wrapper);
        BriupAssert.requireNotNull(classify,ResultCodeEnum.DATA_NOT_EXIST);
        //取出图片完整的路径URL
        List<String> urlList = this.listByIds(entityIds).stream().peek(entity -> entity.setEntityUrl(CommonConstant.createEntityPath(nginxFilePath, datasetId, classify.getClassifyName(), entity.getEntityUrl())))
                .map(Entity::getEntityUrl).toList();
        //删除实体图片
        this.removeBatchByIds(entityIds);
        //删除磁盘上的图片
        urlList.forEach(FileUtil::del);
    }
}