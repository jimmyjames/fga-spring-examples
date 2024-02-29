package com.fga.example.controller;

import com.fga.example.service.Document;
import com.fga.example.service.DocumentService;
import org.springframework.web.bind.annotation.*;

@RestController
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/docs/{id}")
    public Document simpleBean(@PathVariable String id) {
        return documentService.getDocumentWithPreAuthorize(id);
    }

    @GetMapping("/docsaop/{id}")
    public Document customAnnotation(@PathVariable String id) {
        return documentService.getDocumentWithFgaCheck(id);
    }

    @PostMapping("/docs")
    public String createDoc(@RequestBody String id) {
        return documentService.createDoc(id);
    }
}
