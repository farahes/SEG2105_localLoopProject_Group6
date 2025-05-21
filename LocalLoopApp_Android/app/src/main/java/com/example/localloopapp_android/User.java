public abstract class User {
    protected String userID;
    protected String username;
    protected String email;
    protected Role role;

    public User() {}

    public User(String userID, String username, String email, Role role) {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public String getUserID() {
        return this.userID;
    }

    public String getUsername() {
        return this.username;
    }

    public String getEmail() {
        return this.email;
    }

    public Role getRole() {
        return this.role;
    }
}