package OrderBook;

import OrderBook.Listeners.OrderBookListener;
import OrderBook.Listeners.OrderListener;
import OrderRequest.CancelOrderRequest;
import OrderRequest.ReplaceOrderRequest;
import OrderRequest.UserRequest;
import OrderRequest.CreateOrderRequest;
import Orders.*;
import common.*;

import java.time.Instant;
import java.util.*;

import static common.CancelReason.USER_REQUEST;
import static common.RejectReason.ORDER_NOT_EXISTS;

public class OrderBook implements OrderListener, OrderRegistry{
    //We are going to Consider a OrderBook per Asset
    //Trades between Same Client Order ID are allowed

    private long orderID = 0;
    private long execID = 0;
    private long matchID = 0;
    private long lastExecutedPrice;
    private final String asset;
    private final OrderBookLogger orderBookLogger;
    private final PromptParser userPromptParser;
    private final List<OrderBookListener> listeners= new ArrayList<>(1);
    private final Map<Long, Order> orders = new HashMap<>();
    private final Map<Side, PriceLevel> headPriceLevel = new HashMap<>();
    private final Map<Side, PriceLevel> tailPriceLevel = new HashMap<>();
    private final Map<PegType, PeggedOrders> peggedOrders = new HashMap<>();
    private final Map<Side, Map<Long, PriceLevel>> pricingLevelMap = new HashMap<>();
    private boolean isPeggedUpdating;

    //Market Data Feed Utility
    public long getLastExecutedPrice(){return this.lastExecutedPrice;}
    private void setUpdatePeggedSuppression(boolean isPeggedUpdating ){this.isPeggedUpdating = isPeggedUpdating;}
    private boolean isPeggedUpdateSuppressed(){return this.isPeggedUpdating;}

    public OrderBook(String asset){
        this.asset = asset;
        this.orderBookLogger = new OrderBookLogger();
        this.userPromptParser = new PromptParser();
        this.orderBookLogger.setLoggerOn();
        this.addListener(orderBookLogger);

        headPriceLevel.put(Side.BUY, null);
        headPriceLevel.put(Side.SELL, null);
        tailPriceLevel.put(Side.BUY, null);
        tailPriceLevel.put(Side.SELL, null);

        pricingLevelMap.put(Side.BUY, new HashMap<>());
        pricingLevelMap.put(Side.SELL, new HashMap<>());

        peggedOrders.put(PegType.BID, new PeggedOrders(PegType.BID));
        peggedOrders.put(PegType.ASK, new PeggedOrders(PegType.ASK));
    }

    public void addListener(OrderBookListener orderBookListener){
        this.listeners.add(orderBookListener);
    }
    public String getAsset(){return this.asset;
    }
    public final boolean hasBids(){
        PriceLevel priceLevel = headPriceLevel.get(Side.BUY);
        return priceLevel != null && priceLevel.getHeadOrder()!= null;
    }
    public final boolean hasAsks(){
        PriceLevel priceLevel = headPriceLevel.get(Side.SELL);
        return priceLevel != null && priceLevel.getHeadOrder()!= null;
    }

    public final Long getBestBidPrice(){
        return hasBids() ? headPriceLevel.get(Side.BUY).getHeadOrder().getPrice() : null;
    }

    public final Long getBestAskPrice(){
        return hasAsks() ? headPriceLevel.get(Side.SELL).getHeadOrder().getPrice() : null;
    }

    public final Long getBestPrice(Side side){
        return side == Side.BUY ? getBestBidPrice() : getBestAskPrice();
    }

    public final boolean isOrderPriceBest(Side side, long price){
        Long best = getBestPrice(side);
        if (best == null)
            return true;
        return side==Side.BUY ? price > best : price < best;
    }

    private void tryFillNewOrder(Order order){
        if (Objects.requireNonNull(order.getOrderType()) == OrderType.MARKET) {
            fillOrCancel(order);
        } else {
            fillOrRest(order);
        }
    }

    //Idea of this Function is for Market Orders to Work as an IOC
    private void fillOrCancel(Order order){
        fill(order);
        if (order.needResting())
            order.cancel(CancelReason.NO_LIQUIDITY);
    }

    private void fillOrRest(Order order){
        fill(order);
        if(order.needResting())
            restOrder(order);
    }

