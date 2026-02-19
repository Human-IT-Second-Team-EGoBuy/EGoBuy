package com.avengers.matefarm.rag.controller;

import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.rag.dto.response.ConversationDto;
import com.avengers.matefarm.rag.service.ChatConversationService;
import com.avengers.matefarm.rag.dto.request.StatusPatchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-chat")
public class ChatConversationController {

    private final ChatConversationService chatConversationService;

    @GetMapping("/conversations")
    public ResponseDTO<List<ConversationDto>> list() {
    return ResponseDTO.ok(chatConversationService.list());
}
    @PostMapping("/conversations")
    public ResponseDTO<ConversationDto> create() {
        return ResponseDTO.ok(chatConversationService.createConversation());
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseDTO<ConversationDto> detail(
            @PathVariable long conversationId,
            @RequestParam(value = "includeMessages", defaultValue = "true") boolean includeMessages
    ) {
        return ResponseDTO.ok(chatConversationService.getConversation(conversationId, includeMessages));
    }


    @PatchMapping("/conversations/{conversationId}/status")
   public ResponseDTO<Void> patchStatus(
            @PathVariable long conversationId,
            @RequestBody StatusPatchRequest req
    ) {
        chatConversationService.patchConversationStatus(conversationId, req.getStatus());
        return ResponseDTO.ok(null);
    }

}
