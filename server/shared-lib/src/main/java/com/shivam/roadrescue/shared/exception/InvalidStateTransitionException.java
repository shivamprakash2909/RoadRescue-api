package com.shivam.roadrescue.shared.exception;

public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }

    public InvalidStateTransitionException(String currentState, String requestedState) {
        super(String.format("Cannot transition booking from state '%s' to '%s'", currentState, requestedState));
    }
}
