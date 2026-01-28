package common;

public enum RejectReason {

    //ORDER CREATION Rejections
    BAD_INPUT_FORMATTING("BAD_INPUT_FORMATTING"),
    WRONG_SIDE			("WRONG_SIDE"),
    WRONG_PEGTYPE		("WRONG_PEGTYPE"),
    WRONG_QUANTITY		("WRONG_QUANTITY"),
    NO_ASK_TO_PEG ("NO_ASK_TO_PEG"),
    NO_BID_TO_PEG ("NO_BID_TO_PEG"),

    //ORDER CANCEL and REPLACE Rejections
    ORDER_NOT_EXISTS    ("ORDER_NOT_EXISTS"),
    PEGGED_USER_PRICE_REPLACE("PEGGED_USER_PRICE_REPLACE");

    private final String valueName;

    RejectReason(String rejectName) {this.valueName = rejectName;}
    public final String getValueName() {return this.valueName;}
}