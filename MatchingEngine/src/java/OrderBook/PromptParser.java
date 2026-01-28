package OrderBook;

import OrderRequest.*;
import common.PegType;
import common.Side;
import common.UserRequestType;

import java.util.Objects;

import static common.UserRequestType.*;

/* Parsing Rules for User Input:
    CREATE_MARKET_ORDER:
            market [side] [quantity]
            example: market buy 150

    CREATE_LIMIT_ORDER:
            limit [side] [price] [quantity]
            example: limit buy 10 100

    CREATE_PEGGED_ORDER:
            pegged [pegtype] [quantity]
            example: pegged bid 150

    CANCEL_ORDER:
            cancel order [orderID]
            example: cancel 2

    SHOW_ORDER_BOOK: show

    REPLACE_ORDER: replace [orderID] [quantity] [price]
 */

public class PromptParser {
    public static UserRequestType userRequestTypeFromCommand(String command){
        return switch (command) {
            case "cancel" -> CANCEL_ORDER;
            case "replace" -> REPLACE_ORDER;
            case "market" -> CREATE_MARKET_ORDER;
            case "limit" -> CREATE_LIMIT_ORDER;
            case "pegged" -> CREATE_PEGGED_ORDER;
            case "show" -> SHOW_ORDER_BOOK;
            default -> INVALID;
        };
    }

    public UserRequest parseUserRequest(String userInput){
        String[] tokens = userInput.split("\\s+");
        String command = tokens[0];

        UserRequestType userRequestType = PromptParser.userRequestTypeFromCommand(command);
        return switch (userRequestType) {
            case CREATE_MARKET_ORDER -> parseMarket(tokens);
            case CREATE_LIMIT_ORDER -> parseLimit(tokens);
            case CREATE_PEGGED_ORDER -> parsePegged(tokens);
            case CANCEL_ORDER -> parseCancel(tokens);
            case REPLACE_ORDER -> parseReplace(tokens);
            case SHOW_ORDER_BOOK -> parseShowOrderBook(tokens);
            case INVALID -> new InvalidUserRequest();
        };
    }
    private UserRequest parseMarket(String[] tokens){
        if(tokens.length != 4)
            return new InvalidUserRequest();

        return(new CreateOrderRequest(CREATE_MARKET_ORDER,
            Side.fromString(tokens[1]),
            Integer.parseInt(tokens[2]),
            null,
            null,
            tokens[3]));
    }

    private UserRequest parseLimit(String[] tokens){
        if(tokens.length != 5)
            return new InvalidUserRequest();

        return(new CreateOrderRequest(CREATE_LIMIT_ORDER,
                Side.fromString(tokens[1]),
                Integer.parseInt(tokens[2]),
                Long.parseLong(tokens[3]),
                null,
                tokens[4]));
    }

    private UserRequest parsePegged(String[] tokens){
        if(tokens.length != 4)
            return new InvalidUserRequest();

        PegType pegType = PegType.fromString(tokens[1]);
        Side side = PegType.sideFromPegType(pegType);
        return(new CreateOrderRequest(CREATE_PEGGED_ORDER,
                side,
                Integer.parseInt(tokens[2]),
                null,
                pegType,
                tokens[3]));
    }

    private UserRequest parseCancel(String[] tokens){
        if(tokens.length != 4)return new InvalidUserRequest();
        return new CancelOrderRequest(Integer.parseInt(tokens[2]));
    }

    private UserRequest parseShowOrderBook(String[] tokens){
        if(tokens.length != 2)return new InvalidUserRequest();
        return new ShowOrderBookRequest();
    }

    private UserRequest parseReplace(String[] tokens){
        if(tokens.length != 5)return new InvalidUserRequest();
        Long orderID = Long.parseLong(tokens[1]);
        Integer quantity = Objects.equals(tokens[2], "-") ? null : Integer.parseInt(tokens[2]);
        Long price = Objects.equals(tokens[3], "-") ? null : Long.parseLong(tokens[3]);
        return new ReplaceOrderRequest(orderID, quantity, price);
    }
}
