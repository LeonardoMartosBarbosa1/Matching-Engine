package OrderBook;

import Orders.PeggedOrder;
import common.PegType;

public class PeggedOrders {
    private final PegType pegType;
    private PeggedOrder headOrder;
    private PeggedOrder tailOrder;

    public final PegType getPegType(){
        return this.pegType;
    }
    public final PeggedOrder getHeadOrder(){
        return this.headOrder;
    }
    public final PeggedOrder getTailOrder(){
        return this.tailOrder;
    }

    public PeggedOrders(PegType pegType){
        this.pegType = pegType;
    }

    void addOrder(PeggedOrder order){
        if(this.headOrder == null){
            this.headOrder = order;
            this.tailOrder = order;
            order.setPrevPeggedOrder(null);
            order.setNextPeggedOrder(null);
        }else{
            this.tailOrder.setNextPeggedOrder(order);
            order.setPrevPeggedOrder(this.tailOrder);
            this.tailOrder = order;
        }
        order.setNextPeggedOrder(null);
    }

    void removeOrder(PeggedOrder order){
        PeggedOrder prevOrder = order.getPrevPeggedOrder();
        PeggedOrder nextOrder = order.getNextPeggedOrder();

        if(prevOrder != null) prevOrder.setNextPeggedOrder(nextOrder);
        if(nextOrder != null) nextOrder.setPrevPeggedOrder(prevOrder);
        if(tailOrder == order) tailOrder = prevOrder;
        if(headOrder == order) headOrder = nextOrder;
    }
}
