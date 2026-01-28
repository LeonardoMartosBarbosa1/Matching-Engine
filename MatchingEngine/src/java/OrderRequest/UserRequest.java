package OrderRequest;

import common.UserRequestType;

public class UserRequest {
    protected final UserRequestType userRequestType;

    protected UserRequest(UserRequestType userRequestType) {
        this.userRequestType = userRequestType;
    }

    public final UserRequestType getUserRequestType(){return this.userRequestType;}
}
