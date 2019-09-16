package ru.asander.health.servlets;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.servlet.InstrumentedFilterContextListener;

/**
 * ========== MyInstrumentedFilterContextListener.java ==========
 * <p/>
 * $Revision:  $<br/>
 * $Author:  $<br/>
 * $HeadURL:  $<br/>
 * $Id:  $
 * <p/>
 * 11.07.2019 12:03: Original version (AAVolkov)<br/>
 */
public class MyInstrumentedFilterContextListener
        extends InstrumentedFilterContextListener {

    private static MetricRegistry registry = SharedMetricRegistries.getOrCreate("default");

    @Override
    protected MetricRegistry getMetricRegistry() {
        return registry;
    }
}
