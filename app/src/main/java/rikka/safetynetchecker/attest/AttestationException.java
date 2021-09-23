package rikka.safetynetchecker.attest;

public class AttestationException extends Exception {

    public AttestationException(String message) {
        super(message);
    }

    public AttestationException(String message, Throwable cause) {
        super(message, cause);
    }
}

