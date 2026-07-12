package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.ProviderDataHealthResponse;
import com.bkash.baymax.superagent_api.model.Agent;
import com.bkash.baymax.superagent_api.model.Provider;

import com.bkash.baymax.superagent_api.model.enums.ProviderDataHealthStatus;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.ProviderDataHealthRepository;
import com.bkash.baymax.superagent_api.repository.ProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderDataHealthServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-07-11T10:00:00Z");

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private ProviderDataHealthRepository
            providerDataHealthRepository;

    @Test
    void delayedStatusShouldTrackDelayAndLastSuccessfulUpdate() {
        Agent agent = Agent.builder()
                .id(1L)
                .agentCode("AGT-001")
                .displayName("Rahim Store")
                .area("Zindabazar")
                .district("Sylhet")
                .active(true)
                .build();

        Provider provider = Provider.builder()
                .id(1L)
                .providerCode("NAGAD")
                .displayName("Nagad")
                .active(true)
                .build();

        when(
                agentRepository.findByAgentCode("AGT-001")
        ).thenReturn(Optional.of(agent));

        when(
                providerRepository.findByProviderCode("NAGAD")
        ).thenReturn(Optional.of(provider));

        when(
                providerDataHealthRepository
                        .findByAgentAgentCodeAndProviderProviderCode(
                                "AGT-001",
                                "NAGAD"
                        )
        ).thenReturn(Optional.empty());

        when(
                providerDataHealthRepository.save(any())
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        Clock clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        ProviderDataHealthService service =
                new ProviderDataHealthService(
                        agentRepository,
                        providerRepository,
                        providerDataHealthRepository,
                        clock
                );

        ProviderDataHealthResponse response =
                service.updateSyntheticHealth(
                        "agt-001",
                        "nagad",
                        ProviderDataHealthStatus.DELAYED,
                        11,
                        null
                );

        assertEquals(
                ProviderDataHealthStatus.DELAYED,
                response.status()
        );

        assertEquals(
                11,
                response.delayMinutes()
        );

        assertEquals(
                NOW.minusSeconds(660),
                response.lastSuccessfulUpdateAt()
        );
    }
}
