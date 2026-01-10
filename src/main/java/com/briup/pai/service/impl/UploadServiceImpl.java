package com.briup.pai.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.briup.pai.common.enums.DatasetStatusEnum;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.enums.UploadStatusEnum;
import com.briup.pai.common.exception.BriupAssert;
import com.briup.pai.entity.dto.UploadChunkDTO;
import com.briup.pai.entity.dto.UploadVerifyFileDTO;
import com.briup.pai.entity.po.Dataset;
import com.briup.pai.entity.po.FileInfo;
import com.briup.pai.entity.vo.UploadVerifyFileVO;
import com.briup.pai.service.IDatasetService;
import com.briup.pai.service.IFileInfoService;
import com.briup.pai.service.IUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UploadServiceImpl implements IUploadService {
    @Autowired
    private IDatasetService datasetService;
    @Autowired
    private IFileInfoService fileInfoService;

    @Override
    public void modifyDatasetStatus(Integer datasetId, Integer status) {
        Dataset dataset = BriupAssert.requireNotNull(datasetService, Dataset::getId, datasetId, ResultCodeEnum.DATA_NOT_EXIST);
        BriupAssert.requireIn(status, DatasetStatusEnum.statusList(),ResultCodeEnum.PARAM_IS_ERROR);
        //修改数据集状态
        dataset.setDatasetStatus(status);
        datasetService.updateById(dataset);
    }

    /*
    * "fileHash": "45a1c6a118cea74baf9abb4df053871c",
    * "fileName": "CornDisease1.zip",
    * "chunkSize": 5242880,每片大小
    * "fileSize": 1795981105 文件总大小
    * 校验信息时就将文件信息存放到数据库中
    * */
    @Override
    public UploadVerifyFileVO verifyFile(UploadVerifyFileDTO dto) {
        UploadVerifyFileVO uploadVerifyFileVO = new UploadVerifyFileVO();
        uploadVerifyFileVO.setUploaded(true);
        String fileHash = dto.getFileHash();
        String fileName = dto.getFileName();
        Long fileSize = dto.getFileSize();
        Long chunkSize = dto.getChunkSize();
        LambdaQueryWrapper<FileInfo> wrapper = Wrappers.<FileInfo>lambdaQuery().eq(FileInfo::getFileHash, fileHash);
        FileInfo fileInfo = fileInfoService.getOne(wrapper);
        // 如果文件没有上传 上传状态是false
        if(ObjectUtil.isNull(fileInfo)){
            uploadVerifyFileVO.setUploaded(false);
            // 保存文件信息 []
            FileInfo fileInfoSave = new FileInfo();
            fileInfoSave.setFileHash(fileHash);
            fileInfoSave.setFileName(fileName);
            fileInfoSave.setFileSize(fileSize);
            fileInfoSave.setChunkSize(chunkSize);
            fileInfoSave.setChunkNum(fileSize%chunkSize==0?fileSize/chunkSize:fileSize/chunkSize+1);
            fileInfoSave.setUploadStatus(UploadStatusEnum.INIT.getStatus()); // 文件没有上传
            fileInfo.setFilePath();
            fileInfoService.save(fileInfoSave);
        }else if(Objects.equals(fileInfo.getUploadStatus(),UploadStatusEnum.UPLOADING.getStatus())){
            // 如果文件上传了一些 返回已经上传的分片 上传状态是false
            uploadVerifyFileVO.setUploaded(false);
        }
            // 文件状态init
        // 如果文件全部上传完了 返回上传状态是true
        return null;
    }

    /*
    * 存储分片表
    * 上传分片信息
    *
    * */
    @Override
    public void uploadChunk(UploadChunkDTO dto) {

    }

    @Override
    public void modifyUploadStatus(String fileHash, Integer uploadStatus) {

    }

    @Override
    public void mergeChunks(String fileHash) {

    }

    @Override
    public void unzipDataset(Integer datasetId, String fileHash) {

    }

    @Override
    public void unzipClassify(Integer datasetId, Integer classifyId, String fileHash) {

    }
}