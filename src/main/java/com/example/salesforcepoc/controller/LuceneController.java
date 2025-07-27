package com.example.salesforcepoc.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.salesforcepoc.service.LuceneSearchService;

@RestController
@RequestMapping("/api/search")
public class LuceneController {

    @Autowired
    private LuceneSearchService luceneSearchService;

    /**
     * Initialize/rebuild the Lucene index
     */
    @PostMapping("/index/rebuild")
    public ResponseEntity<Map<String, String>> rebuildIndex() {
        try {
            long startTime = System.currentTimeMillis();
            luceneSearchService.indexAllProducts();
            long endTime = System.currentTimeMillis();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Index rebuilt successfully",
                "timeTaken", (endTime - startTime) + "ms"
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to rebuild index: " + e.getMessage()
            ));
        }
    }

    /**
     * Get index statistics
     */
    @GetMapping("/index/stats")
    public ResponseEntity<Map<String, String>> getIndexStats() {
        try {
            String stats = luceneSearchService.getIndexStats();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "stats", stats
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to get index stats: " + e.getMessage()
            ));
        }
    }
}