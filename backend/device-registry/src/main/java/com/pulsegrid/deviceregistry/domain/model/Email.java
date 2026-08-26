package com.pulsegrid.deviceregistry.domain.model;

import com.pulsegrid.deviceregistry.domain.exception.InvalidEmailException;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Email {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private final String value;

    public Email(String value){
        if(value == null || !EMAIL_PATTERN.matcher(value.trim()).matches()){
            throw new InvalidEmailException(value);
        }
        this.value = value.trim().toLowerCase();
    }

    public String getValue(){
        return value;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof Email)) return false;
        Email email = (Email) o;
        return value.equals(email.value);
    }
    @Override
    public int hashCode(){
        return Objects.hash(value);
    }
    @Override
    public String toString(){
        return value;
    }
}
