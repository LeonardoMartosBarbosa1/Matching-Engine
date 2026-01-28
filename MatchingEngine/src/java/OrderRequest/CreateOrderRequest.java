package OrderRequest;

import common.OrderType;
import common.PegType;
import common.Side;
import common.UserRequestType;

public class CreateOrderRequest extends UserRequest {

    private final OrderType orderType;
    private final Side side;
    private final int quantity;
    private final Double price;
    private final PegType pegType;
    private final String clientID;

    public CreateOrderRequest(UserRequestType userRequestType,
                              Side side,
                              int quantity,
                              Double price,
                              PegType pegType,
                              String clientID
    ) {
        super(userRequestType);
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.pegType = pegType;
        this.clientID = clientID;
        this.orderType =  switch (userRequestType) {
            case CREATE_MARKET_ORDER ->OrderType.MARKET;
            case CREATE_LIMIT_ORDER ->OrderType.LIMIT;
            case CREATE_PEGGED_ORDER ->OrderType.PEGGED;
            default -> null;
        };
    }

    public Side getSide(){ return this.side;}
    public int getQuantity(){ return this.quantity;}
    public Double getPrice(){ return this.price; }
    public PegType getPegType(){ return this.pegType; }
    public OrderType getOderType() { return this.orderType; }
    public String getClientID(){ return this.clientID; }


}