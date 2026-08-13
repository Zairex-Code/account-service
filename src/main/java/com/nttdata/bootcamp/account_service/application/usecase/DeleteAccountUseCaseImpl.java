package com.nttdata.bootcamp.account_service.application.usecase;

import com.nttdata.bootcamp.account_service.domain.port.input.DeleteAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service implementation of the {@link DeleteAccountUseCase} input port.
 * <p>
 * Technical & Business Rules (NTT DATA / BCP Standards):
 * - Validates existing account presence prior to deletion.
 * - Verifies that the account balance is strictly zero before allowing account closure.
 * - Prevents account deletion if remaining monetary balance exists in the account.
 * - Delegates reactive non-blocking deletion to {@link AccountPersistencePort}.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteAccountUseCaseImpl implements DeleteAccountUseCase {
    private final AccountPersistencePort accountPersistencePort;

    /**
     * Executes the reactive business orchestration for deleting or closing a bank account.
     *
     * @param id Unique primary database identifier of the account to delete.
     * @return A {@link Mono} emitting void upon successful completion.
     * @throws IllegalArgumentException If the account ID is invalid or not found.
     * @throws IllegalStateException    If the account holds a non-zero balance.
     */
    @Override
    public Mono<Void> execute(String id) {
        log.info("Initiating account closure workflow for account ID: {}", id);

        if (id == null || id.isBlank()) {
            return Mono.error(new IllegalArgumentException("Account ID cannot be null or blank for deletion"));
        }

        return accountPersistencePort.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Cannot delete account: Account with ID '" + id + "' was not found")))
                .flatMap(existingAccount -> {
                    if (existingAccount.getBalance() != null && existingAccount.getBalance() > 0) {
                        log.error("Account closure rejected: Account ID '{}' has remaining balance of {}",
                                id, existingAccount.getBalance());
                        return Mono.error(new IllegalStateException(
                                "Cannot close account: Remaining balance is " + existingAccount.getBalance()
                                        + ". Balance must be exactly 0.0 before closure."));
                    }
                    log.debug("Account ID '{}' balance validated (0.0). Proceeding with deletion", id);
                    return accountPersistencePort.deleteById(id);
                })
                .doOnSuccess(v -> log.info("Account ID '{}' closed and deleted successfully", id))
                .doOnError(error -> log.error("Failed to delete account ID '{}'. Error: {}", id, error.getMessage()));
    }
}
