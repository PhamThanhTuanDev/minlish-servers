package com.minlish.config;


import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.TimeZone;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 15:42
 * File      : AppConfig
 */
@Configuration
@EnableAsync
@EnableCaching
public class AppConfig {

    @PostConstruct
    public void initTimezone() {
        // Keep application timestamps and scheduler logic aligned with Vietnam time.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    /**
     * Mã hóa mật khẩu dùng BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "unreadNotificationCounts",
                "statsSummaries"
        );
    }
}