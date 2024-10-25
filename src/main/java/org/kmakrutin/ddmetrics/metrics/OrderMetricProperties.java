package org.kmakrutin.ddmetrics.metrics;

import io.micrometer.core.instrument.Tag;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Component
@ConfigurationProperties(prefix = "order.metrics")
public class OrderMetricProperties {
    private final Map<String, Set<Tag>> include = new HashMap<>();
    private final Map<String, Set<Tag>> exclude = new HashMap<>();

    @PostConstruct
    public void init() {
        log.info("Include metrics: {}", include);
        log.info("Exclude metrics: {}", exclude);
    }

    public void setExclude(Map<String, Map<String, List<String>>> exclude) {
        exclude.forEach((metricId, tagValues) -> {
            final Set<Tag> tags = tagValues.entrySet().stream()
                    .flatMap(tag -> tag.getValue().stream().map(tagValue -> Tag.of(tag.getKey(), tagValue.toLowerCase())))
                    .collect(Collectors.toSet());
            this.exclude.put(metricId, tags);
        });
    }

    public void setInclude(Map<String, Map<String, List<String>>> include) {
        include.forEach((metricId, tagValues) -> {
            final Set<Tag> tags = tagValues.entrySet().stream()
                    .flatMap(tag -> tag.getValue().stream().map(tagValue -> Tag.of(tag.getKey(), tagValue.toLowerCase())))
                    .collect(Collectors.toSet());
            this.include.put(metricId, tags);
        });
    }

    public Set<Tag> getIncludeTags(String metricName) {
        return include.get(metricName);
    }

    public Set<Tag> getExcludeTags(String metricName) {
        return exclude.get(metricName);
    }
}