    private void fill(Order order){
        Side againstSide = Side.getOtherSide(order.getSide());
        boolean isMarketOrder = order.getOrderType() == OrderType.MARKET;
        for(PriceLevel priceLevel = headPriceLevel.get(againstSide); priceLevel != null; priceLevel = priceLevel.getNextPriceLevel()){
            if( !isMarketOrder && !priceLevel.isPriceTradeable(order.getPrice()) ) break;
            for(Order againstOrder = priceLevel.getHeadOrder(); againstOrder != null; againstOrder = againstOrder.getNextOrder()){
                int quantityToExecute = Math.min(order.getPendingQuantity(), againstOrder.getPendingQuantity());
                long executionPrice = againstOrder.getPrice();
                this.lastExecutedPrice = executionPrice;
                Instant executionTime = Instant.now();
                long matchID = this.matchID++;
                againstOrder.execute(executionPrice, ExecutionSide.MAKER, quantityToExecute, this.execID++, matchID, executionTime );
                order.execute(executionPrice, ExecutionSide.TAKER, quantityToExecute, this.execID++, matchID, executionTime );
                if(order.isFullyFiled())
                    return;
            }
        }
    }
    //Search in findPriceLevel will be designed to be linear, I'm assuming
    //that new created PricingLevels are more likely to be near the top
    //of the book making the Average Cost lower than some Trees. So
    //I Will take a heuristical approach
    private PriceLevel findPriceLevel(Side side, long price){
        if( this.pricingLevelMap.get(side).containsKey(price))
            return pricingLevelMap.get(side).get(price);

        PriceLevel newPriceLevel = new PriceLevel(side, price);
        pricingLevelMap.get(side).put(price, newPriceLevel);

        if(headPriceLevel.get(side) == null){
            headPriceLevel.put(side, newPriceLevel);
            tailPriceLevel.put(side, newPriceLevel);
            newPriceLevel.setPrevPriceLevel(null);
            newPriceLevel.setNextPriceLevel(null);
            return newPriceLevel;
        }

        for(PriceLevel priceLevel = headPriceLevel.get(side); priceLevel != null; priceLevel = priceLevel.getNextPriceLevel()){
            if( priceLevel.hasHigherPriority(side, price) ){
                PriceLevel prev = priceLevel.getPrevPriceLevel();
                if(prev!=null){
                    prev.setNextPriceLevel(newPriceLevel);
                } else{
                    headPriceLevel.put(side, newPriceLevel);
                }
                newPriceLevel.setPrevPriceLevel(prev);
                newPriceLevel.setNextPriceLevel(priceLevel);
                priceLevel.setPrevPriceLevel(newPriceLevel);
                return newPriceLevel;
            }
        }
        //Our new Price Level has the lowest priority
        tailPriceLevel.get(side).setNextPriceLevel(newPriceLevel);
        newPriceLevel.setPrevPriceLevel(tailPriceLevel.get(side));
        newPriceLevel.setNextPriceLevel(null);
        tailPriceLevel.put(side, newPriceLevel);
        return(newPriceLevel);
    }

    private void restOrder(Order order){

        if(isOrderPriceBest(order.getSide(), order.getPrice()) && !isPeggedUpdateSuppressed()){
            updatePeggedOrders(order.getSide(), order.getPrice());
        }

        PriceLevel priceLevel = findPriceLevel(order.getSide(), order.getPrice());
        order.setPriceLevel(priceLevel);
        priceLevel.addOrder(order);
        order.rest();
    }

    private void updatePeggedOrders(Side side, long price){
        setUpdatePeggedSuppression(true);
        PegType pegType = Side.pegTypeFromSide(side);
        for(PeggedOrder pegOrder = peggedOrders.get(pegType).getHeadOrder(); pegOrder!= null; pegOrder = pegOrder.getNextPeggedOrder()){
            pegOrder.updatePricePegged(price);
        }
        setUpdatePeggedSuppression(false);
    }

    public final EnumSet<RejectReason> validate(CreateOrderRequest userRequest){
        EnumSet<RejectReason> rejectReasons = EnumSet.noneOf(RejectReason.class);

        if(userRequest.getQuantity() <= 0){ rejectReasons.add(RejectReason.WRONG_QUANTITY);}
        if(userRequest.getSide() == Side.INVALID){ rejectReasons.add(RejectReason.WRONG_SIDE);}

        if(userRequest.getOderType() == OrderType.PEGGED){
            switch(userRequest.getPegType()){
                case INVALID: rejectReasons.add(RejectReason.WRONG_PEGTYPE);
                case ASK: if(!this.hasAsks()){rejectReasons.add(RejectReason.NO_ASK_TO_PEG);}
                case BID: if(!this.hasBids()){rejectReasons.add(RejectReason.NO_BID_TO_PEG);}
            }
        }
        return rejectReasons;
    }

