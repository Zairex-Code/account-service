package com.nttdata.bootcamp.account_service.infrastructure.persistence.document;


import com.nttdata.bootcamp.account_service.domain.model.AccountStatus;
import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;


/**
 * MongoDB document representation for the 'accounts' collection within the reactive persistence layer.
 * <p>
 * Technical & Business Rules:
 * - Maps database records directly to the 'accounts' collection in MongoDB (account_db).
 * - Enforces unique B-Tree indexing on the 'account_number' field for fast searches.
 * - Decouples NoSQL mapping annotations (@Document, @Id, @Field) from pure domain entities.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "accounts")
public class AccountDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    @Field("account_number")
    private String accountNumber;

    @Indexed
    @Field("customer_id")
    private String customerId;


    @Field("type")
    private AccountType type;

    @Field("status")
    private AccountStatus status;

    @Field("balance")
    private Double balance;


    @Field("maintenance_fee")
    private Double maintenanceFee;


    @Field("max_monthly_transactions")
    private Integer maxMonthlyTransactions;


    @Field("current_monthly_transactions")
    private Integer currentMonthlyTransactions;


    @Field("allowed_transaction_day")
    private Integer allowedTransactionDay;


    @Field("transaction_commission")
    private Double transactionCommission;


    @Field("holders")
    private List<String> holders;


    @Field("signatories")
    private List<String> signatories;


    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;
}
