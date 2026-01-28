package OrderBook;

import Orders.Order;
import Orders.PeggedOrder;

public interface OrderRegistry {
    void registerPegged(PeggedOrder order);
    void registerOrders(Order order);
    void unRegisterPegged(PeggedOrder order);
    void unRegisterOrders(Order order);
}