package pet.skillbox.sitesearchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pet.skillbox.sitesearchengine.model.*;
import pet.skillbox.sitesearchengine.repositories.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CrawlingService {

    private final LemmaRepository lemmaRepository;

    private final FieldRepository fieldRepository;

    private final PageRepository pageRepository;

    private final IndexRepository indexRepository;

    private final SiteRepository siteRepository;

    @Autowired
    public CrawlingService(LemmaRepository lemmaRepository, FieldRepository fieldRepository,
                           PageRepository pageRepository, IndexRepository indexRepository, SiteRepository siteRepository) {
        this.lemmaRepository = lemmaRepository;
        this.fieldRepository = fieldRepository;
        this.pageRepository = pageRepository;
        this.indexRepository = indexRepository;
        this.siteRepository = siteRepository;
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
    public List<Lemma> getLemmaList(Set<String> lemmas) {
        List<Lemma> lemmaList = new ArrayList<>();
        lemmas.forEach(lemmaRepository::getLemmaByLemma);
        return lemmaList;
    }

    @Transactional
    public void addLemma(Lemma lemma) {
        Lemma lemmaFromDB = lemmaRepository.getLemmaByLemma(lemma.getLemma());

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
    public void updateStatus(String name, Status status, String error){
        Site siteFromDB = siteRepository.getByName(name);

        siteRepository.delete(siteFromDB);
        siteFromDB.setStatus(status);
        siteFromDB.setLastError(error);
        siteRepository.save(siteFromDB);
    }

    @Transactional(readOnly = true)
    public List<Site> getIndexingSites() {
        return siteRepository.findAllBy(Status.INDEXING);
    }

    @Transactional(readOnly = true)
    public Site getSiteByName(String name) {
        return siteRepository.getByName(name);
    }
}
