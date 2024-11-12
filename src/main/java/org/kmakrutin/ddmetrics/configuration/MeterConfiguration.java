package org.kmakrutin.ddmetrics.configuration;

import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MeterConfiguration {
    @ConditionalOnProperty(name = "metrics.tag.ignore.city", havingValue = "true")
    @Bean
    public MeterRegistryCustomizer<?> meterRegistryCustomizer() {
        return registry -> registry.config()
                .meterFilter(MeterFilter.ignoreTags("city"));
    }

    @Bean
    public MeterRegistryCustomizer<?> commonTags() {
        return registry -> registry.config()
                .commonTags("env", "local", "service", "orders");
    }
}
