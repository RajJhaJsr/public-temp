#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.exception;


class ApiException {
    private int status;
    private String message;
    private String errorCode;
    private String details;
    private long timestamp;

    public ApiException(int status, String message) {
        this(status, message, null, null);
    }

    public ApiException(int status, String message, String errorCode, String details) {
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
        this.details = details;
        this.timestamp = System.currentTimeMillis();
    }


    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
