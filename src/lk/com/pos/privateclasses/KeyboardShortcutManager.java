package lk.com.pos.privateclasses;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Enhanced keyboard shortcut manager with visual feedback
 * Version 4.0 - Fixed payment conflicts and discount shortcuts
 */
public class KeyboardShortcutManager {
    
    private static KeyboardShortcutManager instance;
    private Map<String, ShortcutInfo> shortcuts = new LinkedHashMap<>();
    private JLabel statusIndicator;
    
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
        // =====================================================
        // ESSENTIAL SHORTCUTS (Most Important)
        // =====================================================
        
        addShortcut("HELP", "F1", "Show this help guide anytime", "Essential", "🔍");
        addShortcut("SEARCH_FOCUS", "F2", "Focus product search bar", "Essential", "🔍");
        addShortcut("CART_FOCUS", "F3", "Jump to cart (if has items)", "Essential", "🛒");
        
        // =====================================================
        // PAYMENT METHODS (F Keys Only - No Arrow Conflicts!)
        // =====================================================
        
        addShortcut("CASH", "F4", "Select Cash Payment", "Payment", "💵");
        addShortcut("CARD", "F5", "Select Card Payment", "Payment", "💳");
        addShortcut("CREDIT", "F6", "Select Credit Payment", "Payment", "📝");
        addShortcut("CHEQUE", "F7", "Select Cheque Payment", "Payment", "🏦");
        
        // =====================================================
        // TRANSACTION ACTIONS
        // =====================================================
        
        addShortcut("CLEAR", "F8", "Clear entire cart", "Transaction", "🗑️");
        addShortcut("COMPLETE", "F9 / Alt+Enter", "Complete sale", "Transaction", "✅");
        addShortcut("GLOBAL_DISCOUNT", "F10 / Alt+D", "Apply discount to entire cart", "Transaction", "💰");
        addShortcut("HOLD", "F11 / Alt+H", "Hold bill for later", "Transaction", "⏸️");
        addShortcut("SWITCH", "F12 / Alt+S", "Switch to held invoice", "Transaction", "🔄");
        addShortcut("EXCHANGE", "Ctrl+E / Alt+E", "Process exchange/return", "Transaction", "🔁");
        
        // =====================================================
        // PRODUCT NAVIGATION (Arrow Keys - No Modifiers!)
        // =====================================================
        
        addShortcut("NAV_PRODUCTS", "Arrow Keys ↑↓←→", "Browse products (plain arrows only!)", "Navigation", "🧭");
        addShortcut("ADD_PRODUCT", "Enter / Space", "Add selected product to cart", "Navigation", "➕");
        addShortcut("CLEAR_SEARCH", "Esc", "Clear search and return to all products", "Navigation", "❎");
        addShortcut("HOME_END", "Home / End", "Jump to first/last product", "Navigation", "⏩");
        addShortcut("PAGE_JUMP", "PgUp / PgDn", "Jump 5 products up/down", "Navigation", "📄");
        
        // =====================================================
        // CART ITEM NAVIGATION (Alt + Arrows)
        // =====================================================
        
        addShortcut("CART_NAV", "Alt + ↑ / ↓", "Navigate cart items (works anywhere!)", "Cart Nav", "🛒");
        addShortcut("CART_SEARCH", "Ctrl + F", "Search items in cart", "Cart Nav", "🔎");
        
        // =====================================================
        // CART ITEM EDITING (Must Select Item First!)
        // =====================================================
        
        addShortcut("EDIT_QTY", "Alt + Q", "Edit quantity of selected cart item", "Cart Edit", "🔢");
        addShortcut("ITEM_DISCOUNT", "Alt + R / Ctrl + D", "Edit discount of selected item", "Cart Edit", "💲");
        addShortcut("ZERO_DISCOUNT", "Shift + D", "Quick remove discount from item", "Cart Edit", "🚫");
        addShortcut("INCREASE_QTY", "+ (Plus)", "Increase quantity (+1) of focused item", "Cart Edit", "⬆️");
        addShortcut("DECREASE_QTY", "- (Minus)", "Decrease quantity (-1) of focused item", "Cart Edit", "⬇️");
        addShortcut("DELETE_ITEM", "Alt + X / Delete", "Remove selected item from cart", "Cart Edit", "❌");
        
