package com.avengers.matefarm.rag.controller;

import com.avengers.matefarm.common.PageResponseDTO;
import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.rag.dto.response.ConversationDto;
import com.avengers.matefarm.rag.service.ChatConversationService;
import com.avengers.matefarm.rag.dto.request.StatusPatchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-chat")
public class ChatConversationController {

    private final ChatConversationService chatConversationService;

    @GetMapping("/conversations")
    public ResponseDTO<PageResponseDTO<ConversationDto>> list(
        @RequestParam(name = "page", defaultValue = "1") int pageNo,
        @RequestParam(name = "size", defaultValue = "50") int elementsPerPage,
        @RequestParam(name = "pageSize", defaultValue = "10") int pageSize
    ) {
    return ResponseDTO.ok(chatConversationService.list(pageNo, elementsPerPage, pageSize));
    }
    
    @PostMapping("/conversations")
    public ResponseDTO<ConversationDto> create() {
        return ResponseDTO.ok(chatConversationService.createConversation());
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseDTO<ConversationDto> detail(
            @PathVariable Long conversationId,
            @RequestParam(value = "includeMessages", defaultValue = "true") Boolean includeMessages
    ) {
        return ResponseDTO.ok(chatConversationService.getConversation(conversationId, includeMessages));
    }


    @PatchMapping("/conversations/{conversationId}/status")
   public ResponseDTO<Void> patchStatus(
            @PathVariable Long conversationId,
            @RequestBody StatusPatchRequest req
    ) {
        chatConversationService.patchConversationStatus(conversationId, req.getStatus());
        return ResponseDTO.ok(null);
    }

}
