package pet.skillbox.sitesearchengine.model.response;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class Data  implements Comparable<Data> {
    private final String site;
    private final String siteName;
    private final String uri;
    private final String title;
    private final String snippet;
    private final Double relevance;

    public Data(String site, String siteName, String uri,
                String title, String snippet, double relevance) {
        this.site = site;
        this.siteName = siteName;
        this.uri = uri;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
    }

    @Override
    public int compareTo(@NotNull Data o) {
        return -this.relevance.compareTo(o.relevance);
    }
}
