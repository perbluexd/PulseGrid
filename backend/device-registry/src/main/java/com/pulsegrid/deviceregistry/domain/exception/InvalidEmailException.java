package com.pulsegrid.deviceregistry.domain.exception;

public class InvalidEmailException extends DomainException {
    public InvalidEmailException(String invalidValue){
        super(ErrorCode.INVALID_EMAIL_FORMAT, "Email inválido: " + invalidValue);
    }
}
