package com.avengers.matefarm.rag.controller;

import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.rag.dto.request.MessageSendRequest;
import com.avengers.matefarm.rag.dto.response.SendMessageData;
import com.avengers.matefarm.rag.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-chat")
public class RagController {

    private final RagService ragService;

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseDTO<SendMessageData> sendMessage(
            @PathVariable("conversationId") Long conversationId,
            @RequestBody MessageSendRequest req
    ) {
        SendMessageData data = ragService.sendMessage(conversationId, req.getContent(), req.getClientMessageId());
        return ResponseDTO.ok(data); // 너희 공통 응답 포맷

    }
}
