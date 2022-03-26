package com.tencent.tsf.femas.common.entity;


import java.util.Objects;

public class ErrorStatus {

    public static final ErrorStatus OK = Code.OK.ToStatus();
    public static final ErrorStatus INVALID_ARGUMENT = Code.INVALID_ARGUMENT.ToStatus();
    public static final ErrorStatus DEADLINE_EXCEEDED = Code.DEADLINE_EXCEEDED.ToStatus();
    public static final ErrorStatus NOT_FOUND = Code.NOT_FOUND.ToStatus();
    public static final ErrorStatus ALREADY_EXISTS = Code.ALREADY_EXISTS.ToStatus();
    public static final ErrorStatus PERMISSION_DENIED = Code.PERMISSION_DENIED.ToStatus();
    public static final ErrorStatus RESOURCE_EXHAUSTED = Code.RESOURCE_EXHAUSTED.ToStatus();
    public static final ErrorStatus OUT_OF_RANGE = Code.OUT_OF_RANGE.ToStatus();
    public static final ErrorStatus UNIMPLEMENTED = Code.UNIMPLEMENTED.ToStatus();
    public static final ErrorStatus INTERNAL = Code.INTERNAL.ToStatus();
    public static final ErrorStatus UNAVAILABLE = Code.UNAVAILABLE.ToStatus();
    public static final ErrorStatus UNAUTHENTICATED = Code.UNAUTHENTICATED.ToStatus();
    private final String message;
    private final Code code;

    public ErrorStatus(Code code) {
        this.code = code;
        this.message = code.name();
    }

    public ErrorStatus(Code code, String message) {
        if ("".equals(message)) {
            message = code.name();
        }
        this.code = code;
        this.message = message;
    }

    public Code getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public String StatusCode() {
        return Integer.toString(this.code.Value());
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (obj instanceof ErrorStatus) {
            return ((ErrorStatus) obj).getCode() == this.getCode();
        }
        return false;
    }

    /**
     * The set of canonical status codes. If new codes are added over time they must choose
     * a numerical value that does not collide with any previously used value.
     */
    public enum Code {
        OK(200),

        INVALID_ARGUMENT(400),

        DEADLINE_EXCEEDED(504),

        NOT_FOUND(404),

        ALREADY_EXISTS(409),

        PERMISSION_DENIED(403),

        RESOURCE_EXHAUSTED(429),

        OUT_OF_RANGE(431),

        UNIMPLEMENTED(501),

        INTERNAL(500),

        UNAVAILABLE(503),

        UNAUTHENTICATED(401),

        CIRCUIT_BREAKER(1000);

        private final int value;

        Code(int value) {
            this.value = value;
        }

        public int Value() {
            return this.value;
        }

        public String String() {
            return Integer.toString(this.Value());
        }

        public ErrorStatus ToStatus() {
            return new ErrorStatus(this);
        }
    }
}
