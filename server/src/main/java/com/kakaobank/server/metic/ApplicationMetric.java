package com.kakaobank.server.metic;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.JvmAttributeGaugeSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;

public class ApplicationMetric {

	private final MetricRegistry metricRegistry;

	public ApplicationMetric(MetricRegistry metricRegistry) {
		this.metricRegistry = metricRegistry;

		initialize();
		initReporters();
	}

	private void initialize() {
		metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
		metricRegistry.register("jvm.vm", new JvmAttributeGaugeSet());
		metricRegistry.register("jvm.garbage-collectors", new GarbageCollectorMetricSet());
		metricRegistry.register("jvm.thread-states", new ThreadStatesGaugeSet());
	}

	private void initReporters() {
		Slf4jReporter slf4jReporter = createSlf4jReporter();
		slf4jReporter.start(60, TimeUnit.SECONDS);
	}

	private Slf4jReporter createSlf4jReporter() {
		Slf4jReporter.Builder builder = Slf4jReporter.forRegistry(metricRegistry);
		builder.convertRatesTo(TimeUnit.SECONDS);
		builder.convertDurationsTo(TimeUnit.MILLISECONDS);

		builder.outputTo(LoggerFactory.getLogger(ApplicationMetric.class));
		return builder.build();
	}
}
