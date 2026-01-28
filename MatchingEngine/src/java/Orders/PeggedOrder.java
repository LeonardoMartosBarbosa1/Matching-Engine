package Orders;

import OrderBook.OrderRegistry;
import common.OrderType;
import common.PegType;
import common.RejectReason;
import common.Side;

//Pegged Orders as
public class PeggedOrder extends Order {
    private final PegType pegType;
    private Double price;
    private PeggedOrder nextPeggedOrder;
    private PeggedOrder prevPeggedOrder;

    public PeggedOrder(Side side, String clientID, int quantity, PegType pegType, Double price){
        super(OrderType.PEGGED, side, clientID, quantity);
        this.pegType = pegType;
        this.price = price;
    }

    @Override
    public final Double getPrice(){
        return this.price;
    }
    public final PegType getPegType(){return this.pegType;}
    public final void setNextPeggedOrder(PeggedOrder order){
        this.nextPeggedOrder = order;
    }
    public final void setPrevPeggedOrder(PeggedOrder order){
        this.prevPeggedOrder = order;
    }
    public final PeggedOrder getNextPeggedOrder(){return this.nextPeggedOrder;}
    public final PeggedOrder getPrevPeggedOrder(){return this.prevPeggedOrder;}

    @Override
    public void update(Integer quantity, Double price) {
        if (price != null){
            rejectReplace(RejectReason.PEGGED_USER_PRICE_REPLACE);
            return;
        }
        if (quantity != null){
            int prevQuantity = this.pendingQuantity;
            this.pendingQuantity = quantity;
            acceptReplace(prevQuantity, this.getPrice());
        }
    }

    //Price Update Due to the definition of Pegged Order and not a user Request
    public void updatePricePegged(double price){
        double prevPrice = this.price;
        this.price = price;
        acceptReplace(this.getPendingQuantity(), prevPrice);
    }

    @Override
    public void register(OrderRegistry orderBook){
        super.register(orderBook);
        orderBook.registerPegged( this);
    }

    @Override
    public void unRegister(OrderRegistry orderBook){
        super.unRegister(orderBook);
        orderBook.unRegisterPegged(this);
    }

}
