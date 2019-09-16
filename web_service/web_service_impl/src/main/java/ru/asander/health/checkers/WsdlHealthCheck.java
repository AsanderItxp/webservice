package ru.asander.health.checkers;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.annotation.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * ========== WsdlHealthCheck.java ==========
 * <p/>
 * $Revision:  $<br/>
 * $Author:  $<br/>
 * $HeadURL:  $<br/>
 * $Id:  $
 * <p/>
 * 11.07.2019 10:46: Original version (AAVolkov)<br/>
 */
@Async(
        initialDelay = 10,
        unit = TimeUnit.SECONDS,
        scheduleType = Async.ScheduleType.FIXED_DELAY,
        period = 60,
        initialState = Async.InitialState.UNHEALTHY)
public class WsdlHealthCheck extends HealthCheck {
    private static final Logger log = LoggerFactory.getLogger(WsdlHealthCheck.class);
    private final String wsdlEndpoint;
    private final QName qname;

    public WsdlHealthCheck(String wsdlEndpoint, QName serviceQName) {
        Optional.ofNullable(wsdlEndpoint).orElseThrow(() -> new IllegalArgumentException("WsdlEndpoint value is null!"));
        Optional.ofNullable(serviceQName).orElseThrow(() -> new IllegalArgumentException("serviceQName value is null!"));
        this.wsdlEndpoint = wsdlEndpoint;
        this.qname = serviceQName;
    }

    protected Result check() throws Exception {
        Result res = null;
        try {
            Service.create(new URL(wsdlEndpoint), qname);
            log.debug("Web-Service for endpoint: {}  and QName: {} can be created!", wsdlEndpoint, qname);
            res = Result.healthy("WSDL адрес %s - доступен!", wsdlEndpoint);
        } catch (MalformedURLException e) {
            res = Result.unhealthy("Адрес сервиса '%s' не является ссылкой. '%s'", wsdlEndpoint);
        } catch (WebServiceException wex) {
            res = Result.unhealthy("Сервис не может быть создан для endpoint: '%s' и QName: '%s'. Exception: %s", wsdlEndpoint, qname, wex.getMessage());
        }
        return res;
    }

}
