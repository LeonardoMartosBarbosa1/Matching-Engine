package OrderBook;

import OrderBook.Listeners.OrderBookListener;
import common.CancelReason;
import Orders.Order;
import common.ExecutionSide;
import common.RejectReason;

import java.time.Instant;
import java.util.EnumSet;

import static common.UserRequestType.CANCEL_ORDER;
import static common.UserRequestType.REPLACE_ORDER;

public class OrderBookLogger implements OrderBookListener {
    private boolean isOn;
    public final void setLoggerOn(){ this.isOn = true;}

    @Override
    public void onUserPromptRejected(OrderBook orderbook, RejectReason rejectreason, String userInput) {
        if(!this.isOn) return;
        System.out.printf("[ ERROR | rejectReason=%s, userPrompt=%s ]", rejectreason.getValueName(), userInput);
        System.out.println();
    }

    @Override
    public void onOrderCanceled(OrderBook orderBook, Order order, Instant cancelTime, CancelReason cancelReason) {
        if(!this.isOn) return;
        System.out.printf("[ ORDER | orderID=%s, orderStatus=%s, cancelReason=%s, orderPendingQuantity=%d, orderFilledQuantity=%d, cancelTime=%s ]",
                order.getOrderID(), order.getOrderStatus(), cancelReason.getValueName(), order.getPendingQuantity(), order.getFilledQuantity(), cancelTime);
        System.out.println();
    }

    @Override
    public void onOrderAccepted(OrderBook orderBook, Order order, Instant acceptTime) {
        if(!this.isOn) return;
        System.out.printf("[ ORDER | orderID=%s, orderStatus=%s, orderPendingQuantity=%d, orderFilledQuantity=%d, acceptTime=%s ]",
                order.getOrderID(), order.getOrderStatus(), order.getPendingQuantity(), order.getFilledQuantity(), acceptTime);
        System.out.println();
    }

    @Override
    public void onOrderRejected(OrderBook orderBook, Order order, Instant rejectTime, EnumSet<RejectReason> rejectReasons) {
        if(!this.isOn) return;
        System.out.printf("[ ORDER | orderStatus=REJECTED, rejectReasons=%s ]", rejectReasons);
        System.out.println();
    }

    @Override
    public void onOrderRest(OrderBook orderBook, Order order, Instant restTime) {}

    @Override
    public void onOrderExecuted(OrderBook orderBook, Order order, Instant lastExecuteTime, ExecutionSide executionSide, int sharesExecuted, long executionPrice, long executionID, long matchID) {
        if(!this.isOn) return;
        System.out.printf("[ ORDER | orderID=%s, executionID=%s, matchID=%s, orderStatus=%s, executionSide=%s, executionQuantity=%s, executionPrice=%s ]",
                order.getOrderID(), executionID, matchID, order.getOrderStatus(), executionSide.getValueName(), sharesExecuted, executionPrice );
        System.out.println();
    }

    @Override
    public void onOrderCancelRejected(OrderBook orderBook, Instant rejectTime, RejectReason rejectReason ) {
        if(!this.isOn) return;
        System.out.printf("[ ORDER | request=%s, rejectReason=%s, rejectTime=%s ]", CANCEL_ORDER.getValueName(), rejectReason, rejectTime);
        System.out.println();
    }

    @Override
    public void onOrderReplaceRejected(OrderBook orderBook, Instant rejectTime, RejectReason rejectReason) {
        if(!this.isOn) return;
        System.out.printf("[ ORDER | request=%s, rejectReason=%s, rejectTime=%s ]", REPLACE_ORDER.getValueName(), rejectReason, rejectTime);
        System.out.println();
    }

    @Override
    public void onOrderReplaceAccepted(OrderBook orderbook, Order order, Instant acceptTime, int prevQuantity, long prevPrice) {
        if(!this.isOn) return;
        System.out.printf("[ ORDER | orderID=%s, request=%s, acceptTime=%s, previousPrice=%d, currentPrice=%d, previousPendingQuantity=%d, currentPendingQuantity=%d ]",
                            order.getOrderID(), REPLACE_ORDER.getValueName(), acceptTime, prevPrice, order.getPrice(), prevQuantity, order.getPendingQuantity() );
        System.out.println();
    }
}
