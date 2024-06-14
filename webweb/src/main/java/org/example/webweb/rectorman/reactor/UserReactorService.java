package org.example.webweb.rectorman.reactor;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.webweb.rectorman.common.Article;
import org.example.webweb.rectorman.common.Image;
import org.example.webweb.rectorman.common.User;
import org.example.webweb.rectorman.common.repository.UserEntity;
import org.example.webweb.rectorman.reactor.repository.ArticleReactorRepository;
import org.example.webweb.rectorman.reactor.repository.FollowReactorRepository;
import org.example.webweb.rectorman.reactor.repository.ImageReactorRepository;
import org.example.webweb.rectorman.reactor.repository.UserReactorRepository;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class UserReactorService {
    private final ArticleReactorRepository articleReactorRepository;
    private final FollowReactorRepository followReactorRepository;
    private final ImageReactorRepository imageReactorRepository;
    private final UserReactorRepository userReactorRepository;


    @SneakyThrows
    public Mono<User> getUserById(String id ){
        return userReactorRepository.findById(id).flatMap(userEntity ->{
            return Mono.fromFuture(this.getUser(Optional.of(userEntity)));
        }).map(userOptional -> userOptional.get());
    }
    @SneakyThrows
    private CompletableFuture<Optional<User>> getUser(Optional<UserEntity> userEntityOptional) {
        if (userEntityOptional.isEmpty()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        var userEntity = userEntityOptional.get();

        var imageFuture = imageReactorRepository.findById(userEntity.getProfileImageId())
                .thenApplyAsync(imageEntityOptional ->
                        imageEntityOptional.map(imageEntity ->
                                new Image(imageEntity.getId(), imageEntity.getName(), imageEntity.getUrl())
                        )
                );


        var articlesFuture = articleReactorRepository.findAllByUserId(userEntity.getId())
                .thenApplyAsync(articleEntities ->
                        articleEntities.stream()
                                .map(articleEntity ->
                                        new Article(articleEntity.getId(), articleEntity.getTitle(), articleEntity.getContent())
                                )
                                .collect(Collectors.toList())
                );

        var followCountFuture = followReactorRepository.countByUserId(userEntity.getId());

        return CompletableFuture.allOf(imageFuture, articlesFuture, followCountFuture)
                .thenAcceptAsync(v -> {
                    log.info("Three futures are completed");
                })
                .thenRunAsync(() -> {
                    log.info("Three futures are also completed");
                })
                .thenApplyAsync(v -> {
                    try {
                        var image = imageFuture.get();
                        var articles = articlesFuture.get();
                        var followCount = followCountFuture.get();

                        return Optional.of(
                                new User(
                                        userEntity.getId(),
                                        userEntity.getName(),
                                        userEntity.getAge(),
                                        image,
                                        articles,
                                        followCount
                                )
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }



}
