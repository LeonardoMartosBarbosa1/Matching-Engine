package Orders;

import OrderBook.Listeners.OrderListener;
import OrderBook.OrderRegistry;
import OrderBook.PriceLevel;
import common.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public abstract class Order {

    protected final OrderType type;
    protected final Side side;
    protected final String clientID;
    protected long orderID;
    protected final List<OrderListener> listeners= new ArrayList<>(2);

    private Instant rejectTime;
    private Instant acceptTime;
    private Instant cancelTime;
    private Instant lastRestTime;
    private Instant lastExecuteTime;
    private OrderStatus orderStatus;
    private final boolean isResting;

    protected int pendingQuantity;
    protected final int originalQuantity;
    protected int filledQuantity;
    protected int cancelQuantity;

    private PriceLevel priceLevel;
    private Order nextOrder;
    private Order prevOrder;

    Order(OrderType type, Side side, String clientID, int quantity ){
        this.side = side;
        this.type = type;
        this.clientID = clientID;
        this.pendingQuantity = quantity;
        this.originalQuantity = quantity;
        this.filledQuantity = 0;
        this.isResting = false;
    }

    public final OrderType getOrderType(){
        return this.type;
    }
    public final Side getSide(){
        return this.side;
    }
    public final long getOrderID(){ return this.orderID; }
    public final boolean isResting(){return this.isResting; }

    public final OrderStatus getOrderStatus(){ return this.orderStatus; }
    private void setOrderStatus(OrderStatus orderStatus ){ this.orderStatus = orderStatus; }

    public final PriceLevel getPriceLevel(){ return this.priceLevel;}
    public final void setPriceLevel(PriceLevel priceLevel){ this.priceLevel = priceLevel;}

    public final Order getNextOrder(){return this.nextOrder;}
    public final void setNextOrder(Order order){this.nextOrder = order;}
    public final Order getPrevOrder(){return this.prevOrder;}
    public final void setPrevOrder(Order order){this.prevOrder = order;}

    public final Instant getRejectTime(){ return this.rejectTime;}
    public final Instant getAcceptTime(){ return this.acceptTime;}
    public final Instant getCancelTime(){ return this.cancelTime;}
    public final Instant getLastRestTime(){return this.lastRestTime;}
    public final Instant getLastExecuteTime(){return this.lastExecuteTime;}

    abstract public Double getPrice();
    public final int getFilledQuantity(){
        return this.filledQuantity;
    }
    public final int getPendingQuantity(){ return this.pendingQuantity;}
    public final boolean needResting(){ return this.pendingQuantity != 0; }
    public final boolean isFullyFiled(){ return this.pendingQuantity == 0; }

    public void addListener(OrderListener orderListener){
        listeners.add(orderListener);
    }
    public void removeListener(OrderListener orderListener){
        listeners.remove(orderListener);
    }

    public void register(OrderRegistry orderBook){
        orderBook.registerOrders(this);
    }
    public void unRegister(OrderRegistry orderBook){
        orderBook.unRegisterOrders(this);
    }

    public abstract void update(Integer quantity, Double price);

    public void reject(EnumSet<RejectReason> rejectReasons){
        this.rejectTime = Instant.now();
        setOrderStatus(OrderStatus.REJECTED);

        int x = listeners.size();
        for(int i = x - 1; i >= 0; i--) {
            listeners.get(i).onOrderRejected(this, this.rejectTime, rejectReasons);
        }
    }

    public void accept(long orderID){
        this.orderID = orderID;
        this.acceptTime = Instant.now();
        setOrderStatus(OrderStatus.NEW);

        int x = listeners.size();
        for(int i = x - 1; i >= 0; i--) {
            listeners.get(i).onOrderAccepted(this, this.acceptTime);
        }
    }

    public void cancel(CancelReason cancelReason){
        this.cancelTime = Instant.now();
        this.cancelQuantity = this.pendingQuantity;
        setOrderStatus(OrderStatus.CANCELED);

        int x = listeners.size();
        for(int i = x - 1; i >= 0; i--) {
            listeners.get(i).onOrderCancelled(this, this.cancelTime, cancelReason);
        }
    }

    public void rest(){
        this.lastRestTime = Instant.now();
        int x = listeners.size();
        for(int i = x - 1; i >= 0; i--) {
            listeners.get(i).onOrderRest(this, this.lastRestTime );
        }
    }

    public void execute(double executionPrice, ExecutionSide executionSide, int sharesToExecute, long executionID, long matchId, Instant executionTime ){
        setOrderStatus( pendingQuantity > sharesToExecute? OrderStatus.PARTIAL_FILLED : OrderStatus.FILLED );
        int executedShares = Math.min(getPendingQuantity(), sharesToExecute);

        this.pendingQuantity -= executedShares;
        this.filledQuantity += executedShares;
        this.lastExecuteTime = executionTime;
        int x = listeners.size();
        for(int i = x - 1; i >= 0; i--) {
            listeners.get(i).onOrderExecuted(this, this.lastExecuteTime, executionSide, executedShares,
                    executionPrice, executionID, matchId);
        }
    }

    public void rejectReplace(RejectReason rejectReason){
        int x = listeners.size();
        for(int i = x - 1; i >= 0; i--) {
            listeners.get(i).onOrderReplaceRejected(this, Instant.now(), rejectReason);
        }
    }

    public void acceptReplace(int prevQuantity, double prevPrice){
        int x = listeners.size();
        for(int i = x - 1; i >= 0; i--) {
            listeners.get(i).onOrderReplaceAccepted(this, Instant.now(), prevQuantity, prevPrice );
        }
    }
}

