package ru.asander.health.servlets;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.servlets.MetricsServlet;
import io.prometheus.client.CollectorRegistry;
import ru.asander.health.exporters.MyDropwizardPrometheusExporter;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * ========== MyMetricsServletContextListener.java ==========
 * <p/>
 * $Revision:  $<br/>
 * $Author:  $<br/>
 * $HeadURL:  $<br/>
 * $Id:  $
 * <p/>
 * 11.07.2019 11:56: Original version (AAVolkov)<br/>
 */
public class MyMetricsServletContextListener extends MetricsServlet.ContextListener {
    private static final String CSV_REPORTER_PATH = "U:\\StreamFileService\\Metrics";   //todo to config

    private static final MetricRegistry metric_registry = SharedMetricRegistries.getOrCreate("default");

    static {
        //metric_registry.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        //metric_registry.register("jvm.cl", new ClassLoadingGaugeSet());
        //metric_registry.register("jvm.gc", new GarbageCollectorMetricSet());
        metric_registry.register("service.jvm.memory", new MemoryUsageGaugeSet());
        metric_registry.register("service.jvm.threads", new ThreadStatesGaugeSet());
        //metric_registry.register("ServiceHost.jvm.file.descriptor", new FileDescriptorRatioGauge());

        //Объявление вывода метрик в Prometheus формате.
        CollectorRegistry.defaultRegistry.register(new MyDropwizardPrometheusExporter(metric_registry));

        //Вывод метрик в консоль периодический
        final ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metric_registry).build();
        consoleReporter.start(5, TimeUnit.MINUTES);

        //Вывод метрик в файл периодический
        final CsvReporter csvReporter = CsvReporter.forRegistry(metric_registry)
                .formatFor(Locale.US)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(new File(CSV_REPORTER_PATH));
        csvReporter.start(2, TimeUnit.MINUTES);

        //Вывод в Logback логгер
       /* final Slf4jReporter reporter = Slf4jReporter.forRegistry(metric_registry)
                .outputTo(LoggerFactory.getLogger("ru.asander.metrics"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(3, TimeUnit.MINUTES);*/

    }

    @Override
    protected MetricRegistry getMetricRegistry() {
        return metric_registry;
    }
}
