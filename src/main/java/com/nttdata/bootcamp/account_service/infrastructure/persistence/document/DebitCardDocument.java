package com.nttdata.bootcamp.account_service.infrastructure.persistence.document;

import com.nttdata.bootcamp.account_service.domain.model.DebitCardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * MongoDB document representation for the 'debit_cards' collection.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "debit_cards")
public class DebitCardDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("card_number")
    private String cardNumber;

    @Indexed
    @Field("account_id")
    private String accountId;

    @Field("status")
    private DebitCardStatus status;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;
}
