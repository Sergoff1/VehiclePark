package ru.lessons.my.controller.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lessons.my.service.CacheService;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/cache")
@RequiredArgsConstructor
public class CacheRestController {

    private final CacheService cacheService;

    @GetMapping("/clean")
    public void cleanCache() {
        cacheService.cleanCache();
    }
}