        // =====================================================
        // QUICK ACTIONS
        // =====================================================
        
        addShortcut("CREDIT_PAY", "Alt + P", "Open credit payment dialog", "Quick", "💳");
        addShortcut("PRODUCT_COUNT", "Ctrl + Q", "Show product/cart statistics", "Quick", "📊");
    }
    
    private void addShortcut(String id, String keys, String description, String category, String icon) {
        shortcuts.put(id, new ShortcutInfo(id, keys, description, category, icon));
    }
    
    /**
     * Get comprehensive help with better visual hierarchy
     */
    public String getShortcutHelp() {
        StringBuilder help = new StringBuilder();
        help.append("<html><body style='font-family: Segoe UI, Nunito, sans-serif; padding: 20px; background: #f5f7fa;'>");
        
        // Header
        help.append("<div style='text-align: center; margin-bottom: 25px; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 10px; color: white;'>");
        help.append("<h1 style='margin: 0; font-size: 28px;'>⌨️ Keyboard Shortcuts Guide</h1>");
        help.append("<p style='margin: 10px 0 0 0; font-size: 14px; opacity: 0.9;'>Lightning-fast cashier operations made easy</p>");
        help.append("</div>");
        
        // Quick Start Guide
        help.append("<div style='margin-bottom: 25px; padding: 15px; background: #fff3cd; border-left: 5px solid #ffc107; border-radius: 5px;'>");
        help.append("<h3 style='margin: 0 0 10px 0; color: #856404;'>⚡ Quick Start (5 Steps)</h3>");
        help.append("<ol style='margin: 0; padding-left: 20px; line-height: 2.2; font-size: 14px;'>");
        help.append("<li><b>Press F2</b> → Search products (type name/scan barcode)</li>");
        help.append("<li><b>Arrow Keys ↑↓←→</b> → Browse products</li>");
        help.append("<li><b>Enter</b> → Add product to cart</li>");
        help.append("<li><b>F4-F7</b> → Select payment method (Cash/Card/Credit/Cheque)</li>");
        help.append("<li><b>F9</b> → Complete sale!</li>");
        help.append("</ol>");
        help.append("</div>");
        
        // IMPORTANT: Arrow Keys Notice
        help.append("<div style='margin-bottom: 25px; padding: 15px; background: #d4edda; border-left: 5px solid #28a745; border-radius: 5px;'>");
        help.append("<h3 style='margin: 0 0 10px 0; color: #155724;'>✅ FIXED: Arrow Keys Now Work Correctly!</h3>");
        help.append("<ul style='margin: 0; padding-left: 20px; line-height: 2; font-size: 14px;'>");
        help.append("<li><b>Plain Arrow Keys ↑↓←→</b> = Navigate PRODUCTS only</li>");
        help.append("<li><b>Alt + ↑↓</b> = Navigate CART ITEMS</li>");
        help.append("<li><b>Payment methods won't interfere</b> with arrow navigation</li>");
        help.append("</ul>");
        help.append("</div>");
        
        // Categorized shortcuts
        String[][] categories = {
            {"Essential", "#4CAF50"},
            {"Payment", "#2196F3"},
            {"Transaction", "#FF9800"},
            {"Navigation", "#9C27B0"},
            {"Cart Nav", "#E91E63"},
            {"Cart Edit", "#F44336"},
            {"Quick", "#607D8B"}
        };
        
        for (String[] cat : categories) {
            String category = cat[0];
            String color = cat[1];
            
            help.append("<div style='margin-bottom: 20px;'>");
            help.append("<h3 style='color: ").append(color).append("; margin: 15px 0 10px 0; padding: 8px; background: white; border-radius: 5px; border-left: 5px solid ").append(color).append(";'>");
            help.append(getCategoryTitle(category));
            help.append("</h3>");
            help.append("<table cellpadding='0' cellspacing='0' style='width: 100%; border-collapse: separate; border-spacing: 0 5px;'>");
            
            for (ShortcutInfo info : shortcuts.values()) {
                if (info.category.equals(category)) {
                    help.append("<tr style='background: white;'>")
                        .append("<td style='padding: 12px 15px; border-radius: 5px 0 0 5px; width: 35%;'><span style='font-size: 18px;'>").append(info.icon).append("</span> <b style='color: ").append(color).append("; font-size: 13px;'>")
                        .append(info.keys)
                        .append("</b></td>")
                        .append("<td style='padding: 12px 15px; border-radius: 0 5px 5px 0; color: #555; font-size: 13px;'>")
                        .append(info.description)
                        .append("</td></tr>");
                }
            }
            help.append("</table>");
            help.append("</div>");
        }
        
        // DISCOUNT WORKFLOW Guide
        help.append("<div style='margin-top: 25px; padding: 20px; background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); border-radius: 10px; color: white;'>");
        help.append("<h3 style='margin: 0 0 15px 0;'>💰 Discount Workflow (Step by Step)</h3>");
        help.append("<table style='width: 100%; color: white;' cellpadding='8'>");
        help.append("<tr><td style='background: rgba(0,0,0,0.2); border-radius: 5px; margin-bottom: 5px;'>");
        help.append("<b>1. Global Discount</b> (entire cart)<br>");
        help.append("→ Press <b>F10</b> or <b>Alt+D</b> → Opens discount dialog");
        help.append("</td></tr>");
        help.append("<tr><td style='background: rgba(0,0,0,0.2); border-radius: 5px; margin-bottom: 5px;'>");
        help.append("<b>2. Item Discount</b> (individual products)<br>");
        help.append("→ Press <b>Alt+↑/↓</b> to select cart item<br>");
        help.append("→ Press <b>Alt+R</b> or <b>Ctrl+D</b> → Edit discount field<br>");
        help.append("→ Type amount → Press <b>Enter</b>");
        help.append("</td></tr>");
        help.append("<tr><td style='background: rgba(0,0,0,0.2); border-radius: 5px;'>");
        help.append("<b>3. Quick Remove Discount</b><br>");
        help.append("→ Select item (<b>Alt+↑/↓</b>)<br>");
        help.append("→ Press <b>Shift+D</b> → Instant zero discount");
        help.append("</td></tr>");
        help.append("</table>");
        help.append("</div>");
        
        // Pro Tips
        help.append("<div style='margin-top: 25px; padding: 20px; background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); border-radius: 10px; color: white;'>");
        help.append("<h3 style='margin: 0 0 15px 0;'>💡 Pro Tips & Tricks</h3>");
        help.append("<ul style='margin: 0; padding-left: 20px; line-height: 2.2;'>");
        help.append("<li><b>Arrow Keys Confusion?</b> Plain arrows = products, Alt+arrows = cart</li>");
        help.append("<li><b>Payment Methods:</b> F4-F7 select payment, arrows won't change them anymore!</li>");
        help.append("<li><b>Fast Editing:</b> Alt+Q (quantity), Alt+R (discount) - instant focus</li>");
        help.append("<li><b>Barcode Speed:</b> Scan → Enter → Added! No manual clicking needed</li>");
        help.append("<li><b>Cart Full?</b> Alt+↑/↓ to navigate, +/- to adjust, Alt+X to remove</li>");
        help.append("<li><b>Lost Focus?</b> Press F2 (product search) or F3 (cart) to refocus</li>");
        help.append("</ul>");
        help.append("</div>");
        
        // Common Issues Fixed
        help.append("<div style='margin-top: 25px; padding: 20px; background: #e3f2fd; border-left: 5px solid #2196F3; border-radius: 5px;'>");
        help.append("<h3 style='margin: 0 0 15px 0; color: #1565c0;'>🔧 Common Issues FIXED</h3>");
        help.append("<table style='width: 100%; color: #1565c0;' cellpadding='8'>");
        help.append("<tr><td style='background: white; border-radius: 5px; margin-bottom: 5px;'>");
        help.append("❌ <b>OLD:</b> Arrow keys changed payment method<br>");
        help.append("✅ <b>NOW:</b> Arrows only navigate products. Use F4-F7 for payment!");
        help.append("</td></tr>");
        help.append("<tr><td style='background: white; border-radius: 5px; margin-bottom: 5px;'>");
        help.append("❌ <b>OLD:</b> Discount shortcuts didn't work<br>");
        help.append("✅ <b>NOW:</b> Alt+R, Ctrl+D, Shift+D all work perfectly!");
        help.append("</td></tr>");
        help.append("<tr><td style='background: white; border-radius: 5px;'>");
        help.append("❌ <b>OLD:</b> Confusing navigation between cart & products<br>");
        help.append("✅ <b>NOW:</b> Plain arrows = products, Alt+arrows = cart. Simple!");
        help.append("</td></tr>");
        help.append("</table>");
        help.append("</div>");
        
        // Footer
        help.append("<div style='margin-top: 20px; text-align: center; padding: 15px; background: white; border-radius: 5px;'>");
        help.append("<p style='color: #666; font-size: 12px; margin: 0;'>");
        help.append("<b>Press F1</b> anytime to view this help | <b>Version:</b> 4.0 User-Friendly Enhanced");
        help.append("</p>");
        help.append("</div>");
        
        help.append("</body></html>");
        return help.toString();
    }
    
    private String getCategoryTitle(String category) {
        switch(category) {
            case "Essential": return "🔑 Essential Controls";
            case "Payment": return "💰 Payment Methods (F Keys)";
            case "Transaction": return "📋 Transaction Actions";
            case "Navigation": return "🧭 Product Navigation (Plain Arrows)";
            case "Cart Nav": return "🛒 Cart Navigation (Alt+Arrows)";
            case "Cart Edit": return "✏️ Cart Item Editing";
            case "Quick": return "⚡ Quick Actions";
            default: return category;
        }
    }
    
    /**
     * Show help dialog with improved styling
     */
    public void showHelp(JComponent parent) {
        JEditorPane editorPane = new JEditorPane("text/html", getShortcutHelp());
        editorPane.setEditable(false);
        editorPane.setCaretPosition(0);
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(750, 650));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        JOptionPane.showMessageDialog(
            SwingUtilities.getWindowAncestor(parent),
            scrollPane,
            "⌨️ Keyboard Shortcuts - Press F1 Anytime",
            JOptionPane.PLAIN_MESSAGE
        );
    }
    
    /**
     * Create a status indicator label for visual feedback
     */
    public JLabel createStatusIndicator() {
        statusIndicator = new JLabel("🔍 Product Mode - Use Arrow Keys");
        statusIndicator.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusIndicator.setForeground(new Color(102, 102, 102));
        statusIndicator.setOpaque(true);
        statusIndicator.setBackground(new Color(230, 245, 255));
        statusIndicator.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(28, 181, 187), 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        statusIndicator.setHorizontalAlignment(SwingConstants.CENTER);
        return statusIndicator;
    }
    
    /**
     * Update status indicator
     */
    public void updateStatus(String mode, String hint) {
        if (statusIndicator != null) {
            String icon = mode.contains("Product") ? "🔍" : "🛒";
            statusIndicator.setText(icon + " " + mode + " - " + hint);
            
            Color bgColor = mode.contains("Product") ? 
                new Color(230, 245, 255) : new Color(255, 240, 245);
            statusIndicator.setBackground(bgColor);
        }
    }
    
    public ShortcutInfo getShortcut(String id) {
        return shortcuts.get(id);
    }
    
    public Map<String, ShortcutInfo> getAllShortcuts() {
        return new HashMap<>(shortcuts);
    }
    
    /**
     * Shortcut information with icon
     */
    public static class ShortcutInfo {
        public final String id;
        public final String keys;
        public final String description;
        public final String category;
        public final String icon;
        
        ShortcutInfo(String id, String keys, String description, String category, String icon) {
            this.id = id;
            this.keys = keys;
            this.description = description;
            this.category = category;
            this.icon = icon;
        }
        
        @Override
        public String toString() {
            return icon + " " + keys + " - " + description;
        }
    }
}