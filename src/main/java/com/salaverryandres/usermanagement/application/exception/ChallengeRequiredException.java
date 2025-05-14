package com.salaverryandres.usermanagement.application.exception;

import lombok.Getter;

@Getter
public class ChallengeRequiredException extends RuntimeException {
    private final String challenge;
    private final String session;

    public ChallengeRequiredException(String challenge, String session, String message) {
        super(message);
        this.challenge = challenge;
        this.session = session;
    }

}

