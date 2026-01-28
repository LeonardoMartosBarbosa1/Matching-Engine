package common;

public enum CancelReason {

    USER_REQUEST	("USER_REQUEST"),
    NO_LIQUIDITY	("NO_LIQUIDITY");

    private final String valueName;

    CancelReason(String cancelName) {
        this.valueName = cancelName;
    }
    public final String getValueName()
    {
        return this.valueName;
    }

}