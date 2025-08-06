#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.repository;


import ${package}.entity.SmokeTestEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SmokeTestRepository extends R2dbcRepository<SmokeTestEntity, Long> {
    
    Flux<SmokeTestEntity> findByStatus(String status);
    
    Mono<SmokeTestEntity> findByName(String name);
    
    @Query("SELECT * FROM smoke_test WHERE name ILIKE '%' || :name || '%'")
    Flux<SmokeTestEntity> findByNameContaining(String name);
    
    @Query("SELECT COUNT(*) FROM smoke_test WHERE status = :status")
    Mono<Long> countByStatus(String status);
}