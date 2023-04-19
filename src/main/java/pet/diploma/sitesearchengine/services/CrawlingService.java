package pet.diploma.sitesearchengine.services;

import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pet.diploma.sitesearchengine.model.*;
import pet.diploma.sitesearchengine.repositories.*;
import pet.diploma.sitesearchengine.model.response.LinkModel;

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
    }


    @Transactional(readOnly = true)
    public double countPages(int id) {
        return id > 0 ? pageRepository.countPagesBySiteId(id) : pageRepository.countPagesByCode(200);
    }

    @Transactional(readOnly = true)
    public List<Field> gelAllFields() {
        return fieldRepository.findAll();
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

    @Transactional
    public int addSite(Site site) {
        site.setIsDeleted(0);
        siteRepository.save(site);
        return getSiteIdByUrl(site.getUrl(), site.getUserId());
    }

    @Transactional
    public int updateStatus(Site site) {
        int siteId = getSiteIdByUrl(site.getUrl(), site.getUserId());
        if (siteId > 0) {
            siteRepository.updateSiteStatus(site.getStatus(), site.getStatusTime(), site.getLastError(), siteId, site.getUserId());

        } else {
            siteId = addSite(site);
        }
        return siteId;
    }

    @Transactional(readOnly = true)
    public Site getSiteByUrl(String url, int userId) {
        return siteRepository.getByUrlAndUserIdAndIsDeleted(url, userId, 0) ;
    }

    @Transactional(readOnly = true)
    public int getSiteIdByUrl(String url, int userId) {
        Site site = siteRepository.getByUrlAndUserIdAndIsDeleted(url, userId, 0);
        return site == null ? -1 : site.getId() ;
    }

    @Transactional
    public synchronized void savePage(Page page) {
        page.setIsDeleted(0);
        pageRepository.save(page);
    }

    @Transactional
    public boolean saveLink(Link link) {
        if (linkRepository.getLinkByLinkAndUserId(link.getLink(), link.getUserId()) == null) {
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
    public boolean deleteSiteInfo(String url, int userId) {
        Site site = siteRepository.getSiteByUrlAndUserIdAndIsDeleted(url, userId, 0);
        Integer deleteIndex = indexDeleteRepository.getOne(1).getDeleteNumber();
        if (site != null) {
            int id = site.getId();
            LogManager.getLogger("index").info("Удаляем информацию с сайта: " + id);
            if (lemmaRepository.getFirstBySiteId(id) != null) {
                lemmaRepository.updateLemmaDelete(site, deleteIndex);
            }
            if (pageRepository.getFirstBySiteId(id) != null) {
                pageRepository.updatePageDelete(site, deleteIndex);
            }
            if (indexRepository.getFirstBySiteId(id) != null) {
                indexRepository.updateIndexDelete(site, deleteIndex);
            }
            indexDeleteRepository.updateIndexDeleteDelete(deleteIndex + 1);
        }
        else {
            return false;
        }
        LogManager.getLogger("index").info("Удаление данных сайта " + site.getId() + " закончено");
        return true;
    }

    @Transactional
    public void setNewDeleteIndex() {
        indexDeleteRepository.updateDefaultDeleteDelete();
    }

    @Transactional
    public void deleteAllDeletedDataB() {
        LogManager.getLogger("index").info("Удаление всех нужных данных ночью");
        lemmaRepository.deleteByIsDeleted();
        pageRepository.deleteByIsDeleted();
        indexRepository.deleteByIsDeleted();
        siteRepository.deleteByIsDeleted();
        LogManager.getLogger("index").info("Закончили ночное удаление");
    }

    @Transactional
    public void deleteSite(String url, int userId) {
        Site site = siteRepository.getSiteByUrlAndUserIdAndIsDeleted(url, userId, 0);

        if (site != null) {
            siteRepository.updateSiteDelete(site.getId(), 1);
        }
    }

    @Transactional
    public boolean deleteLink(String url, int userId) {
        if (linkRepository.getLinkByLinkAndUserId(url, userId) != null) {
            linkRepository.deleteLinkByLinkAndUserId(url, userId);
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
