package edu.byu.cs.client.view.asyncTasks;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import edu.byu.cs.client.model.domain.User;
import edu.byu.cs.client.net.request.FollowingRequest;
import edu.byu.cs.client.net.response.FollowingResponse;
import edu.byu.cs.client.presenter.FollowingPresenter;
import edu.byu.cs.client.view.cache.ImageCache;
import edu.byu.cs.client.view.util.ImageUtils;

public class GetFollowingTask extends AsyncTask<FollowingRequest, Void, FollowingResponse> {

    private final FollowingPresenter presenter;
    private final GetFolloweesObserver observer;

    public interface GetFolloweesObserver {
        void followeesRetrieved(FollowingResponse followingResponse);
    }

    public GetFollowingTask(FollowingPresenter presenter, GetFolloweesObserver observer) {
        this.presenter = presenter;
        this.observer = observer;
    }

    @Override
    protected FollowingResponse doInBackground(FollowingRequest... followingRequests) {
        FollowingResponse response = presenter.getFollowing(followingRequests[0]);
        if(response.isSuccess()) {
            loadImages(response);
        }
        return response;
    }

    private void loadImages(FollowingResponse response) {
        for(User user : response.getFollowees()) {

            Drawable drawable;

            try {
                drawable = ImageUtils.drawableFromUrl(user.getImageUrl());
            } catch (IOException e) {
                Log.e(this.getClass().getName(), e.toString(), e);
                drawable = null;
            }

            ImageCache.getInstance().cacheImage(user, drawable);
        }
    }

    @Override
    protected void onPostExecute(FollowingResponse followingResponse) {

        if(observer != null) {
            observer.followeesRetrieved(followingResponse);
        }
    }
}
