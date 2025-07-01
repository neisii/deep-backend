package org.example.backendproject.board.elasticsearch.controller;

import lombok.RequiredArgsConstructor;
import org.example.backendproject.board.elasticsearch.dto.BoardEsDocument;
import org.example.backendproject.board.elasticsearch.service.BoardEsService;
import org.example.backendproject.board.searchlog.dto.SearchLogMessage;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/boards")
public class BoardEsController {

    private final BoardEsService boardEsService;
    private final KafkaTemplate<String, SearchLogMessage> kafkaTemplate; // Spring to Kafka


    @GetMapping("/elasticsearch")
    public ResponseEntity<Page<BoardEsDocument>> elasticSearch(
            @RequestParam String keyword
            , @RequestParam(defaultValue = "0") int page
            , @RequestParam(defaultValue = "10") int size) {

        // 검색어 정보 카프카 전송
        String userId = "1";
        String searchedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

        SearchLogMessage message = new SearchLogMessage(keyword, userId, searchedAt);
        kafkaTemplate.send("search-log", message); // search-log topic으로 메시지 전달

        return ResponseEntity.ok(boardEsService.search(keyword, page, size));
    }
}
