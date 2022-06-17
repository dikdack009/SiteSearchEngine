package crawling;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class SearchResult implements Comparable<SearchResult>{
    String uri;
    String title;
    String snippet;
    Double relevance;

    @Override
    public String toString() {
        return  "\n\n" + uri + "\n" +
                title + "\n" +
                snippet + "\n" +
                relevance ;
    }

    @Override
    public int compareTo(@NotNull SearchResult o) {
        return -this.relevance.compareTo(o.relevance);
    }
}
