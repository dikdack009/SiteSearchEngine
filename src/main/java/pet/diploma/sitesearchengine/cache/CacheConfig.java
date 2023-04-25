package pet.diploma.sitesearchengine.cache;

import com.google.common.cache.CacheBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {
    @Bean("CacheManager")
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        ConcurrentMapCache statisticCache = new ConcurrentMapCache("statistic",  CacheBuilder.newBuilder()
                .expireAfterWrite(15, TimeUnit.SECONDS)
                .build().asMap(),
                false);
        ConcurrentMapCache searchCache = new ConcurrentMapCache("search",  CacheBuilder.newBuilder()
                .expireAfterWrite(3, TimeUnit.HOURS)
                .build().asMap(),
                false);
        cacheManager.setCaches(Arrays.asList(statisticCache, searchCache));
        return cacheManager;
    }
}
