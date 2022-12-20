package pet.skillbox.sitesearchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pet.skillbox.sitesearchengine.model.*;
import pet.skillbox.sitesearchengine.model.response.LinkModel;
import pet.skillbox.sitesearchengine.repositories.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CrawlingService {

    private final LemmaRepository lemmaRepository;
    private final FieldRepository fieldRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final SiteRepository siteRepository;
    private final LinkRepository linkRepository;
    private final IndexDeleteRepository indexDeleteRepository;

    @Autowired
    public CrawlingService(LemmaRepository lemmaRepository, FieldRepository fieldRepository,
                           PageRepository pageRepository, IndexRepository indexRepository,
                           SiteRepository siteRepository, LinkRepository linkRepository,
                           IndexDeleteRepository indexDeleteRepository) {
        this.lemmaRepository = lemmaRepository;
        this.fieldRepository = fieldRepository;
        this.pageRepository = pageRepository;
        this.indexRepository = indexRepository;
        this.siteRepository = siteRepository;
        this.linkRepository = linkRepository;
        this.indexDeleteRepository = indexDeleteRepository;
//        DBConnection.addIndexes();
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

    @Transactional(readOnly = true)
    public List<Lemma> getLemmaList(Set<String> lemmas, int id) {
        List<Lemma> lemmaList = new ArrayList<>();
        lemmas.forEach(l -> {
            Lemma lemma = id > 0 ? lemmaRepository.getLemmaByLemmaAndSiteIdAndIsDeleted(l, id, 0)
                    : lemmaRepository.getLemmaByLemmaAndIsDeleted(l, 0);
            if (lemma != null) {
                lemmaList.add(lemma);
            }
        });
        return lemmaList;
    }

//    @Transactional
//    public void addLemma(Lemma lemma) {
//        System.out.println(lemma);
//        Lemma oldLemma = lemmaRepository.getLemmaByLemmaAndIsDeleted(lemma.getLemma(), 0);
//        System.out.println(oldLemma);
//        if (oldLemma == null) {
//            lemmaRepository.save(lemma);
//        } else {
//            lemmaRepository.updateLemma(lemma.getLemma(), lemma.getFrequency() + 1, lemma.getId());
//        }
//    }

    @Transactional
    public int addSite(Site site) {
        site.setIsDeleted(0);
        siteRepository.save(site);
        return getSiteIdByUrl(site.getUrl(), site.getUserId());
    }

    @Transactional
    public int updateStatus(Site site) {
        int siteId = getSiteIdByUrl(site.getUrl(), site.getUserId());
        System.out.println(siteId + " " + site);
        if (siteId > 0) {
            siteRepository.updateSiteStatus(site.getStatus(), site.getStatusTime(), site.getLastError(), siteId);

        } else {
            siteId = addSite(site);
        }
        return siteId;
    }

    @Transactional(readOnly = true)
    public Site getSiteByUrl(String url) {
        return siteRepository.getByUrlAndUserId(url, 0) ;
    }

    @Transactional(readOnly = true)
    public int getMaxPageId() {
        Integer id = pageRepository.getMaxPageId();
        return id == null ? 0 : id;
    }

    @Transactional(readOnly = true)
    public int getSiteIdByUrl(String url, int userId) {
        System.out.println("url = " + url);
        System.out.println(siteRepository.findAll());
        Site site = siteRepository.getByUrlAndUserId(url, userId);
        return site == null ? -1 : site.getId() ;
    }

    @Transactional
    public synchronized void savePage(Page page) {
        System.out.println("saving page");
        System.out.println("<" + page.getId() + ">");
        System.out.println(" - " + page.getSite().getId() + " " + page.getSite().getUrl());
        page.setIsDeleted(0);
        pageRepository.save(page);
    }

    @Transactional
    public boolean saveLink(Link link) {
        if (linkRepository.getLinkByLink(link.getLink()) == null) {
            linkRepository.save(link);
            return true;
        }
        return false;
    }

    @Transactional
    public void updateLinks(Map<String, Integer> links, int userId) {
        links.forEach((l, i) -> linkRepository.updateLink(l, i, userId));
    }

    @Transactional
    public void updateLink(String link, Integer isSelected, int userId) {
        linkRepository.updateLink(link, isSelected, userId);
    }

    @Transactional(readOnly = true)
    public List<LinkModel> getLinks(int userId) {
        List<LinkModel> models = new ArrayList<>();
        List<Link> links = linkRepository.findAll().stream().filter(link -> link.getUserId() == userId).collect(Collectors.toList());
        for (Link link : links) {
            models.add(new LinkModel(link.getLink(), link.getName(), link.getIsSelected()));
        }
        return models;
    }

    @Transactional
    public boolean deleteSiteInfo(String url) {
        Site site = siteRepository.getSiteByUrl(url);
        System.out.println("Удаляемый сайт: " + site);
        Integer deleteIndex = indexDeleteRepository.getOne(1).getDeleteNumber();
        System.out.println("Индекс = " + deleteIndex);
        if (site != null) {
            int id = site.getId();
            System.out.println("Удаление сайта с id = " + id);
            if (lemmaRepository.getFirstBySiteId(id) != null) {
                lemmaRepository.updateLemmaDelete(site, deleteIndex);
                System.out.println("Удалили леммы с id = " + id);
            }
            if (pageRepository.getFirstBySiteId(id) != null) {
                pageRepository.updatePageDelete(site, deleteIndex);
                System.out.println("Удалили странницы с id = " + id);
            }
            if (indexRepository.getFirstBySiteId(id) != null) {
                indexRepository.updateIndexDelete(site, deleteIndex);
                System.out.println("Удалили индексы с id = " + id);
            }
            indexDeleteRepository.updateIndexDeleteDelete(deleteIndex + 1);
            System.out.println("Обновили метку удаления");
        }
        else {
            return false;
        }
        System.out.println("ENNNNNNNNNNNNNNND");
        return true;
    }

    @Transactional
    public void setNewDeleteIndex() {
        indexDeleteRepository.updateDefaultDeleteDelete();
    }

    @Transactional
    public void deleteAllDeletedDataB() {
        System.out.println("Удаление всех нужных данных " + LocalDateTime.now());
        siteRepository.deleteByIsDeleted();
        lemmaRepository.deleteByIsDeleted();
        System.out.println(lemmaRepository.countAllByIsDeleted(1));
        pageRepository.deleteByIsDeleted();
        System.out.println(pageRepository.countAllByIsDeleted(1));
        indexRepository.deleteByIsDeleted();
        System.out.println(indexRepository.countAllByIsDeleted(1));
        System.out.println("ENNNNNNNNNNNNNNND");
    }

    @Transactional
    public void deleteSite(String url) {
        Site site = siteRepository.getSiteByUrl(url);
        if (site != null) {
            siteRepository.updateSiteDelete(site.getId(), 1);
        }
    }

    @Transactional
    public boolean deleteLink(String url) {
        if (linkRepository.getLinkByLink(url) != null) {
            linkRepository.deleteLinkByLink(url);
            return true;
        }
        return false;
    }

    @Transactional
    public void deleteAllLinks() {
        linkRepository.deleteAll();
    }

    @Transactional(readOnly = true)
    public List<Field> getFields() {
        return fieldRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Site> getSites() {
        return siteRepository.findAll();
    }

    @Transactional
    public void insertBasicFields() {
        fieldRepository.save(new Field("title", "title", 1.5f));
        fieldRepository.save(new Field("body", "body", 0.8f));
    }
}
