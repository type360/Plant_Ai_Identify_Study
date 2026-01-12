package com.briup.pai.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ZipUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.briup.pai.common.enums.DatasetStatusEnum;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.enums.UploadStatusEnum;
import com.briup.pai.common.exception.BriupAssert;
import com.briup.pai.common.exception.CustomException;
import com.briup.pai.entity.dto.UploadChunkDTO;
import com.briup.pai.entity.dto.UploadVerifyFileDTO;
import com.briup.pai.entity.po.*;
import com.briup.pai.entity.vo.UploadVerifyFileVO;
import com.briup.pai.service.IDatasetService;
import com.briup.pai.service.IFileChunkService;
import com.briup.pai.service.IFileInfoService;
import com.briup.pai.service.IUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class UploadServiceImpl implements IUploadService {
    @Value("${upload.nginx-file-path}")
    private String nginxFilePath;
    @Value("${upload.file-directory-name}")
    private String fileDirectoryName;
    @Value("{upload.chunk-directory-name}")
    private String chunkDirectoryName;
    @Autowired
    private IDatasetService datasetService;
    @Autowired
    private IFileInfoService fileInfoService;
    @Autowired
    private IFileChunkService fileChunkService;
    @Autowired
    private ClassifyServiceImpl classifyServiceImpl;
    @Autowired
    private EntityServiceImpl entityServiceImpl;

    @CacheEvict(
            key = "T(com.briup.pai.common.constant.CommonConstant).DETAIL_CACHE_PREFIX + ':' + #datasetId")
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
            String fileInfoDirectory = createFileInfoDirectory(fileHash);
            fileInfo.setFilePath(fileInfoDirectory + "/" + fileName);
            fileInfoService.save(fileInfoSave);
        }else if(!Objects.equals(fileInfo.getUploadStatus(),UploadStatusEnum.UPLOADED.getStatus())){
            // 如果文件上传了一些 返回已经上传的分片 上传状态是false
            List<FileChunk> list = fileChunkService.list(Wrappers.lambdaQuery(FileChunk.class).eq(FileChunk::getFileHash, fileHash));
            uploadVerifyFileVO.setUploadedChunks(list.stream().map(FileChunk::getChunkIndex).toList());
            uploadVerifyFileVO.setUploaded(false);
        }
            // 文件状态init
        // 如果文件全部上传完了 返回上传状态是true
        return uploadVerifyFileVO;
    }

    /**
     * 创建文件目录方法 [将来合并分片使用这个目录]
     * @param fileHash  文件MD5值
     * @return          文件目录路径
     * 存放路径格式：D:/nginx/pi-file-nginx/html/file/${fileHash}
     */
    private String createFileInfoDirectory(String fileHash) {
        String fileInfoPath = this.nginxFilePath + "/" + this.fileDirectoryName + "/" + fileHash;
        FileUtil.mkdir(fileInfoPath);
        return fileInfoPath;
    }

    /*
    * 存储分片表
    * 上传分片信息
    *
    * */
    @Override
    public void uploadChunk(UploadChunkDTO dto) {
        MultipartFile file = dto.getFile();
        Integer chunkIndex = dto.getChunkIndex();
        String fileHash = dto.getFileHash();
        String filePath = createFileInfoDirectory(fileHash) + "/" + chunkIndex;
        //存储分片到此盘中 /1 /2 /3
        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            throw new CustomException(ResultCodeEnum.FILE_UPLOAD_ERROR);
        }
        //存储分片信息到数据库中
        FileChunk fileChunk = new FileChunk();
        fileChunk.setChunkIndex(chunkIndex);
        fileChunk.setChunkPath(filePath);
        fileChunk.setChunkSize(file.getSize());
        fileChunk.setFileHash(fileHash);
        fileChunkService.save(fileChunk);
    }
    /**
     * 创建分片目录方法
     *
     * @param fileHash 文件MD5值
     * @return 分片目录路径
     * 存放路径格式：D:/nginx/pi-file-nginx/html/chunk/${fileHash}
     * 文件路径：D:/nginx/pi-file-nginx/html/chunk/${fileHash}/1
     */
    private String createFileChunkDirectory(String fileHash) {
        String fileChunkPath = this.nginxFilePath + "/" + this.chunkDirectoryName + "/" + fileHash;
        FileUtil.mkdir(fileChunkPath);
        return fileChunkPath;
    }

    @Override
    public void modifyUploadStatus(String fileHash, Integer uploadStatus) {
        FileInfo fileInfo = BriupAssert.requireNotNull(fileInfoService, FileInfo::getFileHash, fileHash, ResultCodeEnum.DATA_NOT_EXIST);
        BriupAssert.requireNotEqual(uploadStatus,fileInfo.getUploadStatus(),ResultCodeEnum.PARAM_IS_ERROR);
        fileInfo.setUploadStatus(uploadStatus);
        fileInfoService.updateById(fileInfo);
    }

    //文件hash
        //读分片路径上的文件->写到文件路径
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void mergeChunks(String fileHash) {
        //分片必须存在
        FileInfo fileInfo = BriupAssert.requireNotNull(fileInfoService, FileInfo::getFileHash, fileHash, ResultCodeEnum.DATA_NOT_EXIST);
        // 取出所有的分片信息
        List<FileChunk> list = fileChunkService.list(Wrappers
                .lambdaQuery(FileChunk.class)
                .eq(FileChunk::getFileHash, fileHash)
                .orderByAsc(FileChunk::getChunkIndex)
        );
        // 临时数组 做缓冲空间
        byte[] temp = new byte[1024*1024*2];
        int len = -1;
        // 读所有分片路径上的文件
        try(FileOutputStream fos = new FileOutputStream(fileInfo.getFilePath());) {
            for (FileChunk fileChunk : list) {
                try(FileInputStream fis = new FileInputStream(fileChunk.getChunkPath());) {
                    while ((len = fis.read(temp)) != -1){
                        // 写到文件路径
                        fos.write(temp,0,len);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        fileInfo.setUploadStatus(UploadStatusEnum.UPLOADED.getStatus());
        fileInfoService.updateById(fileInfo);
        // 删除分片磁盘数据 E:/pai-file-nginx/html/chunk/e0ddd30b09257745d7c9f76589a633bb
        FileUtil.del(createFileChunkDirectory(fileHash));
        // 删除分片数据库信息
        fileChunkService.removeBatchByIds(list.stream().map(FileChunk::getId).toList());
    }
    // 将指定压缩包文件 解压 指定目录中
    // E:/pai-file-nginx/html/file/d91ccc41c85f9f0852c5604c28725669/Tomato_Optimize_Train.zip
    // E:/pai-file-nginx/html/${datasetId}

    @CacheEvict(
            key = "T(com.briup.pai.common.constant.CommonConstant).DETAIL_CACHE_PREFIX + ':' + #datasetId")
    @Override
    public void unzipDataset(Integer datasetId, String fileHash) {
        // 校验数据
        Dataset dataset = BriupAssert.requireNotNull(datasetService, Dataset::getId, datasetId, ResultCodeEnum.DATA_NOT_EXIST);
        FileInfo fileInfo = BriupAssert.requireNotNull(fileInfoService, FileInfo::getFileHash, fileHash, ResultCodeEnum.DATA_NOT_EXIST);
        // 源文件
//        FileInfo fileInfo = fileInfoService.getOne(Wrappers.<FileInfo>lambdaQuery().eq(FileInfo::getFileHash, fileHash));
        File sourceFile = new File(fileInfo.getFilePath());
        // 目标文件
        File targetFile = new File(nginxFilePath + "/" + datasetId);
        FileUtil.mkdir(targetFile);
        // 解压
        ZipUtil.unzip(sourceFile, targetFile, CharsetUtil.CHARSET_GBK);

        // 存储分类和图片信息
        File[] classifyList = targetFile.listFiles(File::isDirectory);
        if(classifyList!=null && classifyList.length > 0){
            for (File classify : classifyList) { // 遍历每个分类目录
                Classify c = new Classify();
                c.setDatasetId(datasetId);
                c.setClassifyName(classify.getName());
                classifyServiceImpl.save(c); // mp主键是自动回填的
                // 存储图片
//                List<Entity> entityList = entityServiceImpl.list(Wrappers.<Entity>lambdaQuery().eq(Entity::getClassifyId,c.getId()));
                File[] entityList = classify.listFiles(pathname -> pathname.isFile() && !pathname.getName().endsWith("DS_Store"));
                if(entityList != null && entityList.length > 0){
                    saveEntityList(entityList,c.getId());
                }
            }
        }
        dataset.setDatasetStatus(DatasetStatusEnum.DONE.getStatus());
        datasetService.updateById(dataset);

    }

    @Transactional(rollbackFor = Exception.class)
    public void saveEntityList(File[] entityList, Integer classifyId) {
        List<Entity> list = Arrays.stream(entityList).map(file -> {
            Entity entity = new Entity();
            entity.setClassifyId(classifyId);
            entity.setEntityUrl(file.getName());
            return entity;
        }).toList();
        entityServiceImpl.saveBatch(list);
    }

    @CacheEvict(
            key = "T(com.briup.pai.common.constant.CommonConstant).DETAIL_CACHE_PREFIX + ':' + #datasetId")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unzipClassify(Integer datasetId, Integer classifyId, String fileHash) {
        // 校验数据
        Dataset dataset = BriupAssert.requireNotNull(datasetService, Dataset::getId, datasetId, ResultCodeEnum.DATA_NOT_EXIST);
        FileInfo fileInfo = BriupAssert.requireNotNull(fileInfoService, FileInfo::getFileHash, fileHash, ResultCodeEnum.DATA_NOT_EXIST);
        Classify classify = BriupAssert.requireNotNull(classifyServiceImpl, Classify::getId, classifyId, ResultCodeEnum.DATA_NOT_EXIST);
        // 原路径
        File sourceFile = new File(fileInfo.getFilePath());
        // 目标路径
        File targetFile = new File(nginxFilePath + "/" + datasetId + "/" + classify.getClassifyName());
        // 解压之前的图片列表 38张
        List<String> oldEntityNames = Arrays.stream(targetFile.listFiles(file -> file.isFile() && !file.getName().endsWith("DS_Store")))
                .map(File::getName).toList();
        // 归档 新增了2张
        ZipUtil.unzip(sourceFile, targetFile, CharsetUtil.CHARSET_GBK);

        // 存储新增的图片信息
        List<String> newEntityNames = Arrays.stream(targetFile.listFiles(file -> file.isFile() && !file.getName().endsWith("DS_Store")))
                .map(File::getName).toList();
//        newEntityNames.removeAll(oldEntityNames);
        List<String> list = new ArrayList<>();
        for (String newEntity : newEntityNames) { // 40
            if(!oldEntityNames.contains(newEntity)){
                list.add(newEntity);
            }
        }
        List<Entity> entityList = list.stream().map(fileName -> {
            Entity entity = new Entity();
            entity.setClassifyId(classifyId);
            entity.setEntityUrl(fileName);
            return entity;
        }).toList();
        entityServiceImpl.saveBatch(entityList);
    }
}