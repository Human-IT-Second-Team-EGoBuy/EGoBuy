package com.avengers.matefarm.rag.controller;

import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.rag.dto.MessageSendRequest;
import com.avengers.matefarm.rag.dto.SendMessageData;
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
            @PathVariable("conversationId") long conversationId,
            @RequestBody MessageSendRequest req
    ) {
        SendMessageData data = ragService.sendMessage(conversationId, req.getContent());
        return ResponseDTO.ok(data);
    }
}
