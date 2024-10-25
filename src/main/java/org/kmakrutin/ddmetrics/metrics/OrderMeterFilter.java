package org.kmakrutin.ddmetrics.metrics;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class OrderMeterFilter implements MeterFilter {
    private final OrderMetricProperties orderMetricProperties;

    public OrderMeterFilter(OrderMetricProperties orderMetricProperties) {
        this.orderMetricProperties = orderMetricProperties;
    }

    @Override
    public MeterFilterReply accept(Meter.Id id) {
        final String metricName = id.getName();
        final Set<Tag> includeTags = orderMetricProperties.getIncludeTags(metricName);

        // if include is not empty, then keep only included
        if (includeTags != null && !includeTags.isEmpty()) {
            if (id.getTags().stream().anyMatch(includeTags::contains)) {
                return MeterFilterReply.ACCEPT;
            }

            return MeterFilterReply.DENY;
        }

        final Set<Tag> exclude = orderMetricProperties.getExcludeTags(metricName);
        if (exclude != null && !exclude.isEmpty() && id.getTags().stream().anyMatch(exclude::contains)) {
            return MeterFilterReply.DENY;
        }

        return MeterFilter.super.accept(id);
    }
}
