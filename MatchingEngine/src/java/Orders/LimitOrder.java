package Orders;

import common.OrderType;
import common.Side;

public class LimitOrder extends Order{
    private long price;

    public LimitOrder( Side side, String clientID, int quantity, long limitPrice){
        super(OrderType.LIMIT, side, clientID, quantity);
        this.price = limitPrice;
    }

    @Override
    public final Long getPrice(){
        return price;
    }

    @Override
    public void update(Integer quantity, Long newPrice) {
        int prevQuantity = pendingQuantity;
        long prevPrice = price;

        if(quantity != null)
            pendingQuantity = quantity;

        if(newPrice != null)
            price = newPrice;

        acceptReplace(prevQuantity, prevPrice);
    }
}
