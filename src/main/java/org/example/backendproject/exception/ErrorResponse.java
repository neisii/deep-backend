package org.example.backendproject.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private int code;
    private String message;
    private String detail;
}
