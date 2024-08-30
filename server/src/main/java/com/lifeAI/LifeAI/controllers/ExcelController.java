package com.lifeAI.LifeAI.controllers;

import com.lifeAI.LifeAI.services.impl.ExcelReaderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/excel")
public class ExcelController {

    private final ExcelReaderService excelReaderService;

    public ExcelController(ExcelReaderService excelReaderService) {
        this.excelReaderService = excelReaderService;
    }

    @PostMapping("/upload")
    public void uploadExcelFile() throws IOException {
        excelReaderService.readExcelFile();
    }
}

