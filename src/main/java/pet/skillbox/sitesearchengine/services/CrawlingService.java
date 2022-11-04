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

    @Autowired
    public CrawlingService(LemmaRepository lemmaRepository, FieldRepository fieldRepository,
                           PageRepository pageRepository, IndexRepository indexRepository,
                           LemmaTmpRepository lemmaTmpRepository, PageTmpRepository pageTmpRepository,
                           IndexTmpRepository indexTmpRepository, SiteRepository siteRepository) {
        this.lemmaRepository = lemmaRepository;
        this.fieldRepository = fieldRepository;
        this.pageRepository = pageRepository;
        this.indexRepository = indexRepository;
        this.lemmaTmpRepository = lemmaTmpRepository;
        this.pageTmpRepository = pageTmpRepository;
        this.indexTmpRepository = indexTmpRepository;
        this.siteRepository = siteRepository;
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
    public void addSite(Site site){
        siteRepository.save(site);
    }

    @Transactional
    public void updateStatus(String url, String status, String error){
//        Site siteFromDB = siteRepository.getByUrl(url);
//        Site newSite = siteFromDB;
//        siteRepository.delete(siteFromDB);
//        newSite.setStatus(status);
//        newSite.setLastError(error);
//        siteRepository.save(newSite);
        siteRepository.updateSiteStatus(status, LocalDateTime.now(), error, url);
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
    public int getSiteIdByUrl(String url) {
        Site site = siteRepository.getByUrl(url);
        return site == null ? -1 : site.getId() ;
    }

    @Transactional
    public void deleteSiteInfo(int id) {
        lemmaRepository.deleteBySiteId(id);
        pageRepository.deleteBySiteId(id);
        indexRepository.deleteBySiteId(id);
    }

    @Transactional
    public void deleteTmpSiteInfo(int id) {
        lemmaTmpRepository.deleteBySiteId(id);
        pageTmpRepository.deleteBySiteId(id);
        indexTmpRepository.deleteBySiteId(id);
    }
}
