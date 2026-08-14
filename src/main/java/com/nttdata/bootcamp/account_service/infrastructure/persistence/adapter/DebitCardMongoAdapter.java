package com.nttdata.bootcamp.account_service.infrastructure.persistence.adapter;

import com.nttdata.bootcamp.account_service.domain.model.DebitCard;
import com.nttdata.bootcamp.account_service.domain.port.output.DebitCardPersistencePort;
import com.nttdata.bootcamp.account_service.infrastructure.persistence.mapper.DebitCardPersistenceMapper;
import com.nttdata.bootcamp.account_service.infrastructure.persistence.repository.RxDebitCardRepository;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Persistence adapter implementation for {@link DebitCardPersistencePort} interfacing with MongoDB.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DebitCardMongoAdapter implements DebitCardPersistencePort {

    private final RxDebitCardRepository repository;
    private final DebitCardPersistenceMapper mapper;

    @Override
    public Single<DebitCard> save(DebitCard debitCard) {
        log.debug("Adapting domain DebitCard to Document for persistence. Account ID: {}",
                debitCard.getAccountId());
        return Single.just(debitCard)
                .map(mapper::toDocument)
                .flatMap(repository::save)
                .map(mapper::toDomain);
    }

    @Override
    public Maybe<DebitCard> findById(String id) {
        log.debug("Finding debit card document in MongoDB by ID: {}", id);
        return repository.findById(id)
                .map(mapper::toDomain);
    }
}
