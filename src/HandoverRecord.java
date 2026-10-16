
import java.time.LocalDate;

public class HandoverRecord {
    private String handoverId;
    private String itemId;
    private String fromParty;
    private String toParty;
    private LocalDate handoverDate;
    private String conditionAtHandover;
    private String notes;

    public HandoverRecord(String handoverId, String itemId,
                          String fromParty, String toParty,
                          LocalDate handoverDate,
                          String conditionAtHandover,
                          String notes) {
        this.handoverId = handoverId;
        this.itemId = itemId;
        this.fromParty = fromParty;
        this.toParty = toParty;
        this.handoverDate = handoverDate;
        this.conditionAtHandover = conditionAtHandover;
        this.notes = notes;
    }

    public String getHandoverId() {
        return handoverId;
    }

    public String getItemId() {
        return itemId;
    }

    public String getFromParty() {
        return fromParty;
    }

    public String getToParty() {
        return toParty;
    }

    public LocalDate getHandoverDate() {
        return handoverDate;
    }

    public String getConditionAtHandover() {
        return conditionAtHandover;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public String toString() {
        return "Handover ID: " + handoverId
            + "\nItem ID: " + itemId
            + "\nFrom: " + fromParty
            + "\nTo: " + toParty
            + "\nDate: " + handoverDate
            + "\nCondition: " + conditionAtHandover
            + "\nNotes: " + notes;
    }
}
