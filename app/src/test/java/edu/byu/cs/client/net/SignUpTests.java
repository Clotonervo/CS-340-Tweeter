package edu.byu.cs.client.net;

import android.view.View;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.byu.cs.client.model.domain.User;
import edu.byu.cs.client.model.services.LoginService;
import edu.byu.cs.client.model.services.SignUpService;
import edu.byu.cs.client.net.request.SignUpRequest;
import edu.byu.cs.client.net.response.SignUpResponse;
import edu.byu.cs.client.presenter.SignUpPresenter;

public class SignUpTests {

    private SignUpService service = SignUpService.getInstance();
    private LoginService loginService = LoginService.getInstance();
    private SignUpRequest request;
    private SignUpResponse response;

    public class ViewImplementation implements SignUpPresenter.View {
        @Override
        public void register(View v)
        {

        }
    }

    private SignUpPresenter presenter = new SignUpPresenter(new ViewImplementation());


    @AfterEach
    void cleanUp(){
        request = null;
        response = null;

        loginService.setCurrentUser(null);
        loginService.setLoggedInUser(null);
    }

    @Test
    void testSignUpNewUser(){
        request = new SignUpRequest("Username", "password", "Test", "SignUP", null);
        response = presenter.signUpUser(request);
        User signedUpUser = presenter.getUserByAlias("@Username");

        Assertions.assertNotNull(signedUpUser);
        Assertions.assertTrue(response.isSuccess());
    }

    @Test
    void testSignUpAndLogIn(){
        request = new SignUpRequest("Username2", "password", "Test", "SignUP", null);
        response = presenter.signUpUser(request);

        Assertions.assertTrue(response.isSuccess());

        Assertions.assertNotNull(presenter.getLoggedInUser());
        Assertions.assertNotNull(presenter.getCurrentUser());
        Assertions.assertEquals(presenter.getCurrentUser().getAlias(), "@Username2");
    }


    @Test
    void testSignUpWithErrors(){
        request = new SignUpRequest("Username3", null, "Test", "SignUP", null);
        response = presenter.signUpUser(request);
        Assertions.assertFalse(response.isSuccess());

        User signedUpUser = presenter.getUserByAlias("@Username3");
        Assertions.assertNull(signedUpUser);
    }

    @Test
    void signUpAlreadyExistingUser(){
        request = new SignUpRequest("Username", "password", "Test", "SignUP", null);
        response = presenter.signUpUser(request);

        Assertions.assertFalse(response.isSuccess());
        User signedUpUser = presenter.getUserByAlias("@Username");
        Assertions.assertNotNull(signedUpUser);
        Assertions.assertEquals(signedUpUser.getAlias(), "@Username");
    }
}
