package ru.lessons.my.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.lessons.my.model.entity.Vehicle;

@Repository
@RequiredArgsConstructor
public class ReactiveVehicleRepository {

    private final R2dbcEntityTemplate entityTemplate;

    public Mono<Vehicle> findById(Long id) {
        return entityTemplate.select(Vehicle.class)
                .matching(Query.query(Criteria.where("id").is(id)))
                .one();
    }
}
