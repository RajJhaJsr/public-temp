#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.scheduler;


import ${package}.constants.AppConstants;
import ${package}.repository.SmokeTestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//@Component
public class ScheduledTasks {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    
    private final SmokeTestRepository repository;
    
    public ScheduledTasks(SmokeTestRepository repository) {
        this.repository = repository;
    }

    //@Scheduled(cron = "0 0 * * * ?") // Every hour
    //@Scheduled(fixedRate = 300000) // Every 5 minutes
    public void reportActiveRecords() {
        repository.countByStatus(AppConstants.DEFAULT_STATUS)
                .subscribe(count -> 
                    logger.info("Scheduled task - Active smoke test records: {}", count));
    }
    

}