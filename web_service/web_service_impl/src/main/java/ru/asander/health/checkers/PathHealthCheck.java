package ru.asander.health.checkers;

import com.codahale.metrics.health.HealthCheck;

import java.io.File;
import java.util.Optional;

/**
 * ========== WinDirHealthCheck.java ==========
 * <p/>
 * $Revision:  $<br/>
 * $Author:  $<br/>
 * $HeadURL:  $<br/>
 * $Id:  $
 * <p/>
 * 10.07.2019 16:55: Original version (AAVolkov)<br/>
 */
public class PathHealthCheck extends HealthCheck {

    private final File path;

    public PathHealthCheck(File path) {
        Optional.ofNullable(path).orElseThrow(() -> new IllegalArgumentException("Path value is null!"));
        this.path = path;
    }

    public PathHealthCheck(String path) {
        Optional.ofNullable(path).orElseThrow(() -> new IllegalArgumentException("Path value is null!"));
        this.path = new File(path);
    }

    @Override
    protected Result check() throws Exception {
        return (path.exists()) ?
                Result.healthy("Каталог %s - доступен!", path.getAbsolutePath()) :
                Result.unhealthy("Папка %s не существует", path.getAbsolutePath());
    }
}
