package pharmacyims.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int saleId;
    private Timestamp saleDate;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal changeGiven;
    private Integer userId;
    private String cashierName;
    private List<SaleItem> items = new ArrayList<>();

    public Sale() {}

    public Sale(BigDecimal totalAmount, BigDecimal amountPaid, BigDecimal changeGiven, Integer userId) {
        this(0, null, totalAmount, amountPaid, changeGiven, userId);
    }

    public Sale(int saleId, Timestamp saleDate, BigDecimal totalAmount, BigDecimal amountPaid, BigDecimal changeGiven, Integer userId) {
        this.saleId = saleId;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.changeGiven = changeGiven;
        this.userId = userId;
    }

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public Timestamp getSaleDate() { return saleDate; }
    public void setSaleDate(Timestamp saleDate) { this.saleDate = saleDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public BigDecimal getChangeGiven() { return changeGiven; }
    public void setChangeGiven(BigDecimal changeGiven) { this.changeGiven = changeGiven; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }

    public void addItem(SaleItem item) {
        this.items.add(item);
    }
}
