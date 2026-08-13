package com.nttdata.bootcamp.account_service.infrastructure.persistence.mapper;


import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.infrastructure.persistence.document.AccountDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountPersistenceMapper {

    /**
     * Converts a pure domain {@link Account} entity into a MongoDB {@link AccountDocument}.
     *
     * @param account Domain model instance containing financial logic and attributes.
     * @return Converted {@link AccountDocument} for NoSQL persistence.
     */
    AccountDocument toDocument(Account account);


    /**
     * Converts a MongoDB {@link AccountDocument} into a pure domain {@link Account} entity.
     *
     * @param document MongoDB document retrieved from storage.
     * @return Converted immutable {@link Account} domain entity.
     */
    Account toDomain(AccountDocument document);

}
