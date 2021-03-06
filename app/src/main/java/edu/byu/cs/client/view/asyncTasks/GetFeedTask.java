package edu.byu.cs.client.view.asyncTasks;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import edu.byu.cs.client.model.domain.User;
import edu.byu.cs.client.model.domain.Status;
import edu.byu.cs.client.net.request.FeedRequest;
import edu.byu.cs.client.net.response.FeedResponse;
import edu.byu.cs.client.presenter.FeedPresenter;
import edu.byu.cs.client.view.cache.ImageCache;
import edu.byu.cs.client.view.util.ImageUtils;

public class GetFeedTask extends AsyncTask<FeedRequest, Void, FeedResponse> {

    private final FeedPresenter presenter;
    private final GetFeedObserver observer;

    public interface GetFeedObserver {
        void feedRetrieved(FeedResponse feedResponse);
    }

    public GetFeedTask(FeedPresenter presenter, GetFeedObserver observer) {
        this.presenter = presenter;
        this.observer = observer;
    }

    @Override
    protected FeedResponse doInBackground(FeedRequest... feedRequests) {
        FeedResponse response = presenter.getFeed(feedRequests[0]);
        if (response.isSuccess()) {
            loadImages(response);
        }
        return response;
    }

    private void loadImages(FeedResponse response) {
        for(edu.byu.cs.client.model.domain.Status status : response.getStatuses()) {
            User user = status.getUser();

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
    protected void onPostExecute(FeedResponse feedResponse) {

        if(observer != null) {
            observer.feedRetrieved(feedResponse);
        }
    }
}
