package ru.asander.health.servlets;

import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.SharedHealthCheckRegistries;
import com.codahale.metrics.servlets.HealthCheckServlet;
import ru.asander.health.checkers.PathHealthCheck;
import ru.asander.health.checkers.UrlHealthCheck;
import ru.asander.health.checkers.WsdlHealthCheck;

import javax.xml.namespace.QName;

/**
 * ========== MyHealthCheckServletContextListener.java ==========
 * <p/>
 * $Revision:  $<br/>
 * $Author:  $<br/>
 * $HeadURL:  $<br/>
 * $Id:  $
 * <p/>
 * 11.07.2019 11:49: Original version (AAVolkov)<br/>
 */
public class MyHealthCheckServletContextListener extends HealthCheckServlet.ContextListener {

    private static HealthCheckRegistry healthCheckRegistry = SharedHealthCheckRegistries.getOrCreate("default");

    static {
        healthCheckRegistry.register("Path_to_save_content_checker", new PathHealthCheck("U:\\temp")); //TODO to config
        healthCheckRegistry.register("Url_service_checker", new UrlHealthCheck("http://localhost:8080/test/StreamFileService?wsdl"));   //TODO to config
        healthCheckRegistry.register("Consul_service_checker", new UrlHealthCheck("http://127.0.0.1:8500/ui"));    //TODO to config
        healthCheckRegistry.register("WSDL_stream_service_checker",
                new WsdlHealthCheck(
                        "http://localhost:8080/test/StreamFileService?wsdl",    //TODO to config
                        new QName("http://www.asander.ru/ws/service/", "StreamFileService")));    //TODO to config

        healthCheckRegistry.register("JVM_thread_deadlock_checker", new ThreadDeadlockHealthCheck());
        healthCheckRegistry.runHealthChecks();
    }

    @Override
    protected HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry;
    }
}
