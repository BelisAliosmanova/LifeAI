package com.lifeAI.LifeAI.crawler;

import com.lifeAI.LifeAI.respository.PageRepository;
import com.lifeAI.LifeAI.respository.SiteRepository;
import com.lifeAI.LifeAI.services.impl.PageService;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrawlerFactory implements CrawlController.WebCrawlerFactory<WebCrawler> {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final CrawlerExtractor crawlerExtractor;
    private final PageService pageService;
    @Override
    public Crawler newInstance() {
        return new Crawler(
                pageRepository,
                siteRepository,
                crawlerExtractor,
                pageService
        );
    }
}
