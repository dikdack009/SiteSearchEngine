package pet.skillbox.sitesearchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pet.skillbox.sitesearchengine.model.*;
import pet.skillbox.sitesearchengine.repositories.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CrawlingService {

    private final LemmaRepository lemmaRepository;
    private final FieldRepository fieldRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final LemmaTmpRepository lemmaTmpRepository;
    private final PageTmpRepository pageTmpRepository;
    private final IndexTmpRepository indexTmpRepository;
    private final SiteRepository siteRepository;
    private final LinkRepository linkRepository;

    @Autowired
    public CrawlingService(LemmaRepository lemmaRepository, FieldRepository fieldRepository,
                           PageRepository pageRepository, IndexRepository indexRepository,
                           LemmaTmpRepository lemmaTmpRepository, PageTmpRepository pageTmpRepository,
                           IndexTmpRepository indexTmpRepository, SiteRepository siteRepository,
                           LinkRepository linkRepository) {
        this.lemmaRepository = lemmaRepository;
        this.fieldRepository = fieldRepository;
        this.pageRepository = pageRepository;
        this.indexRepository = indexRepository;
        this.lemmaTmpRepository = lemmaTmpRepository;
        this.pageTmpRepository = pageTmpRepository;
        this.indexTmpRepository = indexTmpRepository;
        this.siteRepository = siteRepository;
        this.linkRepository = linkRepository;
    }


    @Transactional(readOnly = true)
    public double countPages(int id) {
        return id > 0 ? pageRepository.countPagesBySiteId(id) : pageRepository.countPagesByCode(200);
    }

    @Transactional(readOnly = true)
    public List<Field> gelAllFields() {
        return fieldRepository.findAll();
    }

    @Transactional
    public Page addPage(Page page) {
        Page pageFromDB = pageRepository.getByPath(page.getPath());

        if (pageFromDB != null) {
            return null;
        }

        pageRepository.save(page);
        return pageRepository.getByPath(page.getPath());
    }

    @Transactional
    public void addIndex(Index index) {
        indexRepository.save(index);
    }

    @Transactional(readOnly = true)
    public List<Lemma> getLemmaList(Set<String> lemmas, int id) {
        List<Lemma> lemmaList = new ArrayList<>();
        lemmas.forEach(l -> {
            Lemma lemma = id > 0 ? lemmaRepository.getLemmaByLemmaAndSiteId(l, id) : lemmaRepository.getLemmaByLemma(l);
            if (lemma != null) {
                lemmaList.add(lemma);
            }
        });
        return lemmaList;
    }

    @Transactional
    public void addLemma(Lemma lemma) {
        Lemma lemmaFromDB = lemmaRepository.getLemmaByLemmaAndSiteId(lemma.getLemma(), 0);

        if (lemmaFromDB != null){
            lemmaRepository.delete(lemmaFromDB);
        }

        lemmaRepository.save(lemma);
    }

    @Transactional
    public int addSite(Site site) {
        siteRepository.save(site);
        return getSiteIdByUrl(site.getUrl());
    }

    @Transactional
    public int updateStatus(Site site) {
        int siteId = getSiteIdByUrl(site.getUrl());
        System.out.println(site);
        if (siteId > 0) {
            siteRepository.updateSiteStatus(site.getStatus(), site.getStatusTime(), site.getLastError(), siteId);

        } else {
            siteId = addSite(site);
        }
        return siteId;
    }

//    @Transactional(readOnly = true)
//    public List<Site> getIndexingSites() {
//        return siteRepository.findAllBy(Status.INDEXING);
//    }

    @Transactional(readOnly = true)
    public Site getSiteByUrl(String url) {
        return siteRepository.getByUrl(url) ;
    }

    @Transactional(readOnly = true)
    public int getMaxPageId() {
        Integer id = pageRepository.getMaxPageId();
        return id == null ? 0 : id;
    }

    @Transactional(readOnly = true)
    public int getSiteIdByUrl(String url) {
        Site site = siteRepository.getByUrl(url);
        return site == null ? -1 : site.getId() ;
    }

    @Transactional
    public synchronized int savePage(Page page) {
        pageRepository.save(page);
        return page.getId();
    }

    @Transactional
    public boolean saveLink(Link link) {
        if (linkRepository.getLinkByLink(link.getLink()) == null) {
            linkRepository.save(link);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<Link> getLinks() {
        return linkRepository.findAll();
    }

    @Transactional
    public Integer deleteSiteInfo(String url) {
        Site site = siteRepository.getSiteByUrl(url);
        if (site != null) {
            int id = site.getId();
            System.out.println("Удаление сайта с id = " + id);
            System.out.println(lemmaRepository.getFirstBySiteId(id));
            if (lemmaRepository.getFirstBySiteId(id) != null) {
                lemmaRepository.deleteBySiteId(id);
                System.out.println("Удалили леммы с id = " + id);
            }
            if (pageRepository.getFirstBySiteId(id) != null) {
                pageRepository.deleteBySiteId(id);
                System.out.println("Удалили странницы с id = " + id);
            }
            if (indexRepository.getFirstBySiteId(id) != null) {
                indexRepository.deleteBySiteId(id);
                System.out.println("Удалили индексы с id = " + id);
            }
        }
        else {
            return -1;
        }
        System.out.println("ENNNNNNNNNNNNNNND");
        return 1;
    }

//    @Transactional
//    public void deleteAllSites(int id) {
//        siteRepository.get
//    }

    @Transactional
    public void deleteTmpSiteInfo(int id) {
        System.out.println("Удаление временного сайта с id = " + id);
        if (lemmaTmpRepository.getFirstBySiteId(id) != null) {
            lemmaTmpRepository.deleteBySiteId(id);
        }
        System.out.println("Удалили леммы");
        if (pageTmpRepository.getFirstBySiteId(id) != null) {
            pageTmpRepository.deleteBySiteId(id);
        }
        System.out.println("Удалили странницы");
        if (indexTmpRepository.getFirstBySiteId(id) != null) {
            indexTmpRepository.deleteBySiteId(id);
        }
        System.out.println("Удалили индексы");
    }

    @Transactional
    public void deleteSite(String url) {
        if (siteRepository.getSiteByUrl(url) != null) {
            siteRepository.deleteByUrl(url);
        }
    }
}
