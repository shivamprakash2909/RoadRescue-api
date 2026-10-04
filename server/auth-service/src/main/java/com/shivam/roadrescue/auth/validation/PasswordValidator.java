package com.shivam.roadrescue.auth.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern LETTER_PATTERN = Pattern.compile(".*[a-zA-Z].*");

    public boolean isValid(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return false;
        }
        return DIGIT_PATTERN.matcher(password).matches() && LETTER_PATTERN.matcher(password).matches();
    }

    public void validate(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters long");
        }
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        if (!LETTER_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one letter");
        }
    }
}
