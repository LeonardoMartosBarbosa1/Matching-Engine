package common;

public enum UserRequestType {

    CREATE_MARKET_ORDER("CREATE_ORDER"),
    CREATE_LIMIT_ORDER("CREATE_LIMIT_ORDER"),
    CREATE_PEGGED_ORDER("CREATE_PEGGED_ORDER"),
    CANCEL_ORDER			("CANCEL_ORDER"),
    REPLACE_ORDER			("REPLACE_ORDER"),
    SHOW_ORDER_BOOK ("SHOW_ORDER_BOOK"),
    INVALID                 ("INVALID");

    private final String valueName;
    UserRequestType(String userRequestName) {this.valueName = userRequestName;}
    public final String getValueName() {return this.valueName;}
}