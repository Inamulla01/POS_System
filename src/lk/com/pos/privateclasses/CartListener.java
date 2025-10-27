package lk.com.pos.privateclasses;

public interface CartListener {
    void onCartUpdated(double total, int itemCount);
    void onCheckoutComplete();
}