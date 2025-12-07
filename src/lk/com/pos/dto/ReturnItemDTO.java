package lk.com.pos.dto;

public class ReturnItemDTO {
    private int returnItemId;
    private int returnId;
    private int stockId;
    private double returnQty;
    private double unitReturnPrice;
    private double discountPrice;
    private double totalReturnAmount;

    // Constructors
    public ReturnItemDTO() {}

    public ReturnItemDTO(int returnItemId, int returnId, int stockId, 
                        double returnQty, double unitReturnPrice, 
                        double discountPrice, double totalReturnAmount) {
        this.returnItemId = returnItemId;
        this.returnId = returnId;
        this.stockId = stockId;
        this.returnQty = returnQty;
        this.unitReturnPrice = unitReturnPrice;
        this.discountPrice = discountPrice;
        this.totalReturnAmount = totalReturnAmount;
    }

    // Getters and Setters
    public int getReturnItemId() { return returnItemId; }
    public void setReturnItemId(int returnItemId) { this.returnItemId = returnItemId; }

    public int getReturnId() { return returnId; }
    public void setReturnId(int returnId) { this.returnId = returnId; }

    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }

    public double getReturnQty() { return returnQty; }
    public void setReturnQty(double returnQty) { this.returnQty = returnQty; }

    public double getUnitReturnPrice() { return unitReturnPrice; }
    public void setUnitReturnPrice(double unitReturnPrice) { this.unitReturnPrice = unitReturnPrice; }

    public double getDiscountPrice() { return discountPrice; }
    public void setDiscountPrice(double discountPrice) { this.discountPrice = discountPrice; }

    public double getTotalReturnAmount() { return totalReturnAmount; }
    public void setTotalReturnAmount(double totalReturnAmount) { this.totalReturnAmount = totalReturnAmount; }
}