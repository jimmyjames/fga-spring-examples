package com.fga.example.controller;

import com.fga.example.service.DocumentService;
import org.springframework.web.bind.annotation.*;

@RestController
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/docs/{id}")
    public String simpleBean(@PathVariable String id) {
        return documentService.getDocumentWithSimpleFgaBean(id);
    }

    @GetMapping("/docsaop/{id}")
    public String customAnnotation(@PathVariable String id) {
        return documentService.getDocumentWithFgaAnnotation(id);
    }

    @PostMapping("/docs")
    public String createDoc(@RequestBody String id) {
        return documentService.createDoc(id);
    }
}
