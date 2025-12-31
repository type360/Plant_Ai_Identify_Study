package com.briup.pai.convert;

import com.briup.pai.entity.dto.DatasetSaveDTO;
import com.briup.pai.entity.po.Dataset;
import com.briup.pai.entity.vo.DatasetDetailVO;
import com.briup.pai.entity.vo.DatasetEchoVO;
import com.briup.pai.entity.vo.DatasetPageVO;
import com.briup.pai.entity.vo.TrainingDatasetQueryVO;
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
public class DatasetConvertImpl implements DatasetConvert {

    @Override
    public DatasetPageVO po2DatasetPageVO(Dataset dataset) {
        if ( dataset == null ) {
            return null;
        }

        DatasetPageVO datasetPageVO = new DatasetPageVO();

        datasetPageVO.setDatasetId( dataset.getId() );
        datasetPageVO.setDatasetStatus( getDatasetStatus( dataset.getDatasetStatus() ) );
        datasetPageVO.setDatasetUsage( getDatasetUsage( dataset.getDatasetUsage() ) );
        datasetPageVO.setDatasetName( dataset.getDatasetName() );

        datasetPageVO.setDatasetType( BaseConvert.INSTANCE.getDictionaryValue(dataset.getDatasetType()) );

        return datasetPageVO;
    }

    @Override
    public List<DatasetPageVO> po2DatasetPageVOList(List<Dataset> datasets) {
        if ( datasets == null ) {
            return null;
        }

        List<DatasetPageVO> list = new ArrayList<DatasetPageVO>( datasets.size() );
        for ( Dataset dataset : datasets ) {
            list.add( po2DatasetPageVO( dataset ) );
        }

        return list;
    }

    @Override
    public Dataset datasetSaveDTO2Po(DatasetSaveDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Dataset dataset = new Dataset();

        dataset.setId( dto.getDatasetId() );
        dataset.setDatasetName( dto.getDatasetName() );
        dataset.setDatasetType( dto.getDatasetType() );
        dataset.setDatasetDesc( dto.getDatasetDesc() );
        dataset.setDatasetUsage( dto.getDatasetUsage() );

        dataset.setCreateBy( BaseConvert.INSTANCE.getCreateUserId() );

        return dataset;
    }

    @Override
    public DatasetEchoVO po2DatasetEchoVO(Dataset dataset) {
        if ( dataset == null ) {
            return null;
        }

        DatasetEchoVO datasetEchoVO = new DatasetEchoVO();

        datasetEchoVO.setDatasetId( dataset.getId() );
        datasetEchoVO.setDatasetName( dataset.getDatasetName() );
        datasetEchoVO.setDatasetType( dataset.getDatasetType() );
        datasetEchoVO.setDatasetUsage( dataset.getDatasetUsage() );
        datasetEchoVO.setDatasetDesc( dataset.getDatasetDesc() );

        return datasetEchoVO;
    }

    @Override
    public DatasetDetailVO po2DatasetDetailVO(Dataset dataset) {
        if ( dataset == null ) {
            return null;
        }

        DatasetDetailVO datasetDetailVO = new DatasetDetailVO();

        datasetDetailVO.setDatasetId( dataset.getId() );
        datasetDetailVO.setDatasetStatus( getDatasetStatus( dataset.getDatasetStatus() ) );
        datasetDetailVO.setDatasetUsage( getDatasetUsage( dataset.getDatasetUsage() ) );
        datasetDetailVO.setDatasetName( dataset.getDatasetName() );
        datasetDetailVO.setDatasetDesc( dataset.getDatasetDesc() );
        datasetDetailVO.setCreateTime( dataset.getCreateTime() );

        datasetDetailVO.setDatasetType( BaseConvert.INSTANCE.getDictionaryValue(dataset.getDatasetType()) );
        datasetDetailVO.setCreateUser( BaseConvert.INSTANCE.getCreateUser(dataset.getCreateBy()) );

        return datasetDetailVO;
    }

    @Override
    public TrainingDatasetQueryVO po2TrainingDatasetQueryVO(Dataset dataset) {
        if ( dataset == null ) {
            return null;
        }

        TrainingDatasetQueryVO trainingDatasetQueryVO = new TrainingDatasetQueryVO();

        trainingDatasetQueryVO.setDatasetId( dataset.getId() );
        trainingDatasetQueryVO.setDatasetName( dataset.getDatasetName() );

        return trainingDatasetQueryVO;
    }

    @Override
    public List<TrainingDatasetQueryVO> po2TrainingDatasetQueryVOList(List<Dataset> datasetList) {
        if ( datasetList == null ) {
            return null;
        }

        List<TrainingDatasetQueryVO> list = new ArrayList<TrainingDatasetQueryVO>( datasetList.size() );
        for ( Dataset dataset : datasetList ) {
            list.add( po2TrainingDatasetQueryVO( dataset ) );
        }

        return list;
    }
}
