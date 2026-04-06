package com.marcaaiback.dto.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDTO {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private Object message;
}
