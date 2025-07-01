package org.example.backendproject.board.elasticsearch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backendproject.board.elasticsearch.dto.BoardEsDocument;
import org.example.backendproject.board.elasticsearch.service.BoardEsService;
import org.example.backendproject.board.searchlog.dto.SearchLogMessage;
import org.example.backendproject.board.service.BoardService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/boards")
public class BoardEsController {

    private final BoardEsService boardEsService;
    private final BoardService boardService;
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

    /** 게시판 글 쓰기 ES 배치 작업 **/
    @PostMapping("/batchEsInsert")
    public String batchESInsert() {
        try {
            boardService.batchSaveEsBoard();
        } catch (Exception e) {
            log.error("batchESInsert error : {}", e.getMessage());
        }
        return "ok";
    }
}
