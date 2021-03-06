package edu.byu.cs.client.net;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import edu.byu.cs.client.model.domain.User;
import edu.byu.cs.client.model.services.LoginService;
import edu.byu.cs.client.net.request.FollowerRequest;
import edu.byu.cs.client.net.request.LoginRequest;
import edu.byu.cs.client.net.response.FollowerResponse;
import edu.byu.cs.client.net.response.LoginResponse;
import edu.byu.cs.client.presenter.FollowerPresenter;

public class ViewUserFollowersTest {

    private LoginService loginService = LoginService.getInstance();

    public class ViewImplementation implements FollowerPresenter.View {

    }

    private FollowerPresenter presenter = new FollowerPresenter(new ViewImplementation());

    @Test
    void viewOtherUserFollowers(){
        LoginRequest loginRequest = new LoginRequest("@TestUser", "password");
        LoginResponse loginResponse = loginService.authenticateUser(loginRequest);

        Assertions.assertTrue(loginResponse.isSuccess());
        Assertions.assertEquals(presenter.getCurrentUser().getAlias(), loginRequest.getUsername());

        FollowerResponse response = presenter.getFollowers(new FollowerRequest(presenter.getLoggedInUser().getAlias(), 1000, null));
        Assertions.assertTrue(response.isSuccess());

        List<User> followers = response.getFollowers();
        loginService.setCurrentUser(followers.get(0));


        response = presenter.getFollowers(new FollowerRequest(presenter.getCurrentUser().getAlias(), 1000, null));
        Assertions.assertTrue(response.isSuccess());

        List<User> followersOtherUser = response.getFollowers();

        Assertions.assertNotEquals(followers, followersOtherUser);
        Assertions.assertNotEquals(followersOtherUser.size(), 0);


        for (User user: followersOtherUser) {
            Assertions.assertNotEquals(user.getAlias(), loginService.getCurrentUser());
        }
    }
}
