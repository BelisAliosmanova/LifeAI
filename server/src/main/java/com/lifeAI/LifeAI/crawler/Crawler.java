package com.lifeAI.LifeAI.crawler;

import com.lifeAI.LifeAI.model.Site;
import com.lifeAI.LifeAI.respository.PageRepository;
import com.lifeAI.LifeAI.respository.SiteRepository;
import com.lifeAI.LifeAI.services.PageService;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Map;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class Crawler extends WebCrawler {
    final static Pattern FILTERS = Pattern.compile(".*\\.(css|js|xml|json).*|.*(\\.(bmp|gif|jpe?g|jpg|png|tiff?|ttf|font|woff|woff2|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|rm|smil|wmv|swf|wma|rar|zip|gz|ico|svg))$");
    private final String[] toNotInclude = {"/wp-content/", "/wp-json/", "/wp-includes/", "/img/", "/css"};
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final CrawlerExtractor crawlerExtractor;
    private final PageService pageService;

    @Override
    public boolean shouldVisit(Page referringPage, WebURL webURL) {

        var url = webURL.getURL();

        for (var x : toNotInclude) {
            if (url.contains(x)) {
                return false;
            }
        }

        return !FILTERS.matcher(url).matches();
    }

    @Override
    public void visit(Page pageToVisit) {
        if (pageToVisit.getParseData() instanceof HtmlParseData htmlParseData) {
            Document htmlDoc = Jsoup.parse(htmlParseData.getHtml());
            WebURL webURL = pageToVisit.getWebURL();
            String url = webURL.getURL();

            System.out.println("WEB URL: " + url);
            System.out.println("WEB PARENT URL: " + webURL.getParentUrl());
            System.out.println("WEB DOMAIN: " + webURL.getDomain());
            Site site = siteRepository.getSiteByUrlContaining(webURL.getDomain()).orElse(null);

            if (site == null) {
                System.out.println(" SKIPPED");
                return;
            }

            Map<String, String> metadata = crawlerExtractor.extractMetadata(htmlDoc);
            String title = crawlerExtractor.extractTitle(htmlDoc);
            String body = crawlerExtractor.extractBody(htmlDoc);

            var page = new com.lifeAI.LifeAI.model.Page();
            page.setUrl(url);
            page.setData(body);
            page.setTitle(title);
            page.setMetaData(metadata.toString());
            page.setSite(site);
            var savedPage = pageRepository.save(page);

            pageService.savePageToVectorDatabase(savedPage);
            System.out.println(" SAVED");
        }
    }
}
