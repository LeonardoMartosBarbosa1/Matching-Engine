package OrderRequest;

import common.UserRequestType;

public class ShowOrderBookRequest extends UserRequest {
    public ShowOrderBookRequest() {
        super(UserRequestType.SHOW_ORDER_BOOK);
    }
}
