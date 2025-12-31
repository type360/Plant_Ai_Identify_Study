package com.briup.pai.convert;

import com.briup.pai.entity.dto.OperatorImportDTO;
import com.briup.pai.entity.dto.OperatorUpdateDTO;
import com.briup.pai.entity.po.Operator;
import com.briup.pai.entity.vo.DropDownVO;
import com.briup.pai.entity.vo.OperatorEchoVO;
import com.briup.pai.entity.vo.OperatorPageVO;
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
public class OperatorConvertImpl implements OperatorConvert {

    @Override
    public OperatorPageVO po2OperatorPageVO(Operator operator) {
        if ( operator == null ) {
            return null;
        }

        OperatorPageVO operatorPageVO = new OperatorPageVO();

        operatorPageVO.setOperatorId( operator.getId() );
        operatorPageVO.setOperatorType( getOperatorTypeStr( operator.getOperatorType() ) );
        operatorPageVO.setOperatorCategory( getOperatorCategoryStr( operator.getOperatorCategory() ) );
        operatorPageVO.setOperatorName( operator.getOperatorName() );
        operatorPageVO.setOperatorUrl( operator.getOperatorUrl() );
        operatorPageVO.setCreateTime( operator.getCreateTime() );

        operatorPageVO.setCreateUser( BaseConvert.INSTANCE.getCreateUser(operator.getCreateBy()) );

        return operatorPageVO;
    }

    @Override
    public List<OperatorPageVO> po2OperatorPageVOList(List<Operator> operators) {
        if ( operators == null ) {
            return null;
        }

        List<OperatorPageVO> list = new ArrayList<OperatorPageVO>( operators.size() );
        for ( Operator operator : operators ) {
            list.add( po2OperatorPageVO( operator ) );
        }

        return list;
    }

    @Override
    public OperatorEchoVO po2OperatorEchoVO(Operator operator) {
        if ( operator == null ) {
            return null;
        }

        OperatorEchoVO operatorEchoVO = new OperatorEchoVO();

        operatorEchoVO.setOperatorId( operator.getId() );
        operatorEchoVO.setOperatorName( operator.getOperatorName() );
        operatorEchoVO.setOperatorUrl( operator.getOperatorUrl() );

        return operatorEchoVO;
    }

    @Override
    public Operator operatorUpdateDTO2po(OperatorUpdateDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Operator operator = new Operator();

        operator.setId( dto.getOperatorId() );
        operator.setOperatorName( dto.getOperatorName() );
        operator.setOperatorUrl( dto.getOperatorUrl() );

        return operator;
    }

    @Override
    public Operator operatorImportDto2Po(OperatorImportDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Operator operator = new Operator();

        operator.setOperatorType( getOperatorTypeNum( dto.getOperatorType() ) );
        operator.setOperatorCategory( getOperatorCategoryNum( dto.getOperatorCategory() ) );
        operator.setOperatorName( dto.getOperatorName() );
        operator.setOperatorUrl( dto.getOperatorUrl() );

        operator.setCreateBy( BaseConvert.INSTANCE.getCreateUserId() );

        return operator;
    }

    @Override
    public List<Operator> operatorImportDto2PoList(List<OperatorImportDTO> operatorExcelDTOList) {
        if ( operatorExcelDTOList == null ) {
            return null;
        }

        List<Operator> list = new ArrayList<Operator>( operatorExcelDTOList.size() );
        for ( OperatorImportDTO operatorImportDTO : operatorExcelDTOList ) {
            list.add( operatorImportDto2Po( operatorImportDTO ) );
        }

        return list;
    }

    @Override
    public DropDownVO po2DropDownVO(Operator operator) {
        if ( operator == null ) {
            return null;
        }

        DropDownVO dropDownVO = new DropDownVO();

        dropDownVO.setKey( operator.getOperatorUrl() );
        dropDownVO.setValue( operator.getOperatorName() );

        return dropDownVO;
    }

    @Override
    public List<DropDownVO> po2DropDownList(List<Operator> operators) {
        if ( operators == null ) {
            return null;
        }

        List<DropDownVO> list = new ArrayList<DropDownVO>( operators.size() );
        for ( Operator operator : operators ) {
            list.add( po2DropDownVO( operator ) );
        }

        return list;
    }
}
