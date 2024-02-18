package com.fga.example.controller;

import com.fga.example.service.DocumentService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/docsbean/{id}")
    public String simpleBean(@PathVariable String id) {
        return documentService.getDocumentWithSimpleFgaBean(id);
    }

    @GetMapping("/docsaop/{id}")
    public String getDocumentWithFgaAnnotation(@PathVariable String id) {
        return documentService.customAnnotation(id);
    }

    @PostMapping("/docs")
    public String createDoc(@RequestBody String id, Principal principal) {
        return documentService.createDoc(id, principal);
    }
}
