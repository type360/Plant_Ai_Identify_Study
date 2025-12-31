package com.briup.pai.convert;

import com.briup.pai.entity.dto.ClassifySaveDTO;
import com.briup.pai.entity.po.Classify;
import com.briup.pai.entity.vo.ClassifyEchoVO;
import com.briup.pai.entity.vo.ClassifyInDatasetVO;
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
public class ClassifyConvertImpl implements ClassifyConvert {

    @Override
    public ClassifyInDatasetVO po2ClassifyInDatasetVO(Classify classify) {
        if ( classify == null ) {
            return null;
        }

        ClassifyInDatasetVO classifyInDatasetVO = new ClassifyInDatasetVO();

        classifyInDatasetVO.setClassifyId( classify.getId() );
        classifyInDatasetVO.setClassifyName( classify.getClassifyName() );

        return classifyInDatasetVO;
    }

    @Override
    public List<ClassifyInDatasetVO> po2ClassifyInDatasetVOList(List<Classify> classifyList) {
        if ( classifyList == null ) {
            return null;
        }

        List<ClassifyInDatasetVO> list = new ArrayList<ClassifyInDatasetVO>( classifyList.size() );
        for ( Classify classify : classifyList ) {
            list.add( po2ClassifyInDatasetVO( classify ) );
        }

        return list;
    }

    @Override
    public Classify classifySaveDTO2Po(ClassifySaveDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Classify classify = new Classify();

        classify.setId( dto.getClassifyId() );
        classify.setDatasetId( dto.getDatasetId() );
        classify.setClassifyName( dto.getClassifyName() );

        return classify;
    }

    @Override
    public ClassifyEchoVO po2ClassifyEchoVO(Classify classify) {
        if ( classify == null ) {
            return null;
        }

        ClassifyEchoVO classifyEchoVO = new ClassifyEchoVO();

        classifyEchoVO.setClassifyId( classify.getId() );
        classifyEchoVO.setClassifyName( classify.getClassifyName() );

        return classifyEchoVO;
    }
}
