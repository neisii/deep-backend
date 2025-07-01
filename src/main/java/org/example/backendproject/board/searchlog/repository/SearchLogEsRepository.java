package org.example.backendproject.board.searchlog.repository;

import org.example.backendproject.board.searchlog.domain.SearchLogDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchLogEsRepository extends ElasticsearchRepository<SearchLogDocument, String> {

    // ES 저장/검색용

}
