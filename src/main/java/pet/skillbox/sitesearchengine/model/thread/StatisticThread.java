package pet.skillbox.sitesearchengine.model.thread;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pet.skillbox.sitesearchengine.model.response.Data;
import pet.skillbox.sitesearchengine.model.response.Statistic;

import java.util.concurrent.Callable;

public class StatisticThread implements Callable<ResponseEntity<Statistic>> {
    private final boolean isIndexing;

    public StatisticThread(boolean isIndexing) {
        this.isIndexing = isIndexing;
    }

    @Override
    public ResponseEntity<Statistic> call() throws Exception {
        return new ResponseEntity<>(new Statistic(isIndexing), HttpStatus.OK);
    }
}
