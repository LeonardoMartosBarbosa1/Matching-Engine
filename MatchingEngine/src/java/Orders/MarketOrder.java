package Orders;

import common.OrderType;
import common.Side;

public class MarketOrder extends Order {
    public MarketOrder(Side side, String clientID, int quantity){
        super(OrderType.MARKET, side, clientID, quantity);
    }

    @Override
    public final Long getPrice(){
        return null;
    }

    @Override
    public void update(Integer quantity, Long price) {}
}
