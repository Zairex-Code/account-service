package com.nttdata.bootcamp.account_service.infrastructure.persistence.repository;

import com.nttdata.bootcamp.account_service.infrastructure.persistence.document.DebitCardDocument;
import org.springframework.data.repository.reactive.RxJava3CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Reactive Spring Data MongoDB repository for the 'debit_cards' collection.
 */
@Repository
public interface RxDebitCardRepository extends RxJava3CrudRepository<DebitCardDocument, String> {
}
