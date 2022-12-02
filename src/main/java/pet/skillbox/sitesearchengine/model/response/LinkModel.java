package pet.skillbox.sitesearchengine.model.response;

import lombok.Getter;

@Getter
public class LinkModel {
    private final String url;
    private final String name;
    private final int isSelected;

    public LinkModel(String url, String name, int isSelected) {
        this.url = url;
        this.name = name;
        this.isSelected = isSelected;
    }
}
