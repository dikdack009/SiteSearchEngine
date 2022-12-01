package pet.skillbox.sitesearchengine.model.response;

import lombok.Getter;

@Getter
public class LinkModel {
    private final String url;
    private final String name;

    public LinkModel(String url, String name) {
        this.url = url;
        this.name = name;
    }
}
