package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.mapper;


import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.AccountRequestDto;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.AccountResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper interface for converting between REST HTTP DTOs
 * and pure domain {@link Account} entities.
 * <p>
 * Technical & Business Rules (NTT DATA / BCP Standards):
 * - Generates high-performance mapping code at compile-time to eliminate reflection.
 * - Decouples public HTTP API contracts from internal core domain representations.
 * - Explicitly ignores system-generated audit and business attributes during request mapping.
 * - Configured with Spring component model for seamless dependency injection.
 * </p>

 * @author NTT DATA Bootcamp Team
 * @version 1.0
 */
@Mapper(componentModel = "spring")
public interface AccountRestMapper {

    /**
     * Converts an incoming {@link AccountRequestDto} payload into a pure domain {@link Account} entity.
     * <p>
     * System fields like id, status, fees, transaction counters, and audit timestamps are intentionally
     * ignored because they must be populated strictly by domain business logic during account opening.
     * </p>
     *
     * @param dto Incoming HTTP request body payload.
     * @return Converted immutable {@link Account} domain model.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "maintenanceFee", ignore = true)
    @Mapping(target = "maxMonthlyTransactions", ignore = true)
    @Mapping(target = "currentMonthlyTransactions", ignore = true)
    @Mapping(target = "allowedTransactionDay", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Account toDomain(AccountRequestDto dto);

    /**
     * Converts a pure domain {@link Account} entity into an outbound {@link AccountResponseDto} REST payload.
     *
     * @param account Core domain account instance.
     * @return Converted {@link AccountResponseDto} for REST HTTP response body.
     */
    AccountResponseDto toResponseDto(Account account);
}