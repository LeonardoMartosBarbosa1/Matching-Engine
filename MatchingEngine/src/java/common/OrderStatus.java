package common;

public enum OrderStatus {

    NEW	("NEW"),
    CANCELED("CANCELED"),
    REJECTED("REJECTED"),
    PARTIAL_FILLED ("PARTIAL_FILLED"),
    FILLED("FILLED"),
    PENDING_CANCEL ("PENDING_CANCEL");

    private final String valueName;

    OrderStatus(String orderStatusName) {
        this.valueName = orderStatusName;
    }
    public final String getValueName()
    {
        return this.valueName;
    }

}
