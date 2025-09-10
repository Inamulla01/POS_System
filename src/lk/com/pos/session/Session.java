/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.com.pos.session;

public class Session {
    private static Session instance;
    private String email;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setEmail(String email) {
        this.email = email;

    }

    public String getEmail() {
        return email;
    }

    public void clear() {
        email = null;
    }
}
