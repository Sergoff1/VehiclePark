package ru.lessons.my.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.lessons.my.model.entity.Trip;

@Repository
@RequiredArgsConstructor
public class ReactiveTripRepository {

    private final R2dbcEntityTemplate entityTemplate;

    public Mono<Trip> findById(Long id) {
        return entityTemplate.select(Trip.class)
                .matching(Query.query(Criteria.where("id").is(id)))
                .one();
    }
}
