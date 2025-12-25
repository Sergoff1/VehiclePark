package ru.lessons.my.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CacheService {

    @CacheEvict(allEntries = true, cacheNames = {"geoPointsByTrips", "vehiclesByManager"})
    public void cleanCache() {
        log.info("Clean cache");
    }
}
