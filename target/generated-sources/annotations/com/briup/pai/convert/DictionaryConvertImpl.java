package com.briup.pai.convert;

import com.briup.pai.entity.dto.DictionarySaveDTO;
import com.briup.pai.entity.po.Dictionary;
import com.briup.pai.entity.vo.DictionaryEchoVO;
import com.briup.pai.entity.vo.DictionaryPageVO;
import com.briup.pai.entity.vo.DropDownVO;
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
public class DictionaryConvertImpl implements DictionaryConvert {

    @Override
    public Dictionary dictionarySaveDTO2PO(DictionarySaveDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Dictionary dictionary = new Dictionary();

        dictionary.setId( dto.getDictId() );
        dictionary.setParentId( dto.getParentId() );
        dictionary.setDictCode( dto.getDictCode() );
        dictionary.setDictValue( dto.getDictValue() );

        return dictionary;
    }

    @Override
    public DictionaryEchoVO po2DictionaryEchoVO(Dictionary dictionary) {
        if ( dictionary == null ) {
            return null;
        }

        DictionaryEchoVO dictionaryEchoVO = new DictionaryEchoVO();

        dictionaryEchoVO.setDictId( dictionary.getId() );
        dictionaryEchoVO.setDictCode( dictionary.getDictCode() );
        dictionaryEchoVO.setDictValue( dictionary.getDictValue() );

        return dictionaryEchoVO;
    }

    @Override
    public DictionaryPageVO po2DictionaryPageVO(Dictionary dictionary) {
        if ( dictionary == null ) {
            return null;
        }

        DictionaryPageVO dictionaryPageVO = new DictionaryPageVO();

        dictionaryPageVO.setDictId( dictionary.getId() );
        dictionaryPageVO.setParentId( dictionary.getParentId() );
        dictionaryPageVO.setDictCode( dictionary.getDictCode() );
        dictionaryPageVO.setDictValue( dictionary.getDictValue() );

        return dictionaryPageVO;
    }

    @Override
    public List<DictionaryPageVO> po2DictionaryPageVOList(List<Dictionary> dictList) {
        if ( dictList == null ) {
            return null;
        }

        List<DictionaryPageVO> list = new ArrayList<DictionaryPageVO>( dictList.size() );
        for ( Dictionary dictionary : dictList ) {
            list.add( po2DictionaryPageVO( dictionary ) );
        }

        return list;
    }

    @Override
    public DropDownVO po2DictionaryDropDownVO(Dictionary dictionary) {
        if ( dictionary == null ) {
            return null;
        }

        DropDownVO dropDownVO = new DropDownVO();

        if ( dictionary.getId() != null ) {
            dropDownVO.setKey( String.valueOf( dictionary.getId() ) );
        }
        dropDownVO.setValue( dictionary.getDictValue() );

        return dropDownVO;
    }

    @Override
    public List<DropDownVO> po2DictionaryDropDownVOList(List<Dictionary> dictList) {
        if ( dictList == null ) {
            return null;
        }

        List<DropDownVO> list = new ArrayList<DropDownVO>( dictList.size() );
        for ( Dictionary dictionary : dictList ) {
            list.add( po2DictionaryDropDownVO( dictionary ) );
        }

        return list;
    }
}
