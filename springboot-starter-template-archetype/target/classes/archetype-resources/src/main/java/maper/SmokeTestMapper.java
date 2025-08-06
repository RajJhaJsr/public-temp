#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.maper;


import ${package}.dto.SmokeTestRequestDto;
import ${package}.dto.SmokeTestResponseDto;
import ${package}.entity.SmokeTestEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SmokeTestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SmokeTestEntity toEntity(SmokeTestRequestDto dto);

    @Mapping(target = "externalData", ignore = true)
    @Mapping(target = "message", ignore = true)
    SmokeTestResponseDto toResponseDto(SmokeTestEntity entity);
}