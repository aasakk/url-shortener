
package com.example.shortener.config;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Configuration
public class MetricsConfig {
    @Bean
    public GraphiteReporter graphiteReporter(MetricRegistry metricRegistry) {
        Graphite graphite = new Graphite(new InetSocketAddress("localhost", 2003));
        GraphiteReporter reporter = GraphiteReporter.forRegistry(metricRegistry)
                .prefixedWith("shortener.metrics")
                .build(graphite);
        reporter.start(10, TimeUnit.SECONDS);
        return reporter;
    }

    @Bean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }
}
