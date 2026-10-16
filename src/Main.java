
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);

    private static final FileStorage storage = new FileStorage();
    private static final EWasteManager manager = new EWasteManager();

    public static void main(String[] args) {
        // Load saved data
        for (EWasteItem item : storage.loadItems()) {
            manager.addLoadedItem(item);
        }

        for (HandoverRecord h : storage.loadHandovers()) {
            manager.addLoadedHandover(h);
        }

        System.out.println("==================================");
        System.out.println(" E-WASTE TRACEABILITY SYSTEM");
        System.out.println("==================================");
        System.out.println("Items loaded: " + manager.getItems().size());
        System.out.println("Handovers loaded: "
                + manager.getHandovers().size());

        boolean running = true;

        while (running) {
            showMenu();
            int choice = readInt("Enter your choice: ", 0, 11);

            try {
                switch (choice) {
                    case 1:
                        registerItem();
                        break;
                    case 2:
                        viewAllItems();
                        break;
                    case 3:
                        searchItem();
                        break;
                    case 4:
                        filterCategory();
                        break;
                    case 5:
                        recordHandover();
                        break;
                    case 6:
                        viewHistory();
                        break;
                    case 7:
                        filterHolder();
                        break;
                    case 8:
                        recordFinalOutcome();
                        break;
                    case 9:
                        showGaps();
                        break;
                    case 10:
                        manager.printSummary();
                        break;
                    case 11:
                        deleteItem();
                        break;
                    case 0:
                        storage.saveItems(manager);
                        storage.saveHandovers(manager);
                        running = false;
                        System.out.println("Data saved. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid data: " + e.getMessage());
            } catch (Exception e) {
                System.out.println(
                    "Operation failed: " + e.getMessage()
                );
            }
        }

        sc.close();
    }

    private static void showMenu() {
        System.out.println("\n========== MAIN MENU ==========");
        System.out.println("1. Register New E-Waste Item");
        System.out.println("2. View All Items");
        System.out.println("3. Search Item by ID or Name");
        System.out.println("4. Filter Items by Category");
        System.out.println("5. Record Item Handover");
        System.out.println("6. View Complete Handover History");
        System.out.println("7. View Items by Current Holder");
        System.out.println("8. Record Final Processing Outcome");
        System.out.println("9. Find Incomplete Traceability Records");
        System.out.println("10. View Summary Report");
        System.out.println("11. Delete Eligible Item");
        System.out.println("0. Save and Exit");
        System.out.println("===============================");
    }

    private static String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = sc.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            System.out.println("This field cannot be empty.");
        }
    }

    private static String readOptional(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            try {
                int value = Integer.parseInt(input);

                if (value >= min && value <= max) {
                    return value;
                }

                System.out.println(
                    "Enter a number from " + min + " to " + max + "."
                );

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (YYYY-MM-DD): ");
            String input = sc.nextLine().trim();

            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println(
                    "Invalid date. Example: 2026-10-16"
                );
            }
        }
    }

    private static String generateItemId() {
        int max = 0;

        for (EWasteItem item : manager.getItems()) {
            String id = item.getItemId();

            if (id.matches("E\\d+")) {
                try {
                    max = Math.max(max,
                        Integer.parseInt(id.substring(1)));
                } catch (NumberFormatException ignored) {
                    // Ignore IDs too large to parse
                }
            }
        }

        return String.format("E%03d", max + 1);
    }

    private static void registerItem() {
        System.out.println("\n--- Register E-Waste ---");

        String id = generateItemId();
        System.out.println("Generated Item ID: " + id);

        String name = readRequired("Item name: ");
        String category = readRequired(
            "Category (Computer/Mobile/Battery/Other): "
        );
        String condition = readRequired(
            "Condition (Working/Damaged/Non-functional): "
        );
        int quantity = readInt("Quantity: ", 1, Integer.MAX_VALUE);
        String holder = readRequired(
            "Current holder (owner/collection centre): "
        );
        LocalDate date = readDate("Registration date");

        if (date.isAfter(LocalDate.now())) {
            System.out.println("Registration date cannot be in the future.");
            return;
        }

        EWasteItem item = new EWasteItem(
            id, name, category, condition, quantity,
            holder, "REGISTERED", date
        );

        System.out.println(manager.registerItem(item));
    }

    private static void viewAllItems() {
        System.out.println("\n--- All E-Waste Items ---");

        if (manager.getItems().isEmpty()) {
            System.out.println("No items registered.");
            return;
        }

        for (EWasteItem item : manager.getItems()) {
            System.out.println("--------------------------");
            System.out.println(item);
        }
    }

    private static void searchItem() {
        System.out.println("\n--- Search Item ---");
        String query = readRequired("Enter item ID or name: ");

        EWasteItem item = manager.findById(query);

        if (item != null) {
            System.out.println(item);
            return;
        }

        List<EWasteItem> results = manager.searchByName(query);

        if (results.isEmpty()) {
            System.out.println("No matching item found.");
        } else {
            for (EWasteItem result : results) {
                System.out.println("--------------------------");
                System.out.println(result);
            }
        }
    }

    private static void filterCategory() {
        String category = readRequired("Enter category: ");
        List<EWasteItem> results =
                manager.filterByCategory(category);

        printItems(results);
    }

    private static void filterHolder() {
        String holder = readRequired("Enter current holder: ");
        List<EWasteItem> results =
                manager.filterByHolder(holder);

        printItems(results);
    }

    private static void printItems(List<EWasteItem> items) {
        if (items.isEmpty()) {
            System.out.println("No matching records found.");
            return;
        }

        for (EWasteItem item : items) {
            System.out.println("--------------------------");
            System.out.println(item);
        }
    }

    private static void recordHandover() {
        System.out.println("\n--- Record Handover ---");

        String itemId = readRequired("Item ID: ");
        EWasteItem item = manager.findById(itemId);

        if (item == null) {
            System.out.println("Item not found.");
            return;
        }

        System.out.println("Current holder: " + item.getCurrentHolder());

        String newHolder = readRequired("Transfer to: ");
        LocalDate date = readDate("Handover date");

        if (date.isAfter(LocalDate.now())) {
            System.out.println("Handover date cannot be in the future.");
            return;
        }

        String condition = readRequired(
            "Condition at handover: "
        );
        String notes = readOptional("Notes (optional): ");

        System.out.println(manager.recordHandover(
            itemId, newHolder, date, condition, notes
        ));
    }

    private static void viewHistory() {
        String itemId = readRequired("Enter item ID: ");

        if (manager.findById(itemId) == null) {
            System.out.println("Item not found.");
            return;
        }

        List<HandoverRecord> history =
                manager.getItemHistory(itemId);

        System.out.println("\n--- Handover History: " + itemId + " ---");

        if (history.isEmpty()) {
            System.out.println("No handovers recorded.");
            return;
        }

        for (HandoverRecord h : history) {
            System.out.println("--------------------------");
            System.out.println(h);
        }
    }

    private static void recordFinalOutcome() {
        System.out.println("\n--- Final Processing Outcome ---");
        String itemId = readRequired("Item ID: ");

        System.out.println("1. Processing reported");
        System.out.println("2. Recycling reported");
        System.out.println("3. Dismantling reported");
        System.out.println("4. Disposal reported");

        int choice = readInt("Choose outcome: ", 1, 4);

        String outcome;

        switch (choice) {
            case 1:
                outcome = "PROCESSING_REPORTED";
                break;
            case 2:
                outcome = "RECYCLED_REPORTED";
                break;
            case 3:
                outcome = "DISMANTLED_REPORTED";
                break;
            default:
                outcome = "DISPOSAL_REPORTED";
        }

        System.out.println(
            manager.recordFinalOutcome(itemId, outcome)
        );
    }

    private static void showGaps() {
        System.out.println("\n--- Traceability Findings ---");
        List<String> gaps = manager.detectGaps();

        if (gaps.isEmpty()) {
            System.out.println("No gaps found by the current checks.");
            return;
        }

        for (String gap : gaps) {
            System.out.println("- " + gap);
        }
    }

    private static void deleteItem() {
        String itemId = readRequired("Enter item ID to delete: ");

        System.out.println(manager.deleteItem(itemId));
    }
}
