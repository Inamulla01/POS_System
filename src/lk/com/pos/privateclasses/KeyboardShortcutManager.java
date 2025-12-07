package lk.com.pos.privateclasses;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized keyboard shortcut manager to prevent conflicts
 * and provide consistent shortcuts across the POS system
 */
public class KeyboardShortcutManager {
    
    private static KeyboardShortcutManager instance;
    private Map<String, ShortcutInfo> shortcuts = new HashMap<>();
    
    public static KeyboardShortcutManager getInstance() {
        if (instance == null) {
            instance = new KeyboardShortcutManager();
        }
        return instance;
    }
    
    private KeyboardShortcutManager() {
        initializeShortcuts();
    }
    
    private void initializeShortcuts() {
        // GLOBAL SHORTCUTS (work anywhere)
        addShortcut("SEARCH_PRODUCTS", "F2", "Search products");
        addShortcut("FOCUS_CART", "F3", "Focus cart");
        addShortcut("COMPLETE_SALE", "F9", "Complete sale");
        addShortcut("CLEAR_CART", "F8", "Clear cart");
        
        // PAYMENT SHORTCUTS
        addShortcut("CASH_PAYMENT", "F4", "Cash payment");
        addShortcut("CARD_PAYMENT", "F5", "Card payment");
        addShortcut("CREDIT_PAYMENT", "F6", "Credit payment");
        addShortcut("CHEQUE_PAYMENT", "F7", "Cheque payment");
        
        // TRANSACTION SHORTCUTS
        addShortcut("DISCOUNT", "F10", "Apply discount");
        addShortcut("HOLD_BILL", "F11", "Hold bill");
        addShortcut("SWITCH_INVOICE", "F12", "Switch invoice");
        addShortcut("EXCHANGE", "Ctrl+E", "Exchange/Return");
        
        // NAVIGATION SHORTCUTS (context-aware)
        addShortcut("ADD_PRODUCT", "Enter or Space", "Add selected product");
        addShortcut("INCREASE_QTY", "+", "Increase quantity");
        addShortcut("DECREASE_QTY", "-", "Decrease quantity");
        addShortcut("DELETE_ITEM", "Delete", "Delete selected item");
        addShortcut("NAVIGATE_UP", "↑", "Navigate up");
        addShortcut("NAVIGATE_DOWN", "↓", "Navigate down");
        addShortcut("NAVIGATE_LEFT", "←", "Navigate left");
        addShortcut("NAVIGATE_RIGHT", "→", "Navigate right");
        
        // QUICK ACCESS
        addShortcut("FIRST_ITEM", "Home", "Jump to first item");
        addShortcut("LAST_ITEM", "End", "Jump to last item");
        addShortcut("PAGE_UP", "Page Up", "Jump up 5 items");
        addShortcut("PAGE_DOWN", "Page Down", "Jump down 5 items");
        
        // ESCAPE
        addShortcut("CANCEL", "Esc", "Cancel/Clear search");
        
        // SRI LANKAN SPECIFIC SHORTCUTS
        addShortcut("CUSTOMER_LOYALTY", "Ctrl+L", "Customer loyalty points");
        addShortcut("QUICK_PRICE", "Ctrl+P", "Quick price override");
        addShortcut("STOCK_CHECK", "Ctrl+S", "Check stock levels");
        addShortcut("VOID_ITEM", "Ctrl+V", "Void selected item");
        addShortcut("REPRINT_BILL", "Ctrl+R", "Reprint last bill");
        addShortcut("TOGGLE_VAT", "Ctrl+T", "Toggle VAT inclusive/exclusive");
    }
    
    private void addShortcut(String id, String keys, String description) {
        shortcuts.put(id, new ShortcutInfo(id, keys, description));
    }
    
