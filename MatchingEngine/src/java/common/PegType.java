package common;

public enum PegType {
    BID("bid"),
    ASK("ask"),
    INVALID("invalid");
    private final String valueName;

    PegType(String pegName){
        this.valueName = pegName;
    }

    public static PegType fromString(String pegName){
        return switch (pegName) {
            case "bid" -> BID;
            case "ask" -> ASK;
            default -> INVALID;
        };
    }

    public static Side sideFromPegType(PegType pegType){
        return switch (pegType){
            case ASK-> Side.SELL;
            case BID -> Side.BUY;
            case INVALID ->Side.INVALID;
        };
    }
    public String getValueName(){
        return this.valueName;
    }
}