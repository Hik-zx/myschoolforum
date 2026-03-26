package com.example.entity.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AddCommentDTO {
    @Min(1)
    int tid;
    String content;
    @Min(-1)
    int quote;
}
