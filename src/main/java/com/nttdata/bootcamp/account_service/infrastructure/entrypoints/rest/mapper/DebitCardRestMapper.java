package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.mapper;

import com.nttdata.bootcamp.account_service.domain.model.DebitCard;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.DebitCardResponseDto;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper interface for converting between debit card domain entities and REST DTOs.
 */
@Mapper(componentModel = "spring")
public interface DebitCardRestMapper {

    DebitCardResponseDto toResponseDto(DebitCard debitCard);
}
