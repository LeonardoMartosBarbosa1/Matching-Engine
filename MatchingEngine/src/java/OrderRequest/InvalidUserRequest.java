package OrderRequest;


import common.UserRequestType;

public final class InvalidUserRequest extends UserRequest {

    public InvalidUserRequest() {
        super(UserRequestType.INVALID);
    }
}