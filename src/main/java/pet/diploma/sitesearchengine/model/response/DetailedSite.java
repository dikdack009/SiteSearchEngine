package pet.diploma.sitesearchengine.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetailedSite {
    private final String url;
    private final String name;
    private final String status;
    private final Long statusTime;
    private final String error;
    private final int pages;
    private final int lemmas;

    public DetailedSite(String url, String name, String status,
                        Long statusTime, String error, int pages, int lemmas) {
        this.url = url;
        this.name = name;
        this.status = status;
        this.statusTime = statusTime;
        this.error = error;
        this.pages = pages;
        this.lemmas = lemmas;
    }
}
