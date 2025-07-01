package org.example.backendproject.board.elasticsearch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backendproject.board.dto.BoardDTO;
import org.springframework.data.elasticsearch.annotations.Document;

@JsonIgnoreProperties(ignoreUnknown = true) // ES index에 _class 속성 제외
@Document(indexName = "board-index")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardEsDocument {
    /* board-index.txt 에 따름 */

    @Id
    private String id; // ES document Id, 검색 시 String으로 받으려고 이와 같이 선언
    private String title;
    private String content;
    private String username;
    private Long userId;
    private String created_date;
    private String updated_date;
    private Long viewCount = 0L;


    public static BoardEsDocument from(BoardDTO dto) {
        return BoardEsDocument.builder()
                .id(String.valueOf(dto.getId()))
                .title(dto.getTitle())
                .content(dto.getContent())
                .username(dto.getUsername())
                .userId(dto.getUser_id())
                .created_date(dto.getCreated_date() != null ? dto.getCreated_date().toString() : null)
                .updated_date(dto.getUpdated_date() != null ? dto.getUpdated_date().toString() : null)
                .viewCount(dto.getViewCount())
                .build();
    }
}
