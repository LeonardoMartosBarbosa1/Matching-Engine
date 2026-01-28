package common;

public enum ExecutionSide {

    TAKER("TAKER"),
    MAKER("MAKER");

    private final String valueName;

    ExecutionSide(String executionName) {
        this.valueName = executionName;
    }
    public final String getValueName()
    {
        return this.valueName;
    }

}
