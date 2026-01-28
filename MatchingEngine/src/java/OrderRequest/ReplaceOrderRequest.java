package OrderRequest;

import common.UserRequestType;

public class ReplaceOrderRequest extends UserRequest {
    private final Long orderID;
    private final Integer quantity;
    private final Long price;
    public ReplaceOrderRequest(long orderID, Integer quantity, Long price) {
        super(UserRequestType.REPLACE_ORDER);
        this.orderID = orderID;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getOrderID(){
        return orderID;
    }
    public Integer getQuantity() {
        return quantity;
    }
    public Long getPrice() {
        return price;
    }
}
