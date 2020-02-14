package edu.byu.cs.tweeter.model.services;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.net.ServerFacade;
import edu.byu.cs.tweeter.net.request.SignUpRequest;
import edu.byu.cs.tweeter.net.response.SignUpResponse;

public class SignUpService {

    private static SignUpService instance;
    private final ServerFacade serverFacade;


    public static SignUpService getInstance() {
        if(instance == null) {
            instance = new SignUpService();
        }

        return instance;
    }

    private SignUpService() {serverFacade = ServerFacade.getInstance();}

    public SignUpResponse authenticateUser(SignUpRequest signUpRequest){
        SignUpResponse signUpResponse = serverFacade.registerNewUser(signUpRequest);
        return signUpResponse;
    }
}
