package com.briup.pai.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.exception.BriupAssert;
import com.briup.pai.convert.ClassifyConvert;
import com.briup.pai.dao.ClassifyMapper;
import com.briup.pai.entity.dto.ClassifySaveDTO;
import com.briup.pai.entity.po.Classify;
import com.briup.pai.entity.vo.ClassifyEchoVO;
import com.briup.pai.entity.vo.ClassifyInDatasetVO;
import com.briup.pai.entity.vo.EntityInClassifyVO;
import com.briup.pai.service.IClassifyService;
import com.briup.pai.service.IEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Service
public class ClassifyServiceImpl extends ServiceImpl<ClassifyMapper, Classify> implements IClassifyService {
    @Value("${upload.nginx-file-path}")
    private String nginxFilePath;
    @Autowired
    private ClassifyConvert classifyConvert;
    @Autowired
    private IEntityService entityService;

    @Override
    public List<ClassifyInDatasetVO> getClassifiesByDatasetId(Integer datasetId) {
        List<Classify> list = this.list(Wrappers.<Classify>lambdaQuery().eq(Classify::getDatasetId, datasetId));
        List<ClassifyInDatasetVO> classifyInDatasetVOS = classifyConvert.po2ClassifyInDatasetVOList(list)
                .stream().peek(classifyInDatasetVO -> {
                    List<EntityInClassifyVO> entityInClassifyVOS = entityService.getEntityByClassifyId(classifyInDatasetVO.getClassifyId());
                    classifyInDatasetVO.setEntityNum((long) entityInClassifyVOS.size());
                }).toList();
        return classifyInDatasetVOS;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ClassifyEchoVO addOrModifyClassify(ClassifySaveDTO dto) {
        //获取请求参数
        Integer classifyId = dto.getClassifyId();
        Integer datasetId = dto.getDatasetId();
        String classifyName = dto.getClassifyName();
        Classify classify = null;
        if (ObjectUtil.isNull(classifyId)) {
            // 分类名字在当前数据集下不能重复
            Wrapper<Classify> wrapper = Wrappers.<Classify>lambdaQuery()
                    .eq(Classify::getDatasetId, datasetId)
                    .eq(Classify::getClassifyName, classifyName);
            classify = classifyConvert.classifySaveDTO2Po(dto);
            BriupAssert.requireNull(this.getOne(wrapper), ResultCodeEnum.DATA_ALREADY_EXIST);
            this.save(classify);
            //添加物理磁盘目录 E:pai-file-nginx/html
            FileUtil.mkdir(nginxFilePath + "/" + datasetId + "/" + classifyName);
        }else {
            // 分类必须存在
            Classify temp = BriupAssert.requireNotNull(
                    this,
                    Classify::getId,
                    classifyId,
                    ResultCodeEnum.DATA_NOT_EXIST
            );
            // 根据数据集id和分类名字 查出唯一值[不能和其他分类重复] 同时不能是自己的classifyId
            LambdaQueryWrapper<Classify> wrapper = Wrappers.<Classify>lambdaQuery()
                    .eq(Classify::getDatasetId, datasetId)
                    .eq(Classify::getClassifyName, classifyName)
                    .ne(Classify::getId, classifyId);
            BriupAssert.requireNull(this.getOne(wrapper), ResultCodeEnum.DATA_ALREADY_EXIST
            );
            // 数据集ID不能修改
            BriupAssert.requireEqual(
                    temp.getDatasetId(),
                    datasetId,
                    ResultCodeEnum.PARAM_IS_ERROR
            );
            // 转换po修改
            classify = classifyConvert.classifySaveDTO2Po(dto);
            this.updateById(classify);
            // 同步修改nginx目录下文件夹名称
            File file = new File(this.nginxFilePath + "/" + temp.getDatasetId() + "/" + temp.getClassifyName());
            FileUtil.rename(file, dto.getClassifyName(), true);
        }
        return classifyConvert.po2ClassifyEchoVO(classify);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeClassifyById(Integer datasetId, Integer classifyId) {
        // 删除分类下图片
        List<EntityInClassifyVO> entityInClassifyVOS = entityService.getEntityByClassifyId(classifyId);
        int[] entityIds = entityInClassifyVOS.stream().mapToInt(EntityInClassifyVO::getEntityId).toArray();
        entityService.removeBatchByIds(Arrays.asList(entityIds));
        // 删除分类
        // 删除磁盘上的分类文件夹


    }

    @Override
    public ClassifyEchoVO getClassifyById(Integer classifyId) {
        //分类必须存在
        Classify classify = BriupAssert.requireNotNull(
                this,
                Classify::getId,
                classifyId,
                ResultCodeEnum.DATA_NOT_EXIST
        );
        //转换VO返回
        return classifyConvert.po2ClassifyEchoVO(classify);
    }
}