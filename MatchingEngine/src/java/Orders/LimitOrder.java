package Orders;

import common.OrderType;
import common.Side;

public class LimitOrder extends Order{
    private Double price;

    public LimitOrder( Side side, String clientID, int quantity, Double limitPrice){
        super(OrderType.LIMIT, side, clientID, quantity);
        this.price = limitPrice;
    }

    @Override
    public final Double getPrice(){
        return price;
    }

    @Override
    public void update(Integer quantity, Double newPrice) {
        int prevQuantity = pendingQuantity;
        double prevPrice = price;

        if(quantity != null)
            pendingQuantity = quantity;

        if(newPrice != null)
            price = newPrice;

        acceptReplace(prevQuantity, prevPrice);
    }
}
