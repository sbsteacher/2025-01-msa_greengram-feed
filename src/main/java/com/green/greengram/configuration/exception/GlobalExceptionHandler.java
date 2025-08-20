package com.green.greengram.configuration.exception;

import com.green.greengram.configuration.model.ResultResponse;
import jakarta.persistence.PersistenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<?> handleSQLException(SQLException ex) {
        // SQLException 처리 로직
        return new ResponseEntity<>(new ResultResponse("데이터베이스 오류가 발생하였습니다.", null), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<?> handlePersistenceException(PersistenceException ex) {
        // PersistenceException 처리 로직
        return new ResponseEntity<>(new ResultResponse("MyBatis 오류가 발생했습니다.", null), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
