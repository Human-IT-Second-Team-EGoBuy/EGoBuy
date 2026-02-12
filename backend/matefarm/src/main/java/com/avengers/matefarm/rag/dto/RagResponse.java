package com.avengers.matefarm.rag.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RagResponse {
    private String answer;
    private String intent;
    private List<Map<String, Object>> citations;
}
