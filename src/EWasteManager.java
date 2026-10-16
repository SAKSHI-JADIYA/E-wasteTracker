
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class EWasteManager {
    private final ArrayList<EWasteItem> items;
    private final ArrayList<HandoverRecord> handovers;

    public EWasteManager() {
        items = new ArrayList<>();
        handovers = new ArrayList<>();
    }

    // Used to restore records loaded from files
    public void addLoadedItem(EWasteItem item) {
        if (findById(item.getItemId()) == null) {
            items.add(item);
        }
    }

    public void addLoadedHandover(HandoverRecord record) {
        for (HandoverRecord h : handovers) {
            if (h.getHandoverId().equalsIgnoreCase(
                    record.getHandoverId())) {
                return;
            }
        }
        handovers.add(record);
    }

    public List<EWasteItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<HandoverRecord> getHandovers() {
        return Collections.unmodifiableList(handovers);
    }

    // Register a new item
    public String registerItem(EWasteItem item) {
        if (findById(item.getItemId()) != null) {
            return "Error: Item ID already exists.";
        }

        items.add(item);
        return "Item registered successfully. ID: "
                + item.getItemId();
    }

    // Find an item using its unique ID
    public EWasteItem findById(String itemId) {
        for (EWasteItem item : items) {
            if (item.getItemId().equalsIgnoreCase(itemId)) {
                return item;
            }
        }
        return null;
    }

    // Search by item name
    public List<EWasteItem> searchByName(String name) {
        List<EWasteItem> results = new ArrayList<>();

        for (EWasteItem item : items) {
            if (item.getItemName().toLowerCase()
                    .contains(name.toLowerCase())) {
                results.add(item);
            }
        }
        return results;
    }

    // Filter by category
    public List<EWasteItem> filterByCategory(String category) {
        List<EWasteItem> results = new ArrayList<>();

        for (EWasteItem item : items) {
            if (item.getCategory().equalsIgnoreCase(category)) {
                results.add(item);
            }
        }
        return results;
    }

    // Filter by current holder
    public List<EWasteItem> filterByHolder(String holder) {
        List<EWasteItem> results = new ArrayList<>();

        for (EWasteItem item : items) {
            if (item.getCurrentHolder().equalsIgnoreCase(holder)) {
                results.add(item);
            }
        }
        return results;
    }

    // Generate a handover ID
    private String generateHandoverId() {
        int max = 0;

        for (HandoverRecord h : handovers) {
            String id = h.getHandoverId();

            if (id.matches("H\\d+")) {
                try {
                    max = Math.max(max,
                        Integer.parseInt(id.substring(1)));
                } catch (NumberFormatException ignored) {
                    // Ignore IDs too large to parse
                }
            }
        }

        return String.format("H%03d", max + 1);
    }

    // Record a transfer to a new holder
    public String recordHandover(String itemId, String toParty,
                                 LocalDate date, String condition,
                                 String notes) {
        EWasteItem item = findById(itemId);

        if (item == null) {
            return "Error: Item not found.";
        }

        if (isFinalStatus(item.getStatus())) {
            return "Error: This item already has a final outcome.";
        }

        if (toParty == null || toParty.trim().isEmpty()) {
            return "Error: New holder cannot be empty.";
        }

        if (toParty.equalsIgnoreCase(item.getCurrentHolder())) {
            return "Error: New holder is the same as current holder.";
        }

        if (date == null
                || date.isBefore(item.getRegisteredDate())) {
            return "Error: Invalid handover date.";
        }

        // Ensure handovers are chronological
        LocalDate latestDate = item.getRegisteredDate();

        for (HandoverRecord h : handovers) {
            if (h.getItemId().equalsIgnoreCase(itemId)
                    && h.getHandoverDate().isAfter(latestDate)) {
                latestDate = h.getHandoverDate();
            }
        }

        if (date.isBefore(latestDate)) {
            return "Error: Handover date is before the last transfer.";
        }

        HandoverRecord record = new HandoverRecord(
            generateHandoverId(),
            item.getItemId(),
            item.getCurrentHolder(),
            toParty.trim(),
            date,
            condition,
            notes
        );

        handovers.add(record);
        item.setCurrentHolder(toParty.trim());
        item.setCondition(condition);
        item.setStatus("IN_CUSTODY");

        return "Handover recorded successfully. ID: "
                + record.getHandoverId();
    }

    // Get the full transfer history for an item
    public List<HandoverRecord> getItemHistory(String itemId) {
        List<HandoverRecord> history = new ArrayList<>();

        for (HandoverRecord h : handovers) {
            if (h.getItemId().equalsIgnoreCase(itemId)) {
                history.add(h);
            }
        }

        return history;
    }

    // Record a reported final outcome
    public String recordFinalOutcome(String itemId, String outcome) {
        EWasteItem item = findById(itemId);

        if (item == null) {
            return "Error: Item not found.";
        }

        if (isFinalStatus(item.getStatus())) {
            return "A final outcome is already recorded.";
        }

        if (!outcome.equals("PROCESSING_REPORTED")
                && !outcome.equals("RECYCLED_REPORTED")
                && !outcome.equals("DISMANTLED_REPORTED")
                && !outcome.equals("DISPOSAL_REPORTED")) {
            return "Error: Invalid outcome.";
        }

        item.setStatus(outcome);
        return "Final outcome recorded as " + outcome
                + ". This is a reported, not independently verified, outcome.";
    }

    private boolean isFinalStatus(String status) {
        return status.equals("PROCESSING_REPORTED")
            || status.equals("RECYCLED_REPORTED")
            || status.equals("DISMANTLED_REPORTED")
            || status.equals("DISPOSAL_REPORTED");
    }

    // Find records that need traceability follow-up
    public List<String> detectGaps() {
        List<String> gaps = new ArrayList<>();

        for (EWasteItem item : items) {
            List<HandoverRecord> history =
                getItemHistory(item.getItemId());

            if (history.isEmpty()) {
                gaps.add(item.getItemId()
                    + ": No handover recorded.");
            } else {
                HandoverRecord latest = history.get(0);

                for (HandoverRecord h : history) {
                    if (h.getHandoverDate()
                            .isAfter(latest.getHandoverDate())) {
                        latest = h;
                    }
                }

                if (!latest.getToParty().equalsIgnoreCase(
                        item.getCurrentHolder())) {
                    gaps.add(item.getItemId()
                        + ": Current holder does not match history.");
                }
            }

            if (!isFinalStatus(item.getStatus())) {
                gaps.add(item.getItemId()
                    + ": Final processing outcome not recorded.");
            }
        }

        return gaps;
    }

    // Delete only items with no transfer history
    public String deleteItem(String itemId) {
        EWasteItem item = findById(itemId);

        if (item == null) {
            return "Error: Item not found.";
        }

        if (!getItemHistory(itemId).isEmpty()) {
            return "Cannot delete: item has handover history.";
        }

        items.remove(item);
        return "Item deleted successfully.";
    }

    public void printSummary() {
        int totalQuantity = 0;
        int finalOutcomes = 0;

        for (EWasteItem item : items) {
            totalQuantity += item.getQuantity();

            if (isFinalStatus(item.getStatus())) {
                finalOutcomes++;
            }
        }

        System.out.println("\n===== SUMMARY REPORT =====");
        System.out.println("Unique item records: " + items.size());
        System.out.println("Total quantity recorded: " + totalQuantity);
        System.out.println("Total handovers: " + handovers.size());
        System.out.println("Items with reported final outcomes: "
                + finalOutcomes);
        System.out.println("Traceability findings: "
                + detectGaps().size());
    }
}

