package pet.skillbox.sitesearchengine.model.thread;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pet.skillbox.sitesearchengine.model.response.Statistic;

import java.util.concurrent.Callable;

public class StatisticThread implements Callable<ResponseEntity<Statistic>> {
    private final boolean isIndexing;
    private final int userId;

    public StatisticThread(boolean isIndexing, int userId) {
        this.isIndexing = isIndexing;
        this.userId = userId;
    }

    @Override
    public ResponseEntity<Statistic> call() throws Exception {
        return new ResponseEntity<>(new Statistic(isIndexing, userId), HttpStatus.OK);
    }
}
