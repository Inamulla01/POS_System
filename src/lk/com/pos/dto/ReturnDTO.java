package lk.com.pos.dto;

import java.time.LocalDateTime;

public class ReturnDTO {
    private int returnId;
    private int salesId;
    private LocalDateTime returnDate;
    private double totalReturnAmount;
    private double totalDiscountPrice;
    private int returnReasonId;
    private int statusId;
    private int userId;

    // Constructors
    public ReturnDTO() {}

    public ReturnDTO(int returnId, int salesId, LocalDateTime returnDate, 
                    double totalReturnAmount, double totalDiscountPrice, 
                    int returnReasonId, int statusId, int userId) {
        this.returnId = returnId;
        this.salesId = salesId;
        this.returnDate = returnDate;
        this.totalReturnAmount = totalReturnAmount;
        this.totalDiscountPrice = totalDiscountPrice;
        this.returnReasonId = returnReasonId;
        this.statusId = statusId;
        this.userId = userId;
    }

    // Getters and Setters
    public int getReturnId() { return returnId; }
    public void setReturnId(int returnId) { this.returnId = returnId; }

    public int getSalesId() { return salesId; }
    public void setSalesId(int salesId) { this.salesId = salesId; }

    public LocalDateTime getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }

    public double getTotalReturnAmount() { return totalReturnAmount; }
    public void setTotalReturnAmount(double totalReturnAmount) { this.totalReturnAmount = totalReturnAmount; }

    public double getTotalDiscountPrice() { return totalDiscountPrice; }
    public void setTotalDiscountPrice(double totalDiscountPrice) { this.totalDiscountPrice = totalDiscountPrice; }

    public int getReturnReasonId() { return returnReasonId; }
    public void setReturnReasonId(int returnReasonId) { this.returnReasonId = returnReasonId; }

    public int getStatusId() { return statusId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}