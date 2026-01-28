package OrderBook;

import OrderBook.Listeners.OrderListener;
import Orders.Order;
import common.CancelReason;
import common.ExecutionSide;
import common.RejectReason;
import common.Side;

import java.time.Instant;
import java.util.EnumSet;

public class PriceLevel implements OrderListener {
    private final Side side;
    private final double price;
    private long sharesQuantity;
    private long ordersQuantity;
    private Order headOrder;
    private Order tailOrder;
    PriceLevel nextPriceLevel = null;
    PriceLevel prevPriceLevel = null;

    public final boolean isEmpty(){return this.ordersQuantity == 0;}

    public final double getPrice(){return this.price;}
    public final long getSharesQuantity(){return this.sharesQuantity;}
    public final Side getSide(){return this.side;}

    public final Order getHeadOrder(){return this.headOrder;}
    public final Order getTailOrder(){return this.tailOrder;}

    public final PriceLevel getNextPriceLevel(){ return this.nextPriceLevel; }
    public final void setNextPriceLevel(PriceLevel priceLevel){ this.nextPriceLevel = priceLevel; }
    public final PriceLevel getPrevPriceLevel(){ return this.prevPriceLevel; }
    public final void setPrevPriceLevel(PriceLevel priceLevel ){ this.prevPriceLevel = priceLevel; }

    public PriceLevel(Side side, double price){
        this.side = side;
        this.price = price;
    }

    public boolean hasHigherPriority(Side side, double price){
        return side == Side.BUY ? price >= this.price : price<= this.price;
    }

    public boolean isPriceTradeable( double price ){
        return side==Side.BUY ? price<= this.price : price>= this.price;
    }

    //We are Assuming a FIFO in the PriceLevel
    //O(1)
    void addOrder(Order order){
        if(this.headOrder == null){
            this.headOrder = order;
            this.tailOrder = order;
            order.setPrevOrder(null);
        }else{
           this.tailOrder.setNextOrder(order);
           order.setPrevOrder(this.tailOrder);
           this.tailOrder = order;
        }
        this.ordersQuantity ++;
        order.setNextOrder(null);
        order.addListener(this);
    }

    //O(1)
    void removeOrder(Order order){
        Order prevOrder = order.getPrevOrder();
        Order nextOrder = order.getNextOrder();

        if(prevOrder != null) prevOrder.setNextOrder(nextOrder);
        if(nextOrder != null) nextOrder.setPrevOrder(prevOrder);
        if(this.tailOrder == order) this.tailOrder = prevOrder;
        if(this.headOrder == order) this.headOrder = nextOrder;

        this.ordersQuantity--;
        order.removeListener(this);
    }

    @Override
    public void onOrderCancelled(Order order, Instant cancelTime, CancelReason cancelReason) {
        this.sharesQuantity -= order.getPendingQuantity();
        this.removeOrder(order);
    }

    @Override
    public void onOrderRest(Order order, Instant restTime) {
        this.sharesQuantity += order.getPendingQuantity();
    }

    @Override
    public void onOrderExecuted( Order order, Instant lastExecuteTime, ExecutionSide executionSide, int executedShares, double executionPrice, long executionID, long matchId) {
        if(order.isFullyFiled())
            removeOrder(order);
        this.sharesQuantity -= executedShares;
    }

    @Override
    public void onOrderReplaceAccepted(Order order, Instant acceptTime, int prevQuantity, double prevPrice) {
        this.sharesQuantity -= prevQuantity;
        if(order.getPrice() != prevPrice){
            removeOrder(order);
        }else {
            this.sharesQuantity += order.getPendingQuantity();
        }
    }

    @Override
    public void onOrderAccepted(Order order, Instant time) {}

    @Override
    public void onOrderRejected(Order order, Instant rejectTime, EnumSet<RejectReason> rejectReasons) {}

    @Override
    public void onOrderReplaceRejected( Order order, Instant time, RejectReason rejectReason) {}


}
