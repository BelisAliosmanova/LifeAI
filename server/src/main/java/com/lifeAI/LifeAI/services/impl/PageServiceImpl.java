package com.lifeAI.LifeAI.services.impl;

import com.lifeAI.LifeAI.model.Page;
import com.lifeAI.LifeAI.respository.PageRepository;
import com.lifeAI.LifeAI.services.PageService;
import com.lifeAI.LifeAI.utils.MapUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {
    private final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter;

    @Override
    public void savePageToVectorDatabase(Page page) {
        var metadata = MapUtils.stringToMap(page.getMetaData());
        metadata.put("id", page.getId());
        metadata.put("title", page.getTitle());
        metadata.put("url", page.getUrl());
        metadata.put("siteUrl", page.getSite().getUrl());

        Document document = new Document(
                page.getTitle() + " " + page.getUrl() + '\n' + page.getData(),
                metadata
        );

        vectorStore.add(tokenTextSplitter.apply(List.of(document)));
    }
}