package common;

public enum OrderType {
    MARKET("market"),
    LIMIT("limit"),
    PEGGED("pegged");
    private final String valueName;

    OrderType(String orderTypeName){
        this.valueName = orderTypeName;
    }
    public String getValueName(){
        return this.valueName;
    }

}