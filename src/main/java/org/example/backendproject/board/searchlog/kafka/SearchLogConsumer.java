package org.example.backendproject.board.searchlog.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backendproject.board.searchlog.domain.SearchLogDocument;
import org.example.backendproject.board.searchlog.dto.SearchLogMessage;
import org.example.backendproject.board.searchlog.service.SearchLogEsService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SearchLogConsumer {

    // 카프카에서 메시지 꺼내 ES로 넘김
    private final SearchLogEsService searchLogEsService;

    @KafkaListener(
            topics = "search-log", // 구독한 토픽 명
            groupId = "search-ㅣog-group", // 컨슈머 그룹
            containerFactory = "kafkaListenerContainerFactory" // 사용할 리스너 컨테이너
    )
    public void consume(SearchLogMessage message) {

        log.info("카프카로부터 메시지 수신 {}", message);

        SearchLogDocument doc = SearchLogDocument.builder()
                .keyword(message.getKeyword())
                .userId(message.getUserId())
                .searchedAt(message.getSearchedAt())
                .build();

        searchLogEsService.save(doc);
    }

}
