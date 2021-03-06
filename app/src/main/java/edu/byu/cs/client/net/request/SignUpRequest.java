package edu.byu.cs.client.net.request;

public class SignUpRequest {

    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String image;

    public SignUpRequest(String username, String password, String firstName, String lastName, String image){
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.image = image;
        if (image == null){
            this.image = "https://faculty.cs.byu.edu/~jwilkerson/cs340/tweeter/images/donald_duck.png";
        }
        if (username.contains("@")){
            this.username = username;
        }
        else {
            this.username = String.format("@%s", username);
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String fullName) {
        this.lastName = fullName;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }
}
