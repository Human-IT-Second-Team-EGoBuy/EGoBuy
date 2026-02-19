package com.avengers.matefarm.rag.dto.request;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class StatusPatchRequest {

  private Integer status; // 1=ACTIVE, 0=HIDDEN
  

}
