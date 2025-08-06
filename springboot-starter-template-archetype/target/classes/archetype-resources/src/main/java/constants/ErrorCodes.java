#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.constants;

public enum ErrorCodes {
    AUTH_INVALID_CREDENTIALS("STM-AUTH_001", "auth.invalid.credentials"),
    AUTH_TOKEN_EXPIRED("STM-AUTH_002", "auth.token.expired"),
    USER_NOT_FOUND("STM-USR_001", "user.not.found"),
    VALIDATION_FIELD_REQUIRED("STM-VAL_001", "validation.field.required"),
    VALIDATION_FIELD_FORMAT("STM-VAL_002", "validation.invalid.format"),
    INTERNAL_ERROR("STM-SYS_001", "system.internal.error");

    private final String code;
    private final String messageKey;

    ErrorCodes(String code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }

    public String getCode() {
        return code;
    }

    public String getMessageKey() {
        return messageKey;
    }
}