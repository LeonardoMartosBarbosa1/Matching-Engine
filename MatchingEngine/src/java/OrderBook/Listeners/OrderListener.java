package OrderBook.Listeners;

import common.CancelReason;
import Orders.Order;
import common.ExecutionSide;
import common.RejectReason;

import java.time.Instant;
import java.util.EnumSet;

public interface OrderListener {
    void onOrderAccepted(Order order, Instant acceptTime );
    void onOrderRejected(Order order, Instant rejectTime, EnumSet<RejectReason> rejectReasons);
    void onOrderCancelled(Order order, Instant cancelTime, CancelReason cancelReason);
    void onOrderRest(Order order, Instant restTime);
    void onOrderExecuted(Order order, Instant lastExecuteTime, ExecutionSide executionSide, int sharesToExecute, long executionPrice, long executionID, long matchId);
    void onOrderReplaceRejected(Order order, Instant rejectTime, RejectReason rejectReason);
    void onOrderReplaceAccepted(Order order, Instant acceptTime, int prevQuantity, long prevPrice);
}
