package OrderRequest;


import common.UserRequestType;

import static common.UserRequestType.INVALID;

public final class InvalidUserRequest extends UserRequest {

    public InvalidUserRequest() {
        super(INVALID);
    }
}