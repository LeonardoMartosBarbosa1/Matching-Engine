package OrderBook.Listeners;

import OrderBook.OrderBook;
import common.CancelReason;
import Orders.Order;
import common.ExecutionSide;
import common.RejectReason;

import java.time.Instant;
import java.util.EnumSet;

public interface OrderBookListener {
    void onUserPromptRejected(OrderBook orderBook, RejectReason rejectreason, String userInput);

    void onOrderCanceled(OrderBook orderbook, Order order, Instant cancelTime, CancelReason cancelReason);
    void onOrderAccepted(OrderBook orderBook, Order order, Instant acceptTime);
    void onOrderRejected(OrderBook orderBook, Order order, Instant rejectTime, EnumSet<RejectReason> rejectReasons);
    void onOrderRest(OrderBook orderBook, Order order, Instant restTime);
    void onOrderExecuted(OrderBook orderBook, Order order, Instant lastExecuteTime, ExecutionSide executionSide, int sharesToExecute, long executionPrice, long executionID, long matchId);
    void onOrderCancelRejected(OrderBook orderBook, Instant rejectTime, RejectReason rejectReason);
    void onOrderReplaceRejected(OrderBook orderBook, Instant rejectTime, RejectReason rejectReason);
    void onOrderReplaceAccepted(OrderBook orderbook, Order order, Instant acceptTime, int prevQuantity, long prevPrice);
}
