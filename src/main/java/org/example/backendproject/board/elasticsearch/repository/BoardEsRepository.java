package org.example.backendproject.board.elasticsearch.repository;

import org.example.backendproject.board.elasticsearch.dto.BoardEsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardEsRepository extends ElasticsearchRepository<BoardEsDocument, String> {

    // 문서 ID로 데이터 삭제하는 쿼리 메서드
    void deleteById(String id);

}
