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
}