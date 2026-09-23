package com.vaultcore.vbms.exception;
public final class ApiExceptions {
    private ApiExceptions() { }
    public static class NotFound extends RuntimeException { public NotFound(String message) { super(message); } }
    public static class BadRequest extends RuntimeException { public BadRequest(String message) { super(message); } }
    public static class InsufficientBalance extends RuntimeException { public InsufficientBalance() { super("Insufficient account balance"); } }
}
