package OrderRequest;

import common.UserRequestType;

public class CancelOrderRequest extends UserRequest{

    private final long orderID;
    public CancelOrderRequest(long orderID){
        super(UserRequestType.CANCEL_ORDER);
        this.orderID = orderID;
    }

    public long getOrderIDToCancel(){return this.orderID;}
}
