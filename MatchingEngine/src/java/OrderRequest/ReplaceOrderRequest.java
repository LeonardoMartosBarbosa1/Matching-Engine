package OrderRequest;

import common.UserRequestType;

public class ReplaceOrderRequest extends UserRequest {
    private final Long orderID;
    private final Integer quantity;
    private final Double price;
    public ReplaceOrderRequest(long orderID, Integer quantity, Double price) {
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
    public Double getPrice() {
        return price;
    }
}
