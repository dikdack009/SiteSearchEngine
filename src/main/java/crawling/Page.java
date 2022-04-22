package crawling;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "page")
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 65535, columnDefinition = "TEXT")
    private String path;

    @Column
    private int code;

    @Column(length = 16777215, columnDefinition = "MEDIUMTEXT")
    private String content;

    public Page() {

    }

    @Override
    public String toString() {
        return "crawling.Page{" +
                "\tid=" + id + "" +
                "\tpath='" + path + '\'' + "" +
                "\tcode=" + code + "" +
                '}';
    }

    public static Page getPageById(List<Page> pageList, int id){
        for (Page page :pageList){
            if (page.getId().equals(id)){
                return page;
            }
        }
        return null;
    }
}
