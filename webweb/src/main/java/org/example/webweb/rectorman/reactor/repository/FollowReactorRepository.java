package org.example.webweb.rectorman.reactor.repository;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class FollowReactorRepository {
    private Map<String, Long> userFollowCountMap;

    public FollowReactorRepository() {
        userFollowCountMap = Map.of("1234", 1000L);
    }

    @SneakyThrows
    public CompletableFuture<Long> countByUserId(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("FollowRepository.countByUserId: {}", userId);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return userFollowCountMap.getOrDefault(userId, 0L);
        });
    }
}