package pet.skillbox.sitesearchengine.configuration;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pet.skillbox.sitesearchengine.services.CrawlingService;

@Component
@EnableScheduling
@EnableAutoConfiguration
public class ScheduledTasks {

    private final CrawlingService crawlingService;

    @Autowired
    public ScheduledTasks(CrawlingService crawlingService) {
        this.crawlingService = crawlingService;
    }


    @Scheduled(cron = "0 0 3 * * *")
    public void deleteData() {
        System.out.println("SCHEDULE");
        crawlingService.deleteAllDeletedDataB();
        crawlingService.setNewDeleteIndex();
    }
}