#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.exception;

import org.springframework.http.HttpStatus;

public class ServiceException extends RuntimeException {
    private HttpStatus status;
    private String messageKey;
    private Object[] messageArgs;
    private String errorCode;

    public ServiceException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public ServiceException(String messageKey, Object[] messageArgs) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArgs = messageArgs;
        this.status = HttpStatus.BAD_REQUEST;
    }

    public ServiceException(HttpStatus status, String errorCode, String messageKey, Object[] messageArgs) {
        super(messageKey);
        this.status = status;
        this.messageKey = messageKey;
        this.messageArgs = messageArgs;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessageKey() { return messageKey; }
    public Object[] getMessageArgs() { return messageArgs; }
    public String getErrorCode() { return errorCode; }
}