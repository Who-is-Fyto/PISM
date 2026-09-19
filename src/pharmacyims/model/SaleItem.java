package pharmacyims.model;

import java.math.BigDecimal;

public class SaleItem {
    private int saleItemId;
    private int saleId;
    private int medicineId;
    private String medicineName;
    private int quantitySold;
    private BigDecimal priceAtSale;
    private BigDecimal subtotal;

    public SaleItem() {}

    public SaleItem(int saleItemId, int saleId, int medicineId, int quantitySold, BigDecimal priceAtSale) {
        this.saleItemId = saleItemId;
        this.saleId = saleId;
        this.medicineId = medicineId;
        this.quantitySold = quantitySold;
        this.priceAtSale = priceAtSale;
        if (priceAtSale != null) {
            this.subtotal = priceAtSale.multiply(BigDecimal.valueOf(quantitySold));
        }
    }

    public SaleItem(int medicineId, String medicineName, int quantitySold, BigDecimal priceAtSale) {
        this(0, 0, medicineId, quantitySold, priceAtSale);
        this.medicineName = medicineName;
    }

    public int getSaleItemId() { return saleItemId; }
    public void setSaleItemId(int saleItemId) { this.saleItemId = saleItemId; }

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public int getQuantitySold() { return quantitySold; }
    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
        if (this.priceAtSale != null) {
            this.subtotal = this.priceAtSale.multiply(BigDecimal.valueOf(quantitySold));
        }
    }

    public BigDecimal getPriceAtSale() { return priceAtSale; }
    public void setPriceAtSale(BigDecimal priceAtSale) {
        this.priceAtSale = priceAtSale;
        if (priceAtSale != null) {
            this.subtotal = priceAtSale.multiply(BigDecimal.valueOf(this.quantitySold));
        }
    }

    public BigDecimal getSubtotal() {
        if (subtotal == null && priceAtSale != null) {
            subtotal = priceAtSale.multiply(BigDecimal.valueOf(quantitySold));
        }
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
