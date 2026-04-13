package com.budgetpro.infrastructure.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class EvmMetrics {

    public static final String METRIC_EVM_PROGRESS_REGISTERED_COUNT = "evm.progress.registered.count";

    private final MeterRegistry registry;

    public EvmMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void progressRegistered() {
        registry.counter(METRIC_EVM_PROGRESS_REGISTERED_COUNT).increment();
    }
}

