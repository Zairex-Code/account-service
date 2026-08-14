package com.nttdata.bootcamp.account_service.infrastructure.persistence.mapper;

import com.nttdata.bootcamp.account_service.domain.model.DebitCard;
import com.nttdata.bootcamp.account_service.infrastructure.persistence.document.DebitCardDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DebitCardPersistenceMapper {

    DebitCardDocument toDocument(DebitCard debitCard);

    DebitCard toDomain(DebitCardDocument document);
}
