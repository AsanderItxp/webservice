package ru.asander.health.checkers;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.annotation.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
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
 * 11.07.2019 10:37: Original version (AAVolkov)<br/>
 */
@Async(
        initialDelay = 10,
        unit = TimeUnit.SECONDS,
        scheduleType = Async.ScheduleType.FIXED_DELAY,
        period = 60,
        initialState = Async.InitialState.UNHEALTHY)
public class UrlHealthCheck extends HealthCheck {
    private static final Logger log = LoggerFactory.getLogger(UrlHealthCheck.class);
    private final String url;


    public UrlHealthCheck(String url) {
        Optional.ofNullable(url).orElseThrow(() -> new IllegalArgumentException("Url value is null!"));
        this.url = url;
    }

    @Override
    protected Result check() throws Exception {
        Result res = null;
        try (InputStream ignored = new URL(url).openStream()){
           log.debug("Stream for URL: {} can be opened!", url);
            res = Result.healthy("Адрес %s - доступен!", url);
        } catch (MalformedURLException e) {
            res = Result.unhealthy("Адрес сервиса '%s' не является ссылкой.", url);
        } catch (IOException e) {
            res = Result.unhealthy("Адрес сервиса '%s' не доступен. Exception: %s", url, e.getMessage());
        }
        return res;
    }
}
