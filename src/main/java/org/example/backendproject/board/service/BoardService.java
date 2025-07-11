package org.example.backendproject.board.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backendproject.board.dto.BoardDTO;
import org.example.backendproject.board.elasticsearch.dto.BoardEsDocument;
import org.example.backendproject.board.elasticsearch.service.BoardEsService;
import org.example.backendproject.board.entity.Board;
import org.example.backendproject.board.repository.BatchRepository;
import org.example.backendproject.board.repository.BoardRepository;
import org.example.backendproject.user.entity.User;
import org.example.backendproject.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    private final BatchRepository batchRepository;
    private final EntityManager em;

    private final BoardEsService boardEsService;

    /** 글 등록 **/
    @Transactional
    public BoardDTO createBoard(BoardDTO boardDTO) {

        // userId(PK)를 이용해서 User 조회
        if (boardDTO.getUser_id() == null)
            throw new IllegalArgumentException("userId(PK)가 필요합니다!");

        // 연관관계 매핑!
        // 작성자 User 엔티티 조회 (userId 필요)
        User user = userRepository.findById(boardDTO.getUser_id())
                .orElseThrow(() -> new IllegalArgumentException("작성자 정보가 올바르지 않습니다."));

        /** mysql 저장 **/
        Board board = new Board();
        board.setTitle(boardDTO.getTitle());
        board.setContent(boardDTO.getContent());
        // 연관관계 매핑!
        board.setUser(user);

        Board saved = boardRepository.save(board);
        // RDB 저장


        BoardEsDocument doc = BoardEsDocument.builder()
                .id(String.valueOf(board.getId()))
                .title(board.getTitle())
                .content(board.getContent())
                .userId(board.getUser().getId())
                .username(board.getUser().getUserProfile().getUsername())
                .created_date(String.valueOf(board.getCreated_date()))
                .updated_date(String.valueOf(board.getUpdated_date()))
                .build();

        boardEsService.save(doc);
        // ES index 저장

        return toDTO(saved);
    }


    /** 게시글 상세 조회 **/
    public BoardDTO getBoardDetail(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음: " + boardId));

        // Mysql 조회 수 증가
        board.setViewCount(board.getViewCount() + 1);

        // ES 조회 수 증가
        BoardEsDocument esDocument = boardEsService.findById(String.valueOf(boardId))
                .orElseThrow(() -> new IllegalArgumentException("ES에 게시물 없음 : " + boardId));
        esDocument.setViewCount(board.getViewCount());
        boardEsService.save(esDocument);

        return toDTO(board);
    }

    /** 게시글 수정 **/
    @Transactional
    public BoardDTO updateBoard(Long boardId, BoardDTO dto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음: " + boardId));
        board.setTitle(dto.getTitle());
        board.setContent(dto.getContent());
        boardRepository.save(board);
        // RDB 저장

        BoardEsDocument doc = BoardEsDocument.builder()
                .id(String.valueOf(board.getId()))
                .title(board.getTitle())
                .content(board.getContent())
                .userId(board.getUser().getId())
                .username(board.getUser().getUserProfile().getUsername())
                .created_date(String.valueOf(board.getCreated_date()))
                .updated_date(String.valueOf(board.getUpdated_date()))
                .build();

        boardEsService.save(doc);
        // ES index 저장(ID가 동일하면 update함)

        return toDTO(board);
    }


    /** 게시글 삭제 **/
    @Transactional
    public void deleteBoard(Long userid,Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(()-> new IllegalArgumentException("사용자 정보가 없습니다"));

        if (!board.getUser().getId().equals(userid))
            throw new IllegalArgumentException("삭제 권한이 없습니다.");

        if (!boardRepository.existsById(boardId))
            throw new IllegalArgumentException("게시글 없음: " + boardId);

        boardRepository.deleteById(boardId);
        // RDB에서 삭제

        boardEsService.deleteById(String.valueOf(boardId));
        // ES Index에서 삭제
    }


    /** 페이징 적용 전 **/
    /** 페이징 적용 전 **/
    /** 페이징 적용 전 **/
    // 게시글 전체 목록
    @Transactional(readOnly = true)
    public List<BoardDTO> getBoardList() {
        return boardRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    // 게시글 검색  페이징 아님
    public List<BoardDTO> searchBoards(String keyword) {
        return boardRepository.searchKeyword(keyword);
    }




    /** 페이징 적용 후 **/
    /** 페이징 적용 후 **/
    /** 페이징 적용 후 **/
    //페이징 전체 목록
    public Page<BoardDTO> getBoards(int page, int size) {
        return boardRepository.findAllPaging(PageRequest.of(page, size)); //페이저블에 페이징에대한 정보를 담아서 레포지토리에 전달하는 역할
        //    return boardRepository.findAllWithDto(PageRequest.of(page, size, Sort.by("id").ascending())); //함수로 정렬
    }
    //페이징 검색 목록
    public Page<BoardDTO> searchBoardsPage(String keyword, int page, int size) {
        return boardRepository.searchKeywordPaging(keyword, PageRequest.of(page, size));
    }

    // Entity → DTO 변환
    private BoardDTO toDTO(Board board) {
        BoardDTO dto = new BoardDTO();
        dto.setId(board.getId());
        dto.setTitle(board.getTitle());
        dto.setContent(board.getContent());

        dto.setUser_id(board.getUser().getId());
        dto.setUsername(board.getUser() != null ? board.getUser().getUserProfile().getUsername() : null); // ★ username!

        dto.setCreated_date(board.getCreated_date());
        dto.setUpdated_date(board.getUpdated_date());

        dto.setViewCount(board.getViewCount());

        return dto;
    }




    /** 배치작업 **/
    @Transactional
    public void batchSaveBoard(List<BoardDTO> boardDTOList) {

        int batchsize = 1000; //한번에 처리할 배치 크기
        for (int i = 0; i < boardDTOList.size(); i+=batchsize) { //i는 1000씩 증가
            //전체 데이터를 1000개씩 잘라서 배치리스트에 담습니다.

            int end = Math.min(boardDTOList.size(), i+batchsize); //두개의 숫자중에 작은 숫자를 반황ㄴ
            List<BoardDTO> batchList = boardDTOList.subList(i, end);

            //전체 데이터에서 1000씩 작업을 하는데 마지막 데이터가 1000개가 안될수도있으니
            //Math.min()으로 전체 크기를 넘지 않게 마지막 인덱스를 계산해서 작업합니다.


            //내가 넣은 데이터만 엘라스틱서치에 동기화하기 위해 uuid 생성
            String batchKey = UUID.randomUUID().toString();
            for (BoardDTO dto : batchList) {
                dto.setBatchkey(batchKey);
            }


            // 1. MySQL로 INSERT
            batchRepository.batchInsert(batchList);

            // 2. RDB에서 저장한 데이터 조회
            List<BoardDTO> saveBoards = batchRepository.findByBatchkey(batchKey);

            // 3. ES 인덱싱위해 ES용으로 변환
            List<BoardEsDocument> documents = saveBoards.stream()
                    .map(BoardEsDocument::from)
                    .toList();

            try {
                boardEsService.bulkIndexInsert(documents);
            } catch (IOException e) {
                log.error("[{}][BATCH]ES bulk index error: {}", "BOARD", e.getMessage(), e);
            }

        }

   }

   // ES 전체 인덱싱
    public void batchSaveEsBoard() throws Exception {
        // 1. RDB에서 전체 데이터 수 조회
        long boardCount = boardRepository.count();

        int page = 0;
        int batchsize = 1000; //한번에 처리할 배치 크기
        for (int i = 0; i < boardCount; i+=batchsize) {
            int end = Math.min(Math.toIntExact(boardCount), i+batchsize);

            // 2. 단위 데이터 가져와서
            Page<BoardDTO> dtoPage = boardRepository.findAllPaging(PageRequest.of(page, end));

            log.info("page info {} per count {}", page, dtoPage.stream().count());

            List<BoardDTO> batchList = dtoPage.getContent();

            String batchKey;

            for (BoardDTO dto : batchList) {
                batchKey = UUID.randomUUID().toString();

                if (dto.getBatchkey() == null) {
                    dto.setBatchkey(batchKey);
                }
            }

            // 3. ES 인덱싱위해 ES용으로 변환
            List<BoardEsDocument> documents = batchList.stream()
                    .map(BoardEsDocument::from)
                    .toList();

            if (documents.isEmpty()) {
                throw new NoResultException("ESDocument 변환 실패");
            }

            try {
                boardEsService.bulkIndexInsert(documents);
            } catch (IOException e) {
                log.error("[{}][BATCH]ES bulk index error: {}", "BOARD", e.getMessage(), e);
            }

            page++;

        }

   }


    @Transactional
    public void boardSaveAll(List<Board> boardList){
        for (int i = 0; i<boardList.size(); i++) {
            em.persist(boardList.get(i));
            if (i % 1000 == 0){
                em.flush();
                em.clear();
            }
        }

    }
}