#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.event;

public enum EventType {
    ORDER_PLACED,
    ORDER_UPDATED,
    ORDER_CANCELLED,
    TRADE_EXECUTED,
    TRADE_CANCELLED
}