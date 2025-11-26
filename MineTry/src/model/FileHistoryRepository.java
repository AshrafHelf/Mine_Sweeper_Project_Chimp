package model;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileHistoryRepository implements HistoryRepository {

    private final Path path;
    private final List<GameRecord> cache = new ArrayList<>();

    public FileHistoryRepository(String filePath) {
        this(Path.of(filePath));
    }

    public FileHistoryRepository(Path path) {
        this.path = path;
        ensureFileExists();
        loadAllFromDisk();
    }

    @Override
    public synchronized List<GameRecord> loadAll() {
        return new ArrayList<>(cache);
    }

    @Override
    public synchronized void append(GameRecord record) {
        cache.add(record);
        appendToFile(record);
    }

    // ---------- internal helpers ----------

    private void ensureFileExists() {
        try {
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
                // write header
                try (BufferedWriter bw = Files.newBufferedWriter(path)) {
                    bw.write("DateTime,Difficulty,Player1,Player2,Score,Won");
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAllFromDisk() {
        cache.clear();
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { // skip header
                    first = false;
                    continue;
                }
                if (line.isBlank()) continue;
                try {
                    GameRecord rec = GameRecord.fromCsvLine(line);
                    cache.add(rec);
                } catch (Exception ex) {
                    System.err.println("Bad history row, skipping: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void appendToFile(GameRecord record) {
        try (BufferedWriter bw = Files.newBufferedWriter(path,
                StandardOpenOption.APPEND)) {
            bw.write(record.toCsvLine());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
