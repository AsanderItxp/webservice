package ru.asander.health.exporters;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import io.prometheus.client.dropwizard.DropwizardExports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * ========== MyPrometheusExporter.java ==========
 * <p/>
 * $Revision:  $<br/>
 * $Author:  $<br/>
 * $HeadURL:  $<br/>
 * $Id:  $
 * <p/>
 * 19.07.2019 10:27: Original version (AAVolkov)<br/>
 */
public class MyDropwizardPrometheusExporter extends DropwizardExports {
    private static final Logger log = LoggerFactory.getLogger(MyDropwizardPrometheusExporter.class);

    private static final String Q_NAME = "quantile";
    private static final String Q999 = "0.999";
    private static final String Q99 = "0.99";
    private static final String Q98 = "0.98";
    private static final String Q95 = "0.95";
    private static final String Q75 = "0.75";
    private static final String Q5 = "0.5";

    private static final String TOTAL = "_total";
    private static final String COUNT = "_count";
    private static final String MEAN = "_mean";
    private static final String MIN = "_min";
    private static final String MAX = "_max";
    private static final String STDDEV = "_stddev";
    private static final String RATE_15M = "_m15_rate";
    private static final String RATE_5M = "_m5_rate";
    private static final String RATE_1M = "_m1_rate";
    private static final String RATE_MEAN = "_mean_rate";


    /**
     * @param registry a metric registry to export in prometheus.
     */
    private MetricRegistry registry;

    public MyDropwizardPrometheusExporter(MetricRegistry registry) {
        super(registry);
        this.registry = registry;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        ArrayList<MetricFamilySamples> mfSamples = new ArrayList<>();
        for (SortedMap.Entry<String, Gauge> entry : registry.getGauges().entrySet()) {
            mfSamples.addAll(fromGauge(entry.getKey(), entry.getValue()));
        }
        for (SortedMap.Entry<String, Counter> entry : registry.getCounters().entrySet()) {
            mfSamples.addAll(fromCounter(entry.getKey(), entry.getValue()));
        }
        for (SortedMap.Entry<String, Histogram> entry : registry.getHistograms().entrySet()) {
            mfSamples.addAll(fromHistogram(entry.getKey(), entry.getValue()));
        }
        for (SortedMap.Entry<String, Timer> entry : registry.getTimers().entrySet()) {
            mfSamples.addAll(fromTimer(entry.getKey(), entry.getValue()));
        }
        for (SortedMap.Entry<String, Meter> entry : registry.getMeters().entrySet()) {
            mfSamples.addAll(fromMeter(entry.getKey(), entry.getValue()));
        }
        return mfSamples;
    }


    /**
     * Convert histogram snapshot.
     */
    private List<MetricFamilySamples> fromHistogram(String dropwizardName, Histogram histogram) {
        return fromSnapshotAndCount(dropwizardName, histogram.getSnapshot(), histogram.getCount(), 1.0,
                getHelpMessage(dropwizardName, histogram));
    }

    /**
     * Export Dropwizard Timer as a histogram. Use TIME_UNIT as time unit.
     */
    private List<MetricFamilySamples> fromTimer(String dropwizardName, Timer timer) {
        return fromSnapshotAndCount(dropwizardName, timer.getSnapshot(), timer.getCount(),
                1.0D / TimeUnit.SECONDS.toNanos(1L), getHelpMessage(dropwizardName, timer));
    }

    /**
     * Export counter as Prometheus <a href="https://prometheus.io/docs/concepts/metric_types/#gauge">Gauge</a>.
     */
    private List<MetricFamilySamples> fromCounter(String dropwizardName, Counter counter) {
        String name = sanitizeMetricName(dropwizardName);
        MetricFamilySamples.Sample sample = new MetricFamilySamples.Sample(name, new ArrayList<>(), new ArrayList<>(),
                ((Long) counter.getCount()).doubleValue());
        return Collections.singletonList(new MetricFamilySamples(name, Type.GAUGE, getHelpMessage(dropwizardName, counter), Collections.singletonList(sample)));
    }

    /**
     * Export gauge as a prometheus gauge.
     */
    private List<MetricFamilySamples> fromGauge(String dropwizardName, Gauge gauge) {
        String name = sanitizeMetricName(dropwizardName);
        Object obj = gauge.getValue();
        double value;
        if (obj instanceof Number) {
            value = ((Number) obj).doubleValue();
        } else if (obj instanceof Boolean) {
            value = ((Boolean) obj) ? 1 : 0;
        } else {
            log.trace("Invalid type for Gauge {}: {}", name, obj.getClass().getName());
            return new ArrayList<>();
        }
        MetricFamilySamples.Sample sample = new MetricFamilySamples.Sample(name,
                new ArrayList<>(), new ArrayList<>(), value);
        return Collections.singletonList(new MetricFamilySamples(name, Type.GAUGE, getHelpMessage(dropwizardName, gauge), Collections.singletonList(sample)));
    }


