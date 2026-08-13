package com.nttdata.bootcamp.account_service.infrastructure.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Spring configuration class for reactive WebClient HTTP client beans.
 * <p>
 * Technical & Business Rules (NTT DATA / BCP Standards):
 * - Configures a Spring-managed {@link WebClient.Builder} bean for non-blocking HTTP calls.
 * - Applies {@link LoadBalanced} annotation to enable Netflix Eureka Service Discovery.
 * - Resolves virtual microservice target names (e.g., http://customer-service) to physical network IPs.
 * </p>
 *
 * @author NTT DATA Bootcamp Team
 * @version 1.0
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates and registers a load-balanced {@link WebClient.Builder} Bean in the Spring IoC context.
     *
     * @return A {@link WebClient.Builder} instance configured with Spring Cloud LoadBalancer capabilities.
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}