    public void createOrder(CreateOrderRequest userRequest){

        Order order = switch (userRequest.getOderType()){
            case MARKET ->
                    new MarketOrder(userRequest.getSide(), userRequest.getClientID(), userRequest.getQuantity());
            case LIMIT ->
                    new LimitOrder(userRequest.getSide(), userRequest.getClientID(), userRequest.getQuantity(), userRequest.getPrice());
            case PEGGED->
                    new PeggedOrder(userRequest.getSide(), userRequest.getClientID(), userRequest.getQuantity(), userRequest.getPegType(), getBestPrice(userRequest.getSide()));
        };

        order.addListener(this);
        EnumSet<RejectReason> rejectReasons = validate(userRequest);
        if(!rejectReasons.isEmpty()){
            order.reject(rejectReasons);
            return;
        }

        order.accept(this.orderID++);
        order.register(this);
        this.tryFillNewOrder(order);
    }

    public void cancelOrder(CancelOrderRequest cancelRequest){
        long orderID = cancelRequest.getOrderIDToCancel();
        if(!orders.containsKey(orderID)){
            rejectOrderCancel(ORDER_NOT_EXISTS, Instant.now());
            return;
        }
        Order orderToCancel = orders.get(orderID);
        orderToCancel.cancel(USER_REQUEST);
    }

    public void replaceOrder(ReplaceOrderRequest replaceOrderRequest){
        long orderID = replaceOrderRequest.getOrderID();
        if(!orders.containsKey(orderID)) {
            rejectOrderReplace(ORDER_NOT_EXISTS, Instant.now());
            return;
        }
        Order order = orders.get(orderID);
        order.update(replaceOrderRequest.getQuantity(), replaceOrderRequest.getPrice());
    }

    public void removeOrderFromOrderBook(Order order){
        order.removeListener(this);
        if(!order.isResting()) return;

        order.unRegister(this);
        cleanupEmptyPriceLevel(order.getPriceLevel());
    }

    public void cleanupEmptyPriceLevel(PriceLevel priceLevel){
        pricingLevelMap.get(priceLevel.getSide()).remove(priceLevel.getPrice());
        if(priceLevel.isEmpty()){
            PriceLevel nextPriceLevel = priceLevel.getNextPriceLevel();
            PriceLevel prevPriceLevel = priceLevel.getPrevPriceLevel();
            Side side = priceLevel.getSide();

            if(prevPriceLevel != null) prevPriceLevel.setNextPriceLevel(nextPriceLevel);
            if(nextPriceLevel != null) nextPriceLevel.setPrevPriceLevel(prevPriceLevel);
            if(tailPriceLevel.get(side) == priceLevel) tailPriceLevel.put(side, prevPriceLevel);
            if(headPriceLevel.get(side) == priceLevel) headPriceLevel.put(side, nextPriceLevel);
        }
    }

    public void executeUserInput(String userInputRequest){
        UserRequest userRequest = userPromptParser.parseUserRequest(userInputRequest);
        switch(userRequest.getUserRequestType()){
            case CREATE_MARKET_ORDER, CREATE_LIMIT_ORDER, CREATE_PEGGED_ORDER -> createOrder((CreateOrderRequest) userRequest);
            case REPLACE_ORDER -> replaceOrder((ReplaceOrderRequest) userRequest);
            case CANCEL_ORDER -> cancelOrder((CancelOrderRequest) userRequest);
            case SHOW_ORDER_BOOK -> printOrderBook();
            case INVALID -> invalidateParsing(userInputRequest);
        }
    }

    private int setLines(PriceLevel sell, List<String> sellStrings, int sellColumnWidth) {
        for(PriceLevel priceLevel = sell; priceLevel!= null; priceLevel = priceLevel.getNextPriceLevel()){
            for(Order order = priceLevel.getHeadOrder(); order != null; order = order.getNextOrder()){
                String line = String.format( "%d @ %d | %s %d", order.getPendingQuantity(),
                        order.getPrice(), order.getOrderType().getValueName(), order.getOrderID());
                sellStrings.add(line);
                sellColumnWidth = Math.max(sellColumnWidth, line.length());
            }
        }
        return sellColumnWidth;
    }

