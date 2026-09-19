package pharmacyims.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Medicine {
    private int medicineId;
    private String name;
    private String company;
    private String medicineType;
    private BigDecimal price;
    private int quantityInStock;
    private int reorderLevel = 10;
    private Date expiryDate;
    private Integer supplierId;
    private String supplierName;

    public Medicine() {}

    public Medicine(String name, String company, String medicineType,
                    BigDecimal price, int quantityInStock, int reorderLevel,
                    Date expiryDate, Integer supplierId) {
        this(0, name, company, medicineType, price, quantityInStock, reorderLevel, expiryDate, supplierId);
    }

    public Medicine(int medicineId, String name, String company, String medicineType,
                    BigDecimal price, int quantityInStock, int reorderLevel,
                    Date expiryDate, Integer supplierId) {
        this.medicineId = medicineId;
        this.name = name;
        this.company = company;
        this.medicineType = medicineType;
        this.price = price;
        this.quantityInStock = quantityInStock;
        this.reorderLevel = reorderLevel;
        this.expiryDate = expiryDate;
        this.supplierId = supplierId;
    }

    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getMedicineType() { return medicineType; }
    public void setMedicineType(String medicineType) { this.medicineType = medicineType; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(int quantityInStock) { this.quantityInStock = quantityInStock; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public boolean isOutOfStock() {
        return quantityInStock <= 0;
    }

    public boolean isLowStock() {
        return quantityInStock > 0 && quantityInStock <= reorderLevel;
    }

    public long getDaysUntilExpiry() {
        if (expiryDate == null) return Long.MAX_VALUE;
        LocalDate exp = expiryDate.toLocalDate();
        return ChronoUnit.DAYS.between(LocalDate.now(), exp);
    }

    public boolean isExpired() {
        return getDaysUntilExpiry() < 0;
    }

    public boolean isExpiringSoon(int daysThreshold) {
        long days = getDaysUntilExpiry();
        return days >= 0 && days <= daysThreshold;
    }

    @Override
    public String toString() {
        return name + " (" + company + ") - $" + price;
    }
}
