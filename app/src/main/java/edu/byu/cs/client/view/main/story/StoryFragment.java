package edu.byu.cs.client.view.main.story;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import edu.byu.cs.client.R;
import edu.byu.cs.client.model.domain.Status;
import edu.byu.cs.client.model.domain.User;
import edu.byu.cs.client.net.request.StoryRequest;
import edu.byu.cs.client.net.response.StoryResponse;
import edu.byu.cs.client.presenter.StoryPresenter;
import edu.byu.cs.client.view.asyncTasks.GetStoryTask;
import edu.byu.cs.client.view.asyncTasks.UserAliasTask;
import edu.byu.cs.client.view.cache.ImageCache;
import edu.byu.cs.client.view.main.MainActivity;

public class StoryFragment extends Fragment implements StoryPresenter.View {

    private static final int LOADING_DATA_VIEW = 0;
    private static final int ITEM_VIEW = 1;

    private static final int PAGE_SIZE = 10;

    private StoryPresenter presenter;
    private StoryRecyclerViewAdapter storyRecyclerViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story, container, false);

        presenter = new StoryPresenter(this);

        RecyclerView storyRecyclerView = view.findViewById(R.id.storyRecylerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        storyRecyclerView.setLayoutManager(layoutManager);

        storyRecyclerViewAdapter = new StoryRecyclerViewAdapter();
        storyRecyclerView.setAdapter(storyRecyclerViewAdapter);
        storyRecyclerView.addOnScrollListener(new StatusRecyclerViewPaginationScrollListener(layoutManager));

        return view;
    }


    private class StatusHolder extends RecyclerView.ViewHolder implements UserAliasTask.UserAliasObserver {

        private final ImageView userImage;
        private final TextView userAlias;
        private final TextView userName;
        private final TextView message;

        StatusHolder(@NonNull final View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.userImage);
            userAlias = itemView.findViewById(R.id.userAlias);
            userName = itemView.findViewById(R.id.userName);
            message = itemView.findViewById(R.id.message);
        }

        void bindUser(Status status) {
            userImage.setImageDrawable(ImageCache.getInstance().getImageDrawable(status.getUser()));
            userAlias.setText(status.getUser().getAlias());
            userName.setText(status.getUser().getName());
            message.setText(status.getMessage());

            String messageCopy = message.getText().toString();
            SpannableString ss = new SpannableString(messageCopy);

            final StatusHolder instance = this;

            //--------------- User mentions
            ClickableSpan userMentionsSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    TextView tx = (TextView) textView;
                    String s = tx.getText().toString();
                    UserAliasTask userAliasTask = new UserAliasTask(instance, presenter);
                    userAliasTask.execute(tx.getText().toString());
                }
            };

            //-------------- Links
            ClickableSpan linksSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    TextView tx = (TextView) textView;
                    String url = tx.getText().toString();

                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "http://" + url;
                    }
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                }
            };


            List<String> userMentions = status.getUserMentions();
            List<String> links = status.getLinks();

            for(int i = 0; i < userMentions.size(); i++){
                int beginningIndex = messageCopy.indexOf(userMentions.get(i));
                int endingIndex = userMentions.get(i).length() + beginningIndex;

                ss.setSpan(userMentionsSpan, beginningIndex, endingIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            for(int i = 0; i < links.size(); i++){
                int beginningIndex = messageCopy.indexOf(links.get(i));
                int endingIndex = links.get(i).length() + beginningIndex;

                ss.setSpan(linksSpan, beginningIndex, endingIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            message.setText(ss);
            message.setMovementMethod(LinkMovementMethod.getInstance());
            message.setHighlightColor(Color.BLUE);
        }

        @Override
        public void userSuccess(User user)          //TODO: Fix this
        {
            if(user != null){
                presenter.setCurrentUser(user);

                Intent intent = new Intent(itemView.getContext(), MainActivity.class);
                itemView.getContext().startActivity(intent);
            }
            else {
                Toast.makeText(getContext(), "That user does not exist!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void userError(String error)
        {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        }
    }

    private class StoryRecyclerViewAdapter extends RecyclerView.Adapter<StatusHolder> implements GetStoryTask.GetStoryObserver {

        private final List<Status> statuses = new ArrayList<>();

        private Status lastStatus;
        private boolean hasMorePages;
        private boolean isLoading = false;

        StoryRecyclerViewAdapter() {
            loadMoreItems();
        }

        void addItems(List<Status> newStatuses) {
            int startInsertPosition = statuses.size();
            statuses.addAll(newStatuses);
            this.notifyItemRangeInserted(startInsertPosition, newStatuses.size());
        }

        void addItem(Status status) {
            statuses.add(status);
            this.notifyItemInserted(statuses.size() - 1);
        }

        void removeItem(Status status) {
            int position = statuses.indexOf(status);
            statuses.remove(position);
            this.notifyItemRemoved(position);
        }

        @NonNull
        @Override
        public StatusHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(StoryFragment.this.getContext());
            View view;

            if(isLoading) {
                view =layoutInflater.inflate(R.layout.loading_row, parent, false);

            } else {
                view = layoutInflater.inflate(R.layout.story_row, parent, false);
            }

            return new StatusHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StatusHolder statusHolder, int position) {
            if(!isLoading) {
                statusHolder.bindUser(statuses.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return statuses.size();
        }

        @Override
        public int getItemViewType(int position) {
            return (position == statuses.size() - 1 && isLoading) ? LOADING_DATA_VIEW : ITEM_VIEW;
        }


        void loadMoreItems() {
            isLoading = true;
            addLoadingFooter();

            GetStoryTask getStoryTask = new GetStoryTask(presenter, this);
            StoryRequest request = new StoryRequest(presenter.getCurrentUser(), PAGE_SIZE, lastStatus);
            getStoryTask.execute(request);
        }

        @Override
        public void storyRetrieved(StoryResponse storyResponse) {
            List<Status> statusList = storyResponse.getStatusList();

            if(!storyResponse.isSuccess()){
                Toast.makeText(getContext(), storyResponse.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            lastStatus = (statusList.size() > 0) ? statusList.get(statusList.size() -1) : null;
            hasMorePages = storyResponse.hasMorePages();

            isLoading = false;
            removeLoadingFooter();
            storyRecyclerViewAdapter.addItems(statusList);
        }

        private void addLoadingFooter() {
            addItem(new Status(new User("Dummy", "User", ""), "This is a message"));
        }

        private void removeLoadingFooter() {
            removeItem(statuses.get(statuses.size() - 1));
        }
    }


    private class StatusRecyclerViewPaginationScrollListener extends RecyclerView.OnScrollListener {

        private final LinearLayoutManager layoutManager;

        StatusRecyclerViewPaginationScrollListener(LinearLayoutManager layoutManager) {
            this.layoutManager = layoutManager;
        }

        @Override
        public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

            if (!storyRecyclerViewAdapter.isLoading && storyRecyclerViewAdapter.hasMorePages) {
                if ((visibleItemCount + firstVisibleItemPosition) >=
                        totalItemCount && firstVisibleItemPosition >= 0) {
                    storyRecyclerViewAdapter.loadMoreItems();
                }
            }
        }
    }
}