    public void printOrderBook(){

        PriceLevel buy = this.headPriceLevel.get(Side.BUY);
        PriceLevel sell = this.headPriceLevel.get(Side.SELL);

        List<String> buyStrings = new ArrayList<>();
        List<String> sellStrings = new ArrayList<>();

        String bidColumnName = "BID";
        int buyColumnWidth = bidColumnName.length();
        String offerColumnName = "OFFER";
        int sellColumnWidth = offerColumnName.length();

        buyColumnWidth = setLines(buy, buyStrings, buyColumnWidth);
        sellColumnWidth = setLines(sell, sellStrings, sellColumnWidth);

        StringBuilder orderBook = new StringBuilder();
        int rightPadding = 2;
        String padRight = " ".repeat(rightPadding);
        int leftPadding = 2;
        String padLeft = " ".repeat(leftPadding);
        String horizontalLine = "+" + "-".repeat(buyColumnWidth + leftPadding + rightPadding) +
                                "+" + "-".repeat(sellColumnWidth + leftPadding + rightPadding) + "+\n";

        orderBook.append(horizontalLine);
        orderBook.append( "I" )
                .append(padRight)
                .append(String.format("%-" + buyColumnWidth + "s", "BID"))
                .append(padLeft)
                .append("|")
                .append(padRight)
                .append(String.format("%-" + sellColumnWidth + "s", "OFFER"))
                .append(padLeft)
                .append("|\n");
        orderBook.append(horizontalLine);

        for(int i=0; i< Math.max(buyStrings.size(), sellStrings.size()); i++){
            String buyString = i<buyStrings.size() ? buyStrings.get(i) : "";
            String sellString = i<sellStrings.size() ? sellStrings.get(i) : "";
            orderBook.append( "I" )
                     .append(padRight)
                     .append(String.format("%-" + buyColumnWidth + "s", buyString))
                     .append(padLeft)
                     .append("|")
                     .append(padRight)
                     .append(String.format("%-" + sellColumnWidth + "s", sellString))
                     .append(padLeft)
                     .append("|\n");
        }
        orderBook.append(horizontalLine);
        System.out.printf(String.valueOf(orderBook));
    }

    @Override
    public void onOrderAccepted(Order order, Instant time) {
        for (OrderBookListener listener : listeners) {
            listener.onOrderAccepted(this, order, time);
        }
    }

    @Override
    public void onOrderRejected(Order order, Instant rejectTime, EnumSet<RejectReason> rejectReasons) {
        order.removeListener(this);
        for (OrderBookListener listener : listeners) {
            listener.onOrderRejected(this, order, rejectTime, rejectReasons );
        }
    }

    @Override
    public void onOrderCancelled(Order order, Instant cancelTime, CancelReason cancelReason) {
        removeOrderFromOrderBook(order);

        for (OrderBookListener listener : listeners) {
            listener.onOrderCanceled(this, order, cancelTime, cancelReason);
        }
    }

    @Override
    public void onOrderRest(Order order, Instant restTime) {
        for (OrderBookListener listener : listeners) {
            listener.onOrderRest(this, order, restTime );
        }
    }

    @Override
    public void onOrderExecuted(Order order, Instant lastExecuteTime, ExecutionSide executionSide, int sharesToExecute, long executionPrice, long executionID, long matchId) {
        if(order.isFullyFiled())
            removeOrderFromOrderBook(order);

        for (OrderBookListener listener : listeners) {
            listener.onOrderExecuted( this, order, lastExecuteTime, executionSide, sharesToExecute, executionPrice, executionID, matchId );
        }
    }

    @Override
    public void onOrderReplaceAccepted(Order order, Instant acceptTime, int prevQuantity, long prevPrice) {
        for (OrderBookListener listener : listeners) {
            listener.onOrderReplaceAccepted(this, order, acceptTime, prevQuantity, prevPrice);
        }

        if(prevPrice != order.getPrice()) {
            cleanupEmptyPriceLevel(order.getPriceLevel());
            fillOrRest(order);
        }
    }
    public void rejectOrderCancel(RejectReason rejectReason, Instant rejectTime){
        for (OrderBookListener listener : listeners) {
            listener.onOrderCancelRejected(this, rejectTime, rejectReason);
        }
    }

    //Reject Done by OrderBook on NonExistingOrder
    public void rejectOrderReplace(RejectReason rejectReason, Instant rejectTime){
        for (OrderBookListener listener : listeners) {
            listener.onOrderReplaceRejected(this, rejectTime, rejectReason);
        }
    }

    //Reject Done By Order
    public void onOrderReplaceRejected(Order order, Instant rejectTime, RejectReason rejectReason) {
        for (OrderBookListener listener : listeners) {
            listener.onOrderReplaceRejected(this, rejectTime, rejectReason);
        }
    }

    private void invalidateParsing(String userInput){
        for (OrderBookListener listener : listeners) {
            listener.onUserPromptRejected(this, RejectReason.BAD_INPUT_FORMATTING, userInput);
        }
    }

    @Override
    public void registerPegged(PeggedOrder order) {
        PegType pegType = Side.pegTypeFromSide(order.getSide());
        peggedOrders.get(pegType).addOrder(order);
    }

    @Override
    public void registerOrders( Order order) {
        orders.put(order.getOrderID(), order);
    }

    @Override
    public void unRegisterPegged(PeggedOrder order) {
        PegType pegType = Side.pegTypeFromSide(order.getSide());
        peggedOrders.get(pegType).removeOrder(order);
    }

    @Override
    public void unRegisterOrders(Order order) {
        orders.remove(order.getOrderID());
    }
}
