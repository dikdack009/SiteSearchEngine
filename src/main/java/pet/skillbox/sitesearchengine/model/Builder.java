package pet.skillbox.sitesearchengine.model;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Data
@ToString
public class Builder {
    @Getter
    private StringBuilder pageBuilder;

    @Getter
    private StringBuilder indexBuilder;

    @Getter
    private StringBuilder lemmaBuilder;

    public Builder(){
        pageBuilder = new StringBuilder();
        indexBuilder = new StringBuilder();
        lemmaBuilder = new StringBuilder();
    }
}
