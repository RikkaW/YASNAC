package rikka.safetynetchecker.attest;

import androidx.annotation.NonNull;

public final class AttestationException extends Exception {

    @NonNull
    private final String message;

    public AttestationException(@NonNull String message) {
        super(message);
        this.message = message;
    }

    public AttestationException(@NonNull String message, @NonNull Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    @NonNull
    @Override
    public String getMessage() {
        return message;
    }

    @NonNull
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
