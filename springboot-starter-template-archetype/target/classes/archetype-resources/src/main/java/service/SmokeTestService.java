#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.service;


import ${package}.client.http.servicea.ExternalApiResponseDto;
import ${package}.client.http.servicea.ExternalApiClient;
import ${package}.constants.AppConstants;
import ${package}.dto.SmokeTestRequestDto;
import ${package}.dto.SmokeTestResponseDto;
import ${package}.entity.SmokeTestEntity;
import ${package}.maper.SmokeTestMapper;
import ${package}.repository.SmokeTestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SmokeTestService {
    
    private static final Logger logger = LoggerFactory.getLogger(SmokeTestService.class);
    
    private final SmokeTestRepository repository;
    private final SmokeTestMapper mapper;
    private final ExternalApiClient externalApiService;
    
    public SmokeTestService(SmokeTestRepository repository, 
                           SmokeTestMapper mapper,
                           ExternalApiClient externalApiService) {
        this.repository = repository;
        this.mapper = mapper;
        this.externalApiService = externalApiService;
    }

    public Flux<SmokeTestResponseDto> getActiveSmokeTests() {
        logger.info("Fetching all active smoke test records");

        return repository.findByStatus(AppConstants.DEFAULT_STATUS)
                .map(entity -> {
                    SmokeTestResponseDto baseResponse = mapper.toResponseDto(entity);
                    return new SmokeTestResponseDto(
                            baseResponse.id(),
                            baseResponse.name(),
                            baseResponse.description(),
                            baseResponse.status(),
                            null, // No external data for list view
                            baseResponse.createdAt(),
                            "Active record retrieved"
                    );
                })
                .doOnComplete(() -> logger.info("Successfully fetched all active smoke test records"));
    }

    
    public Mono<SmokeTestResponseDto> processSmokeTest(SmokeTestRequestDto requestDto) {
        logger.info("Processing smoke test for: {}", requestDto.name());
        
        return Mono.fromCallable(() -> {
                    SmokeTestEntity entity = mapper.toEntity(requestDto);
                    entity.setStatus(AppConstants.DEFAULT_STATUS);
                    return entity;
                })
                .flatMap(repository::save)
                .flatMap(savedEntity -> 
                    externalApiService.fetchExternalData()
                        .map(externalData -> createResponseDto(savedEntity, externalData))
                        .defaultIfEmpty(createResponseDto(savedEntity, null))
                )
                .doOnSuccess(response -> logger.info("Smoke test processed successfully: {}", response.id()));
    }
    
    private SmokeTestResponseDto createResponseDto(SmokeTestEntity entity, ExternalApiResponseDto externalData) {
        SmokeTestResponseDto baseResponse = mapper.toResponseDto(entity);
        return new SmokeTestResponseDto(
            baseResponse.id(),
            baseResponse.name(),
            baseResponse.description(),
            baseResponse.status(),
            externalData,
            baseResponse.createdAt(),
            AppConstants.SUCCESS_MESSAGE
        );
    }
}