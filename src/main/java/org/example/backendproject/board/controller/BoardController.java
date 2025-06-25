package org.example.backendproject.board.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.backendproject.board.dto.BoardDTO;
import org.example.backendproject.board.entity.Board;
import org.example.backendproject.board.service.BoardService;

import org.example.backendproject.security.core.CustomUserDetails;
import org.example.backendproject.user.repository.UserRepository;
import org.example.backendproject.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    private final UserRepository userRepository;
    private final UserService userService;

    /** 글 작성 **/
    @PostMapping
    public ResponseEntity<BoardDTO> createBoard(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody BoardDTO boardDTO) throws JsonProcessingException {
        boardDTO.setUser_id(userDetails.getId());
        System.out.println("boardDTO 값 "+new ObjectMapper().writeValueAsString(boardDTO));
        BoardDTO created = boardService.createBoard(boardDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** 게시글 상세 조회 **/
    @GetMapping("/{id}")
    public ResponseEntity<BoardDTO> getBoardDetail(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        Long userid = userDetails.getId();

        // 글을 읽으려면 유효한 사용자로 로그인이 되어 있어야 한다.
        if (userService.getMyInfo(userid) == null) {
            return ResponseEntity.status(NOT_FOUND).build();
        }

        return ResponseEntity.ok(boardService.getBoardDetail(id));
    }

    /** 게시글 수정 **/
    @PutMapping("/{id}")
    public ResponseEntity<BoardDTO> updateBoard(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, @RequestBody BoardDTO boardDTO) {
        Long userid = userDetails.getId();

        // 내가 작성한 글만 수정할 수 있다.
        if (!boardService.getBoardDetail(id).getUser_id().equals(userid)) {
            return ResponseEntity.status(NOT_FOUND).build();
        }

        return ResponseEntity.ok(boardService.updateBoard(id, boardDTO));
    }

    /**
     * 게시글 삭제
     **/
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        Long userid = userDetails.getId();

        // 내가 쓴 글만 삭제할 수 있다.
        if (!boardService.getBoardDetail(id).getUser_id().equals(userid)) {
            return ResponseEntity.status(NOT_FOUND).build();
        }

        boardService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }



//    //페이징 적용 전
//    @GetMapping
//    public ResponseEntity<List<BoardDTO>> getBoardList() {
//        return ResponseEntity.ok(boardService.getBoardList());
//    }
//
//    //페이징 적용 전
//    @GetMapping("/search")
//    public List<BoardDTO> search(@RequestParam String keyword) {
//        return boardService.searchBoards(keyword);
//    }


    /** 페이징 적용 **/
    //페이징 적용 전체 목록보기
    //기본값은 0페이지 첫페이지입니다 페이지랑 10개 데이터를 불러옴
    @GetMapping
    public Page<BoardDTO> getBoards(
            @RequestParam(defaultValue = "0") int page, //  첫 페이지부터
            @RequestParam(defaultValue = "10") int size // 열번째 페이지까지 표시
    ) {
        return boardService.getBoards(page, size);
    }

    /** 주석해제하기 front  **/
    //페이징 적용 검색
    @GetMapping("/search")
    public Page<BoardDTO> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return boardService.searchBoardsPage(keyword, page, size);
    }


    /** 게시판 글 쓰기 배치 작업 **/
    @PostMapping("/batchInsert")
    public String batchInsert(@RequestBody List<BoardDTO> boardDTOList) {
        boardService.batchSaveBoard(boardDTOList);
        return "ok";
    }


    @PostMapping("/jpaBatchInsert")
    public String jpaBatchInsert(@RequestBody List<Board> board) {
        boardService.boardSaveAll(board);
        return "ok";
    }
}