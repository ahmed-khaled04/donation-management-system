package impacthub.beneficiary;

import impacthub.lookup.ResourceCategory;

import java.io.Serializable;

/** Goods the organization keeps in stock and hands out. */
public class Resource implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private final String name;
    private final String unit;
    private int stockQuantity;
    private final ResourceCategory category;

    public Resource(String name, String unit, int stockQuantity, ResourceCategory category) {
        this.name = name;
        this.unit = unit;
        this.stockQuantity = stockQuantity;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public ResourceCategory getCategory() {
        return category;
    }
}
