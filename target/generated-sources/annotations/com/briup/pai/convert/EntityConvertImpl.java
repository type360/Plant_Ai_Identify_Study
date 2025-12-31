package com.briup.pai.convert;

import com.briup.pai.entity.po.Entity;
import com.briup.pai.entity.vo.EntityInClassifyVO;
import com.briup.pai.entity.vo.EntityPageVO;
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
public class EntityConvertImpl implements EntityConvert {

    @Override
    public EntityInClassifyVO po2EntityInClassifyVO(Entity entity) {
        if ( entity == null ) {
            return null;
        }

        EntityInClassifyVO entityInClassifyVO = new EntityInClassifyVO();

        entityInClassifyVO.setEntityId( entity.getId() );
        entityInClassifyVO.setEntityUrl( entity.getEntityUrl() );

        return entityInClassifyVO;
    }

    @Override
    public List<EntityInClassifyVO> po2EntityInClassifyVOList(List<Entity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<EntityInClassifyVO> list = new ArrayList<EntityInClassifyVO>( entities.size() );
        for ( Entity entity : entities ) {
            list.add( po2EntityInClassifyVO( entity ) );
        }

        return list;
    }

    @Override
    public EntityPageVO po2EntityPageVO(Entity entity) {
        if ( entity == null ) {
            return null;
        }

        EntityPageVO entityPageVO = new EntityPageVO();

        entityPageVO.setEntityId( entity.getId() );
        entityPageVO.setEntityUrl( entity.getEntityUrl() );

        return entityPageVO;
    }

    @Override
    public List<EntityPageVO> po2EntityPageVOList(List<Entity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<EntityPageVO> list = new ArrayList<EntityPageVO>( entities.size() );
        for ( Entity entity : entities ) {
            list.add( po2EntityPageVO( entity ) );
        }

        return list;
    }
}
