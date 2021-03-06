package edu.byu.cs.client.presenter;

import edu.byu.cs.client.model.services.FollowingService;
import edu.byu.cs.client.net.request.FollowingRequest;
import edu.byu.cs.client.net.response.FollowingResponse;

public class FollowingPresenter extends Presenter {

    private final View view;

    /**
     * The interface by which this presenter communicates with it's view.
     */
    public interface View {
        // If needed, Specify methods here that will be called on the view in response to model updates
    }

    public FollowingPresenter(View view) {
        this.view = view;
    }

    public FollowingResponse getFollowing(FollowingRequest request) {
        return FollowingService.getInstance().getFollowees(request);
    }
}
