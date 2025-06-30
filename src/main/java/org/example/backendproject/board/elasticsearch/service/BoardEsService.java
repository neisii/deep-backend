package org.example.backendproject.board.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backendproject.board.elasticsearch.dto.BoardEsDocument;
import org.example.backendproject.board.elasticsearch.repository.BoardEsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardEsService {

    // ES 명령 전달 API
    private final ElasticsearchClient client;
    private final BoardEsRepository boardEsRepository;


    public void save(BoardEsDocument document) {
        boardEsRepository.save(document);
    }

    public void deleteById(String id) {
       boardEsRepository.deleteById(id);
    }

    // 검색 키워드와 페이지 번호와 페이지 크기를 받아서 ES에서 검색
    // 검색된 정보와 페이징 정보도 함께 반환하기 위해 Page 객체 사용
    public Page<BoardEsDocument> search(String keyword, int page, int size) {
        try {
            // 페이징 시작 위치 계산
            int from = page * size;

            // ES 검색 조건
            Query query;

            // 전체 문서 검색(기본값)
            if (keyword == null || keyword.isBlank()) {
                query = MatchAllQuery.of(m -> m)._toQuery(); // 전체 문서 가져옴
            } else {
                query = BoolQuery.of(b -> {
                    // PrefixQuery: 해당 필드가 키워드로 시작하는지 검사
                    // MatchQuery: 해당 필드가 키워드를 포함하는지 검사

                    // 접두어 검색
                    b.should(PrefixQuery.of(p -> p.field("title").value(keyword))._toQuery());
                    b.should(PrefixQuery.of(p -> p.field("content").value(keyword))._toQuery());

                    // 초성 검색
                    b.should(PrefixQuery.of(p -> p.field("title.chosung").value(keyword))._toQuery());
                    b.should(PrefixQuery.of(p -> p.field("content.chosung").value(keyword))._toQuery());

                    // 중간 글자 검색
                    b.should(MatchQuery.of(m -> m.field("title.ngram").query(keyword))._toQuery());
                    b.should(MatchQuery.of(m -> m.field("content.ngram").query(keyword))._toQuery());

                    // fuzziness: "AUTO"는  오타 허용 검색 기능을 자동으로 켜주는 설정 -> 유사도 계산을 매번 수행하기 때문에 느림
                    //짧은 키워드에는 사용 xxx
                    //오타 허용 (오타허용은 match만 가능 )
                    if (keyword.length()>=3){
                        b.should(MatchQuery.of(m ->m.field("title").query(keyword).fuzziness("AUTO"))._toQuery());
                        b.should(MatchQuery.of(m ->m.field("content").query(keyword).fuzziness("AUTO"))._toQuery());
                    }

                    return b;
                })._toQuery();
                // BoolQuery: 복수 조건 조합 시 사용
                // 키워드를 어떻게 분석해서 데이터를 보여줄지 기술
            }

            // ES 검색 요청 객체
            SearchRequest request = SearchRequest.of(q -> q
                    .index("board-index")
                    .query(query)
                    .from(from)
                    .size(size));

            SearchResponse<BoardEsDocument> response = client
                    .search(request, BoardEsDocument.class);

            List<BoardEsDocument> content = response.hits() // 검색된 문서 1개를 감싸고 있는 객체
                    .hits() // 결과 목록
                    .stream()
                    .map(Hit::source) // Hit 객체에서 실제 문서를 추출
                    .collect(Collectors.toList());

            // 문서 총 갯수
            long total = response.hits().total().value();

            // Spring에서 사용할 수 있도록 Page 객체로 변환
            return new PageImpl<>(content, PageRequest.of(page, size), total);

        } catch (IOException e) {
            log.error("검색 오류: {}", e.getMessage());
            throw new RuntimeException("검색 중 오류 발생", e);
        }

    }

    public void bulkIndexInsert(List<BoardEsDocument> documents) throws IOException {
        int batchSize = 1000;

        for (int i = 0; i < documents.size(); i+=batchSize) {

            int end = Math.min(documents.size(), i+batchSize);

            List<BoardEsDocument> batch = documents.subList(i, end);
            
            // ES의 bulk 요청을 담을 빌더
            BulkRequest.Builder br = new BulkRequest.Builder();

            for (BoardEsDocument document : batch) {
                br.operations(op -> op
                        .index(idx -> idx // 인덱스에 문서 저장
                                .index("board-index")
                                .id(document.getId()) // ID 수동 지정
                                .document(document) // 실 저장 문서 객체
                        ));
            }

            BulkResponse bulkResponse = client.bulk(br.build());

            if (bulkResponse.errors()) {
                for (BulkResponseItem item : bulkResponse.items()) {
                    if (item.error() != null) {
                        log.error("ES bulk index error ID: {}, EORROR: {}", item.id(), item.error());
                    }
                }
            }
        }
    }

}
