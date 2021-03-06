package edu.byu.cs.client.net.response;

import java.util.List;

import edu.byu.cs.client.model.domain.User;

public class FollowingResponse extends PagedResponse {

    private List<User> followees;

    public FollowingResponse(String message) {
        super(false, message, false);
    }

    public FollowingResponse(List<User> followees, boolean hasMorePages) {
        super(true, hasMorePages);
        this.followees = followees;
    }

    public List<User> getFollowees() {
        return followees;
    }

    public boolean isSuccess(){
        return super.isSuccess();
    }
}
