package crawling;

import model.Page;

import java.util.Comparator;
import java.util.Map;

public class ResultComparator implements Comparator<Page> {
    Map<Page, Double> base;

    public ResultComparator(Map<Page, Double> base) {
        this.base = base;
    }

    public int compare(Page a, Page b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}