    public String getShortcutHelp() {
        StringBuilder help = new StringBuilder();
        help.append("<html><body style='font-family: Nunito, sans-serif; padding: 10px;'>");
        help.append("<h2 style='color: #1CB5BB; margin-bottom: 15px;'>⌨️ Keyboard Shortcuts</h2>");
        
        help.append("<h3 style='color: #333; margin-top: 15px;'>🔍 Search & Navigation</h3>");
        help.append("<table cellpadding='5' style='width: 100%;'>");
        help.append("<tr><td><b>F2</b></td><td>Search products</td></tr>");
        help.append("<tr><td><b>F3</b></td><td>Focus cart panel</td></tr>");
        help.append("<tr><td><b>Arrow Keys</b></td><td>Navigate products/cart items</td></tr>");
        help.append("<tr><td><b>Enter/Space</b></td><td>Add selected product to cart</td></tr>");
        help.append("<tr><td><b>Esc</b></td><td>Clear search / Return to products</td></tr>");
        help.append("<tr><td><b>Home/End</b></td><td>Jump to first/last product</td></tr>");
        help.append("<tr><td><b>Page Up/Down</b></td><td>Jump 5 products</td></tr>");
        help.append("</table>");
        
        help.append("<h3 style='color: #333; margin-top: 15px;'>💰 Payment Methods</h3>");
        help.append("<table cellpadding='5' style='width: 100%;'>");
        help.append("<tr><td><b>F4</b></td><td>Cash Payment</td></tr>");
        help.append("<tr><td><b>F5</b></td><td>Card Payment</td></tr>");
        help.append("<tr><td><b>F6</b></td><td>Credit Payment</td></tr>");
        help.append("<tr><td><b>F7</b></td><td>Cheque Payment</td></tr>");
        help.append("<tr><td><b>F9</b></td><td>Complete Sale</td></tr>");
        help.append("</table>");
        
        help.append("<h3 style='color: #333; margin-top: 15px;'>🛒 Cart Actions</h3>");
        help.append("<table cellpadding='5' style='width: 100%;'>");
        help.append("<tr><td><b>+</b></td><td>Increase quantity</td></tr>");
        help.append("<tr><td><b>-</b></td><td>Decrease quantity</td></tr>");
        help.append("<tr><td><b>Delete</b></td><td>Remove selected item</td></tr>");
        help.append("<tr><td><b>F8</b></td><td>Clear entire cart</td></tr>");
        help.append("</table>");
        
        help.append("<h3 style='color: #333; margin-top: 15px;'>📋 Transactions</h3>");
        help.append("<table cellpadding='5' style='width: 100%;'>");
        help.append("<tr><td><b>F10</b></td><td>Apply discount</td></tr>");
        help.append("<tr><td><b>F11</b></td><td>Hold bill</td></tr>");
        help.append("<tr><td><b>F12</b></td><td>Switch invoice</td></tr>");
        help.append("<tr><td><b>Ctrl+E</b></td><td>Exchange/Return</td></tr>");
        help.append("</table>");
        
        help.append("<h3 style='color: #333; margin-top: 15px;'>🇱🇰 Local Features</h3>");
        help.append("<table cellpadding='5' style='width: 100%;'>");
        help.append("<tr><td><b>Ctrl+V</b></td><td>Void item</td></tr>");
        help.append("<tr><td><b>Ctrl+R</b></td><td>Reprint bill</td></tr>");
        help.append("<tr><td><b>Ctrl+T</b></td><td>Toggle VAT display</td></tr>");
        help.append("<tr><td><b>Ctrl+L</b></td><td>Loyalty customer lookup</td></tr>");
        help.append("<tr><td><b>Ctrl+P</b></td><td>Quick price override</td></tr>");
        help.append("<tr><td><b>Ctrl+S</b></td><td>Check stock levels</td></tr>");
        help.append("</table>");
        
        help.append("<div style='margin-top: 20px; padding: 10px; background: #E8F4F5; border-radius: 5px;'>");
        help.append("<b>💡 Tip:</b> Press <b>F1</b> anytime to see this help");
        help.append("</div>");
        
        help.append("</body></html>");
        return help.toString();
    }
    
    public void showHelp(JComponent parent) {
        JOptionPane.showMessageDialog(
            SwingUtilities.getWindowAncestor(parent),
            getShortcutHelp(),
            "Keyboard Shortcuts",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private static class ShortcutInfo {
        String id;
        String keys;
        String description;
        
        ShortcutInfo(String id, String keys, String description) {
            this.id = id;
            this.keys = keys;
            this.description = description;
        }
    }
}