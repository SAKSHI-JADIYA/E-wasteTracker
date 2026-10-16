
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FileStorage {
    private final Path dataDirectory = Paths.get("data");
    private final Path itemsFile = dataDirectory.resolve("items.csv");
    private final Path handoversFile =
            dataDirectory.resolve("handovers.csv");

    public FileStorage() {
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            System.out.println(
                "Warning: Cannot create data directory: "
                + e.getMessage()
            );
        }
    }

    // Load item records from CSV
    public List<EWasteItem> loadItems() {
        List<EWasteItem> loaded = new ArrayList<>();

        if (!Files.exists(itemsFile)) {
            return loaded;
        }

        try (BufferedReader reader = Files.newBufferedReader(
                itemsFile, StandardCharsets.UTF_8)) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()
                        || line.startsWith("itemId,")) {
                    continue;
                }

                try {
                    List<String> f = parseCsv(line);

                    if (f.size() != 8) {
                        throw new IllegalArgumentException(
                            "Expected 8 columns."
                        );
                    }

                    EWasteItem item = new EWasteItem(
                        f.get(0),
                        f.get(1),
                        f.get(2),
                        f.get(3),
                        Integer.parseInt(f.get(4)),
                        f.get(5),
                        f.get(6),
                        LocalDate.parse(f.get(7))
                    );

                    if (item.getItemId().trim().isEmpty()
                            || item.getQuantity() <= 0) {
                        throw new IllegalArgumentException(
                            "Invalid item data."
                        );
                    }

                    loaded.add(item);

                } catch (RuntimeException e) {
                    System.out.println(
                        "Skipping invalid item record at line "
                        + lineNumber + ": " + e.getMessage()
                    );
                }
            }

        } catch (IOException e) {
            System.out.println(
                "Could not read items file: " + e.getMessage()
            );
        }

        return loaded;
    }

    // Load handover records from CSV
    public List<HandoverRecord> loadHandovers() {
        List<HandoverRecord> loaded = new ArrayList<>();

        if (!Files.exists(handoversFile)) {
            return loaded;
        }

        try (BufferedReader reader = Files.newBufferedReader(
                handoversFile, StandardCharsets.UTF_8)) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()
                        || line.startsWith("handoverId,")) {
                    continue;
                }

                try {
                    List<String> f = parseCsv(line);

                    if (f.size() != 7) {
                        throw new IllegalArgumentException(
                            "Expected 7 columns."
                        );
                    }

                    HandoverRecord record = new HandoverRecord(
                        f.get(0),
                        f.get(1),
                        f.get(2),
                        f.get(3),
                        LocalDate.parse(f.get(4)),
                        f.get(5),
                        f.get(6)
                    );

                    if (record.getHandoverId().trim().isEmpty()
                            || record.getItemId().trim().isEmpty()) {
                        throw new IllegalArgumentException(
                            "Missing handover or item ID."
                        );
                    }

                    loaded.add(record);

                } catch (RuntimeException e) {
                    System.out.println(
                        "Skipping invalid handover record at line "
                        + lineNumber + ": " + e.getMessage()
                    );
                }
            }

        } catch (IOException e) {
            System.out.println(
                "Could not read handovers file: " + e.getMessage()
            );
        }

        return loaded;
    }

    // Save all item records
    public void saveItems(EWasteManager manager) {
        List<String> lines = new ArrayList<>();

        lines.add("itemId,itemName,category,condition,quantity,"
                + "currentHolder,status,registeredDate");

        for (EWasteItem item : manager.getItems()) {
            lines.add(toCsv(item.getItemId())
                + "," + toCsv(item.getItemName())
                + "," + toCsv(item.getCategory())
                + "," + toCsv(item.getCondition())
                + "," + item.getQuantity()
                + "," + toCsv(item.getCurrentHolder())
                + "," + toCsv(item.getStatus())
                + "," + item.getRegisteredDate());
        }

        writeLines(itemsFile, lines);
    }

    // Save all handover records
    public void saveHandovers(EWasteManager manager) {
        List<String> lines = new ArrayList<>();

        lines.add("handoverId,itemId,fromParty,toParty,"
                + "handoverDate,conditionAtHandover,notes");

        for (HandoverRecord h : manager.getHandovers()) {
            lines.add(toCsv(h.getHandoverId())
                + "," + toCsv(h.getItemId())
                + "," + toCsv(h.getFromParty())
                + "," + toCsv(h.getToParty())
                + "," + h.getHandoverDate()
                + "," + toCsv(h.getConditionAtHandover())
                + "," + toCsv(h.getNotes()));
        }

        writeLines(handoversFile, lines);
    }

    private void writeLines(Path file, List<String> lines) {
        try {
            Files.createDirectories(dataDirectory);

            try (BufferedWriter writer = Files.newBufferedWriter(
                    file,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {

                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            System.out.println("Saved: " + file);

        } catch (IOException e) {
            System.out.println(
                "Error saving " + file + ": " + e.getMessage()
            );
        }
    }

    // Escape CSV values
    private String toCsv(String value) {
        if (value == null) {
            value = "";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // Parse CSV including quoted commas and escaped quotes
    private List<String> parseCsv(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }

        if (inQuotes) {
            throw new IllegalArgumentException(
                "Unclosed quotation mark."
            );
        }

        fields.add(field.toString());
        return fields;
    }
}
