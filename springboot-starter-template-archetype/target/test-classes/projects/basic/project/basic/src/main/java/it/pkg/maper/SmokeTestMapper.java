package it.pkg.maper;


import it.pkg.dto.SmokeTestRequestDto;
import it.pkg.dto.SmokeTestResponseDto;
import it.pkg.entity.SmokeTestEntity;
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