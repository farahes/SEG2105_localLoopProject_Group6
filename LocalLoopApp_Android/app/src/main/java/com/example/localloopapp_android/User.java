public abstract class User {
    private String userID;
    private String username;
    private String email;
    private Role role;

    public User() {}

    public User(String userID, String username, String email, Role role) {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}