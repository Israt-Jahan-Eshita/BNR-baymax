package com.bkash.baymax.superagent_api.config;

import com.bkash.baymax.superagent_api.service.DemoDataSeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DemoDataInitializer implements ApplicationRunner {

    private final DemoDataSeedService demoDataSeedService;

    @Override
    public void run(ApplicationArguments args) {
        demoDataSeedService.seedBaselineIfMissing();
    }
}
