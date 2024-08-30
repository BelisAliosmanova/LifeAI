package com.lifeAI.LifeAI.crawler;

import com.lifeAI.LifeAI.model.Site;
import com.lifeAI.LifeAI.respository.PageRepository;
import com.lifeAI.LifeAI.respository.SiteRepository;
import com.lifeAI.LifeAI.services.impl.PageService;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
            org.jsoup.nodes.Document htmlDoc = Jsoup.parse(htmlParseData.getHtml());
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
            savePageToDatabase(htmlDoc, url, site);
        }
    }

    private void savePageToDatabase(Document htmlDoc, String url, Site site) {
        Map<String, String> metadata = crawlerExtractor.extractMetadata(htmlDoc);
        String title = crawlerExtractor.extractTitle(htmlDoc);

        // Extract only <p>, <h1>, and <span> elements from the body
        StringBuilder extractedBody = new StringBuilder();

        // Extract <h1> elements
        Elements h1Elements = htmlDoc.select("h1");
        for (Element element : h1Elements) {
            extractedBody.append(element.text()).append("\n");
        }

        // Extract <p> elements
        Elements pElements = htmlDoc.select("p");
        for (Element element : pElements) {
            extractedBody.append(element.text()).append("\n");
        }

        // Extract <span> elements
        Elements spanElements = htmlDoc.select("span");
        for (Element element : spanElements) {
            extractedBody.append(element.text()).append("\n");
        }

        var page = new com.lifeAI.LifeAI.model.Page();
        page.setUrl(url);
        page.setData(extractedBody.toString());
        page.setTitle(title);
        page.setMetaData(metadata.toString());
        page.setSite(site);

        var savedPage = pageRepository.save(page);

        pageService.savePageToVectorDatabase(savedPage);
        System.out.println(" SAVED");
    }
}
