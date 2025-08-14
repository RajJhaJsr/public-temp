#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.event;

public enum Status {
    PENDING,
    PROCESSING,
    PROCESSED,
    FAILED,
    RETRYING,
    SUCCESS,
    DEAD_LETTER
}