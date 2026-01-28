package common;

public enum Side {
    SELL("sell"),
    BUY("buy"),
    INVALID("invalid");

    private final String valueName;

    Side(String sideName){this.valueName = sideName;}
    public String getValue(){ return this.valueName;}

    public static Side fromString(String sideName){
        return switch (sideName) {
            case "sell" -> SELL;
            case "buy" -> BUY;
            default -> INVALID;
        };
    }
    public static Side getOtherSide(Side side){
        return switch (side) {
            case SELL ->BUY;
            case BUY ->SELL;
            default ->INVALID;
        };
    }

    public static PegType pegTypeFromSide(Side side){
        return switch (side){
            case SELL-> PegType.ASK;
            case BUY -> PegType.BID;
            case INVALID ->PegType.INVALID;
        };
    }
}
