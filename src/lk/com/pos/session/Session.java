package lk.com.pos.session;

public class Session {
    private static Session instance;
    private int userId;
    private String roleName;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setSession(int userId, String roleName) {
        this.userId = userId;
        this.roleName = roleName;
    }

    public int getUserId() {
        return userId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void clear() {
        userId = 0;
        roleName = null;
    }

    public int getRoleId() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
