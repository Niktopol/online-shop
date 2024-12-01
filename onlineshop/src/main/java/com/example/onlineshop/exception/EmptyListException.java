package com.example.onlineshop.exception;

public class EmptyListException extends RuntimeException{
    public EmptyListException(String message){
        super(message);
    }
}
