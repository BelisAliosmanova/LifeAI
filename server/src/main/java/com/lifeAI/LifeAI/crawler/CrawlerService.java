package com.lifeAI.LifeAI.crawler;

import com.lifeAI.LifeAI.model.Site;
import com.lifeAI.LifeAI.respository.SiteRepository;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class CrawlerService implements Runnable {

    private static CrawlController controller = null;
    private final static int POLITENESS_DELAY = 200;
    private final static int MAX_CRAWL_DEPTH = 6;
    private final static int MAX_PAGES_COUNT = -1;
    private final static int NUMBER_OF_CRAWLERS = 10;

    private final SiteRepository siteRepository;
    private final CrawlerFactory crawlerFactory;

    public CrawlerService(SiteRepository siteRepository, CrawlerFactory crawlerFactory) {
        this.siteRepository = siteRepository;
        this.crawlerFactory = crawlerFactory;
    }

    @Async
    public void shutdown() {
        controller.shutdown();
    }

    @Async
    public void run() {
        try {
            controller = buildCrawlController();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        for (Site site : siteRepository.findAll().stream().limit(10).toList()) {
            System.out.println(site.getUrl());
            controller.addSeed(site.getUrl());
        }

        controller.start(crawlerFactory, NUMBER_OF_CRAWLERS);
        controller.waitUntilFinish();
    }

    private CrawlController buildCrawlController() throws Exception {
        File crawlStorage = new File("server/src/test/resources/crawler4j");
        CrawlConfig config = new CrawlConfig();
        config.setMaxDepthOfCrawling(MAX_CRAWL_DEPTH);
        config.setIncludeBinaryContentInCrawling(true);
        config.setIncludeHttpsPages(true);
        config.setMaxPagesToFetch(MAX_PAGES_COUNT);
        config.setPolitenessDelay(POLITENESS_DELAY);
        config.setCrawlStorageFolder(crawlStorage.getAbsolutePath());

        PageFetcher pageFetcher = new PageFetcher(config);

        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        return new CrawlController(config, pageFetcher, robotstxtServer);
    }
}
