package org.example.backendproject.stompwebsocket.gpt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class GptService {

    //json문자열 <-> 자바객체, json객체
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${external.chatgpt.api.key}")
    private String API_KEY;
    private final static String GPT_MODEL_4 = "gpt-4.1";
    private final static String GPT_MODEL_4MINI = "gpt-4o-mini";

    public String gptMessage(String message) throws Exception{

        //API 호출을 위한 본문 작성
        Map<String,Object> requestBody  = new HashMap<>();
        requestBody .put("model", GPT_MODEL_4MINI);
        requestBody .put("input", message);

        //http 요청 작성
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/responses"))
                .header("Authorization", API_KEY)
                .header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody))) //본문 삽입
                .build();


        //요청 전송 및 응답 수신
        HttpClient  client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

        //응답을 Json으로 파싱
        JsonNode jsonNode = mapper.readTree(response.body());
        log.info("gpt 응답 : {}", jsonNode);

        //메세지 부분만 추출하여 반환
        String gptMessageResponse = jsonNode.get("output").get(0).get("content").get(0).get("text").asText();
        return gptMessageResponse;

    }

}