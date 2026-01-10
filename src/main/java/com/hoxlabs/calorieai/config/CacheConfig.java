package com.hoxlabs.calorieai.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String NUTRITION_CACHE = "nutritionCache";
    public static final String DAILY_SUMMARY = "dailySummary";
    public static final String MEAL_HISTORY = "mealHistory";
    public static final String USER_PROFILE = "userProfile";

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .initialCapacity(10)
                .maximumSize(100)
                .recordStats();
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(NUTRITION_CACHE, DAILY_SUMMARY, MEAL_HISTORY, USER_PROFILE);
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }
}
