
import java.time.LocalDate;

public class EWasteItem {
    private String itemId;
    private String itemName;
    private String category;
    private String condition;
    private int quantity;
    private String currentHolder;
    private String status;
    private LocalDate registeredDate;

    public EWasteItem(String itemId, String itemName,
                      String category, String condition,
                      int quantity, String currentHolder,
                      String status, LocalDate registeredDate) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.category = category;
        this.condition = condition;
        this.quantity = quantity;
        this.currentHolder = currentHolder;
        this.status = status;
        this.registeredDate = registeredDate;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getCategory() {
        return category;
    }

    public String getCondition() {
        return condition;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getCurrentHolder() {
        return currentHolder;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getRegisteredDate() {
        return registeredDate;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Quantity must be greater than zero."
            );
        }
        this.quantity = quantity;
    }

    public void setCurrentHolder(String currentHolder) {
        this.currentHolder = currentHolder;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRegisteredDate(LocalDate registeredDate) {
        this.registeredDate = registeredDate;
    }

    @Override
    public String toString() {
        return "Item ID: " + itemId
            + "\nName: " + itemName
            + "\nCategory: " + category
            + "\nCondition: " + condition
            + "\nQuantity: " + quantity
            + "\nCurrent Holder: " + currentHolder
            + "\nStatus: " + status
            + "\nRegistered Date: " + registeredDate;
    }
}

