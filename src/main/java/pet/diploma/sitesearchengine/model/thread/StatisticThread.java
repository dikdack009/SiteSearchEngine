package pet.diploma.sitesearchengine.model.thread;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pet.diploma.sitesearchengine.model.response.Statistic;
import pet.diploma.sitesearchengine.services.CrawlingService;

import java.util.concurrent.Callable;

public class StatisticThread implements Callable<ResponseEntity<Statistic>> {
    private final boolean isIndexing;
    private final int userId;
    private final CrawlingService crawlingService;

    public StatisticThread(boolean isIndexing, int userId, CrawlingService crawlingService) {
        this.isIndexing = isIndexing;
        this.userId = userId;
        this.crawlingService = crawlingService;
    }

    @Override
    public ResponseEntity<Statistic> call() {
        return new ResponseEntity<>(new Statistic(isIndexing, userId, crawlingService), HttpStatus.OK);
    }
}