    /**
     * Export a Meter as as prometheus COUNTER.
     */
    private List<MetricFamilySamples> fromMeter(String dropwizardName, Meter meter) {
        String name = sanitizeMetricName(dropwizardName);
        return Arrays.asList(
                new MetricFamilySamples(name + TOTAL, Type.COUNTER, getHelpMessage(dropwizardName, meter), Collections.singletonList(
                        new MetricFamilySamples.Sample(name + TOTAL,
                                new ArrayList<>(),
                                new ArrayList<>(),
                                meter.getCount()))),
                new MetricFamilySamples(name + RATE_1M, Type.COUNTER, getHelpMessage(dropwizardName, meter), Collections.singletonList(
                        new MetricFamilySamples.Sample(name + RATE_1M,
                                new ArrayList<>(),
                                new ArrayList<>(),
                                meter.getOneMinuteRate()))),
                new MetricFamilySamples(name + RATE_5M, Type.COUNTER, getHelpMessage(dropwizardName, meter), Collections.singletonList(
                        new MetricFamilySamples.Sample(name + RATE_5M,
                                new ArrayList<>(),
                                new ArrayList<>(),
                                meter.getFiveMinuteRate()))),
                new MetricFamilySamples(name + RATE_15M, Type.COUNTER, getHelpMessage(dropwizardName, meter), Collections.singletonList(
                        new MetricFamilySamples.Sample(name + RATE_15M,
                                new ArrayList<>(),
                                new ArrayList<>(),
                                meter.getFifteenMinuteRate()))),
                new MetricFamilySamples(name + RATE_MEAN, Type.COUNTER, getHelpMessage(dropwizardName, meter), Collections.singletonList(
                        new MetricFamilySamples.Sample(name + RATE_MEAN,
                                new ArrayList<>(),
                                new ArrayList<>(),
                                meter.getMeanRate())))

        );
    }

    private List<MetricFamilySamples> fromSnapshotAndCount(String dropwizardName, Snapshot snapshot, long count, double factor, String helpMessage) {
        String name = sanitizeMetricName(dropwizardName);
        List<MetricFamilySamples.Sample> samples = Arrays.asList(
                new MetricFamilySamples.Sample(name, Collections.singletonList(Q_NAME), Collections.singletonList(Q5), snapshot.getMedian() * factor),
                new MetricFamilySamples.Sample(name, Collections.singletonList(Q_NAME), Collections.singletonList(Q75), snapshot.get75thPercentile() * factor),
                new MetricFamilySamples.Sample(name, Collections.singletonList(Q_NAME), Collections.singletonList(Q95), snapshot.get95thPercentile() * factor),
                new MetricFamilySamples.Sample(name, Collections.singletonList(Q_NAME), Collections.singletonList(Q98), snapshot.get98thPercentile() * factor),
                new MetricFamilySamples.Sample(name, Collections.singletonList(Q_NAME), Collections.singletonList(Q99), snapshot.get99thPercentile() * factor),
                new MetricFamilySamples.Sample(name, Collections.singletonList(Q_NAME), Collections.singletonList(Q999), snapshot.get999thPercentile() * factor),
                new MetricFamilySamples.Sample(name + MIN, new ArrayList<>(), new ArrayList<>(), snapshot.getMin() * factor),
                new MetricFamilySamples.Sample(name + MAX, new ArrayList<>(), new ArrayList<>(), snapshot.getMax() * factor),
                new MetricFamilySamples.Sample(name + MEAN, new ArrayList<>(), new ArrayList<>(), snapshot.getMean() * factor),
                new MetricFamilySamples.Sample(name + STDDEV, new ArrayList<>(), new ArrayList<>(), snapshot.getStdDev() * factor),
                new MetricFamilySamples.Sample(name + COUNT, new ArrayList<>(), new ArrayList<>(), count)
        );
        return Collections.singletonList(
                new MetricFamilySamples(name, Type.SUMMARY, helpMessage, samples)
        );
    }

    private static String getHelpMessage(String metricName, Metric metric) {
        return String.format("Generated from Asander metric import (metric=%s, type=%s)",
                metricName, metric.getClass().getName());
    }
}
