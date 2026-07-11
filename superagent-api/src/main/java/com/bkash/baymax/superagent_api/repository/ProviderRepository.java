package com.bkash.baymax.superagent_api.repository;

import com.bkash.baymax.superagent_api.model.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProviderRepository
        extends JpaRepository<Provider, Long> {

    Optional<Provider> findByProviderCode(String providerCode);

    boolean existsByProviderCode(String providerCode);

    List<Provider> findAllByActiveTrueOrderByDisplayNameAsc();
}
