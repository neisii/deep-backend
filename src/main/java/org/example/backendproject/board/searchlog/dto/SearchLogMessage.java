package org.example.backendproject.board.searchlog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchLogMessage {

    // kafka message format(dto)
    private String keyword; // 검색 키워드
    private String userId; // 검색한 유저 ID 
    private String searchedAt; // 검색한 시간
}
