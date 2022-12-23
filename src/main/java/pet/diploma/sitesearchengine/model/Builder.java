package pet.diploma.sitesearchengine.model;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Data
@Getter
@ToString
public class Builder {
    private StringBuilder pageBuilder;
    private StringBuilder indexBuilder;
    private StringBuilder lemmaBuilder;

    public Builder(){
        pageBuilder = new StringBuilder();
        indexBuilder = new StringBuilder();
        lemmaBuilder = new StringBuilder();
    }
}
