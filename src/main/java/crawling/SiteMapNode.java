package crawling;

import lombok.Data;
import lombok.Getter;

import java.util.concurrent.CopyOnWriteArraySet;

@Data
public class SiteMapNode {
    @Getter
    private String url;
    private volatile SiteMapNode parent;
    private volatile int depth;
    @Getter
    private volatile CopyOnWriteArraySet<SiteMapNode> subLinks;

    public SiteMapNode(String url) {
        this.url = url;
        subLinks = new CopyOnWriteArraySet<>();
        depth = 0;
        parent = null;

    }

    public void addSubLinks(SiteMapNode subLink) {
        if (!subLinks.contains(subLink) && subLink.getUrl().startsWith(url)) {
            this.subLinks.add(subLink);
            subLink.setParent(this);
        }
    }

    private void setParent(SiteMapNode siteMapNode) {
        synchronized (this) {
            this.parent = siteMapNode;
            this.depth = setDepth();
        }
    }

    private int setDepth() {
        if (parent == null) {
            return 0;
        }
        return 1 + parent.getDepth();
    }
}
