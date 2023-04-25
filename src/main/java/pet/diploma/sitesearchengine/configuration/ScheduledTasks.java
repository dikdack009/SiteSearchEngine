package pet.diploma.sitesearchengine.configuration;

import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pet.diploma.sitesearchengine.services.CrawlingService;

@Component
@EnableScheduling
@EnableAutoConfiguration
public class ScheduledTasks {

    private final CrawlingService crawlingService;

    @Autowired
    public ScheduledTasks(CrawlingService crawlingService) {
        this.crawlingService = crawlingService;
    }


    @Scheduled(cron = "0 2 18 * * *")
    @Async
    public void deleteData() {
        LogManager.getLogger("index").info("Метод по расписанию запущен");
        crawlingService.deleteAllDeletedDataB();
        crawlingService.setNewDeleteIndex();
    }
}