package model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class CsvQuestionRepository implements QuestionRepository {

    private final Path csvPath;
    private final List<Question> cache = new ArrayList<>();
    private final Random rnd = new Random();

    public CsvQuestionRepository(Path csvPath) {
        this.csvPath = csvPath;
        ensureFileExists();
        reload();
    }

    public CsvQuestionRepository(String csvPath) {
        this(Path.of(csvPath));
    }

    @Override
    public List<Question> findAll() {
        return new ArrayList<>(cache);
    }

    @Override
    public List<Question> findByLevel(QuestionLevel level) {
        List<Question> result = new ArrayList<>();
        for (Question q : cache) if (q.getLevel() == level) result.add(q);
        return result;
    }

    @Override
    public Optional<Question> randomByLevel(QuestionLevel level) {
        List<Question> list = findByLevel(level);
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(rnd.nextInt(list.size())));
    }

    @Override
    public void add(Question q) {
        validate(q);
        String id = q.getId().trim();
        if (findById(id) != null) throw new IllegalArgumentException("ID already exists: " + id);
        cache.add(q);
    }

    @Override
    public boolean update(Question q) {
        validate(q);
        String id = q.getId().trim();
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).getId().trim().equals(id)) {
                cache.set(i, q); // immutable replace
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null) return false;
        final String target = id.trim();
        return cache.removeIf(q -> q.getId().trim().equals(target));
    }

    @Override
    public void save() {
        try {
            Files.createDirectories(csvPath.getParent() == null ? Path.of(".") : csvPath.getParent());

            try (BufferedWriter bw = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

                bw.write("ID,Question,Difficulty,A,B,C,D,Correct Answer");
                bw.newLine();

                for (Question q : cache) {
                    List<String> cols = new ArrayList<>();
                    cols.add(q.getId());
                    cols.add(q.getText());
                    cols.add(String.valueOf(levelToDifficulty(q.getLevel())));

                    List<String> o = q.getOptions();
                    cols.add(o.get(0));
                    cols.add(o.get(1));
                    cols.add(o.get(2));
                    cols.add(o.get(3));

                    cols.add(String.valueOf((char) ('A' + q.getCorrectIndex())));

                    bw.write(toCsvLine(cols));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save Questions CSV: " + csvPath.toAbsolutePath(), e);
        }
    }

    @Override
    public void reload() {
        cache.clear();

        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
                if (line.isBlank()) continue;

                Question q = parseLine(line);
                if (q != null) cache.add(q);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Questions CSV: " + csvPath.toAbsolutePath(), e);
        }
    }

    // ===== helpers =====

    private void ensureFileExists() {
        try {
            Files.createDirectories(csvPath.getParent() == null ? Path.of(".") : csvPath.getParent());
            if (!Files.exists(csvPath)) {
                Files.writeString(csvPath,
                        "ID,Question,Difficulty,A,B,C,D,Correct Answer\n",
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot create Questions CSV: " + csvPath.toAbsolutePath(), e);
        }
    }

    private Question findById(String id) {
        for (Question q : cache) if (q.getId().trim().equals(id)) return q;
        return null;
    }

    private void validate(Question q) {
        if (q == null) throw new IllegalArgumentException("Question is null");
        if (q.getId() == null || q.getId().trim().isEmpty()) throw new IllegalArgumentException("ID is required");
        if (q.getText() == null || q.getText().trim().isEmpty()) throw new IllegalArgumentException("Question text is required");
        if (q.getOptions() == null || q.getOptions().size() != 4) throw new IllegalArgumentException("Exactly 4 options required");
        if (q.getCorrectIndex() < 0 || q.getCorrectIndex() > 3) throw new IllegalArgumentException("CorrectIndex must be 0..3");
        if (q.getLevel() == null) throw new IllegalArgumentException("Level is required");
    }

    private Question parseLine(String line) {
        List<String> cols = splitCsvLine(line);
        if (cols.size() < 8) return null;

        String id = cols.get(0).trim();
        String text = cols.get(1).trim();
        String diff = cols.get(2).trim();

        String a = cols.get(3).trim();
        String b = cols.get(4).trim();
        String c = cols.get(5).trim();
        String d = cols.get(6).trim();

        String correct = cols.get(7).trim();

        QuestionLevel level = parseLevel(diff);
        int correctIndex = parseCorrectIndex(correct);

        return new Question(id, text, Arrays.asList(a, b, c, d), correctIndex, level);
    }

    private QuestionLevel parseLevel(String raw) {
        return switch (raw) {
            case "1" -> QuestionLevel.EASY;
            case "2" -> QuestionLevel.MEDIUM;
            case "3" -> QuestionLevel.HARD;
            case "4" -> QuestionLevel.EXPERT;
            default -> throw new IllegalArgumentException("Unknown difficulty: " + raw);
        };
    }

    private int levelToDifficulty(QuestionLevel level) {
        return switch (level) {
            case EASY -> 1;
            case MEDIUM -> 2;
            case HARD -> 3;
            case EXPERT -> 4;
        };
    }

    private int parseCorrectIndex(String raw) {
        raw = raw.trim().toUpperCase(Locale.ROOT);
        if (raw.length() == 1 && raw.charAt(0) >= 'A' && raw.charAt(0) <= 'D') return raw.charAt(0) - 'A';
        if (raw.matches("\\d+")) {
            int idx = Integer.parseInt(raw);
            if (idx >= 1 && idx <= 4) return idx - 1;
        }
        throw new IllegalArgumentException("Unknown correct answer: " + raw);
    }

    private List<String> splitCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                cols.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        cols.add(cur.toString());
        return cols;
    }

    private String toCsvLine(List<String> cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(escapeCsv(cols.get(i)));
        }
        return sb.toString();
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        boolean needQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String out = s.replace("\"", "\"\"");
        return needQuotes ? "\"" + out + "\"" : out;
    }
}
