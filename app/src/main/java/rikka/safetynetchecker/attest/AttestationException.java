package rikka.safetynetchecker.attest;

import androidx.annotation.NonNull;

public class AttestationException extends Exception {

    public AttestationException(String message) {
        super(message);
    }

    public AttestationException(String message, Throwable cause) {
        super(message, cause);
    }

    @NonNull
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
