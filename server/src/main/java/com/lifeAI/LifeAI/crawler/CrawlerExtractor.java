package com.lifeAI.LifeAI.crawler;

import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CrawlerExtractor {
    public Map<String, String> extractMetadata(Document document) {
        Map<String, String> metadata = new HashMap<>();

        document.select("head meta")
                .stream()
                .filter(x -> x.hasAttr("name") && x.hasAttr("content"))
                .forEach(x -> metadata.put(x.attr("name"), x.attr("content")));

        return metadata;
    }

    public String extractBody(Document document) {
        return document.body().toString();
    }

    public String extractTitle(Document document) {
        return document.title();
    }
}
