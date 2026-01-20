package model;

import enums.QuestionLevel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class CsvQuestionRepository implements QuestionRepository {

    private final List<Question> cache = new ArrayList<>();
    private final Random rnd = new Random();

    // ✅ editable external csv
    private final Path csvPath;

    public CsvQuestionRepository(Path csvPath) {
        this.csvPath = csvPath;
        loadAll();
    }

    public CsvQuestionRepository(String csvPath) {
        this(Paths.get(csvPath));
    }

    @Override
    public List<Question> findAll() {
        return new ArrayList<>(cache);
    }

    @Override
    public List<Question> findByLevel(QuestionLevel level) {
        List<Question> result = new ArrayList<>();
        for (Question q : cache) {
            if (q.getLevel() == level) result.add(q);
        }
        return result;
    }

    @Override
    public Optional<Question> randomByLevel(QuestionLevel level) {
        List<Question> list = findByLevel(level);
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(rnd.nextInt(list.size())));
    }

    // =======================
    // Loading
    // =======================

    private void loadAll() {
        cache.clear();

        try (InputStream in = openCsvStream()) {
            if (in == null) {
                throw new IllegalStateException("Questions.csv not found (external file or resource).");
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                boolean first = true;

                while ((line = br.readLine()) != null) {
                    if (first) { first = false; continue; } // header
                    if (line.isBlank()) continue;

                    Question q = parseLine(line);
                    if (q != null) cache.add(q);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InputStream openCsvStream() throws IOException {
        // 1) external editable file first
        if (csvPath != null && Files.exists(csvPath)) {
            return Files.newInputStream(csvPath);
        }
        // 2) fallback to resource inside jar (read-only)
        return CsvQuestionRepository.class.getResourceAsStream("/Questions.csv");
    }

    // =======================
    // Saving (CRUD persistence)
    // =======================

    public void saveAll(List<Question> questions) {
        try {
            if (csvPath.getParent() != null) Files.createDirectories(csvPath.getParent());

            StringBuilder sb = new StringBuilder();
            sb.append("ID,Question,Difficulty,A,B,C,D,Correct Answer\n");

            for (Question q : questions) {
                sb.append(csv(q.getId())).append(",");
                sb.append(csv(q.getText())).append(",");
                sb.append(levelToNumber(q.getLevel())).append(",");

                List<String> opts = q.getOptions();
                sb.append(csv(opts.get(0))).append(",");
                sb.append(csv(opts.get(1))).append(",");
                sb.append(csv(opts.get(2))).append(",");
                sb.append(csv(opts.get(3))).append(",");

                sb.append(indexToLetter(q.getCorrectIndex())).append("\n");
            }

            Files.writeString(
                    csvPath,
                    sb.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            // keep cache in sync
            cache.clear();
            cache.addAll(questions);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String csv(String s) {
        if (s == null) return "";
        String t = s.replace("\"", "\"\"");
        if (t.contains(",") || t.contains("\"") || t.contains("\n")) {
            return "\"" + t + "\"";
        }
        return t;
    }

    private String levelToNumber(QuestionLevel lvl) {
        return switch (lvl) {
            case EASY -> "1";
            case MEDIUM -> "2";
            case HARD -> "3";
            case EXPERT -> "4";
        };
    }

    private String indexToLetter(int idx) {
        return switch (idx) {
            case 0 -> "A";
            case 1 -> "B";
            case 2 -> "C";
            case 3 -> "D";
            default -> "A";
        };
    }

    // =======================
    // Parsing (your existing logic)
    // =======================

    private Question parseLine(String line) {
        List<String> cols = splitCsvLine(line);

        if (cols.size() < 8) {
            System.err.println("Bad CSV row (expected 8+ columns): " + line);
            return null;
        }

        String id            = cols.get(0).trim();
        String text          = cols.get(1).trim();
        String difficultyRaw = cols.get(2).trim();
        String optA          = cols.get(3).trim();
        String optB          = cols.get(4).trim();
        String optC          = cols.get(5).trim();
        String optD          = cols.get(6).trim();
        String correctRaw    = cols.get(7).trim();

        QuestionLevel level = parseLevel(difficultyRaw);
        int correctIndex = parseCorrectIndex(correctRaw);

        List<String> options = new ArrayList<>();
        options.add(optA);
        options.add(optB);
        options.add(optC);
        options.add(optD);

        return new Question(id, text, options, correctIndex, level);
    }

    private QuestionLevel parseLevel(String raw) {
        return switch (raw) {
            case "1" -> QuestionLevel.EASY;
            case "2" -> QuestionLevel.MEDIUM;
            case "3" -> QuestionLevel.HARD;
            case "4" -> QuestionLevel.EXPERT;
            default  -> throw new IllegalArgumentException("Unknown question difficulty: " + raw);
        };
    }

    private int parseCorrectIndex(String raw) {
        raw = raw.trim().toUpperCase();

        if (raw.length() == 1 && raw.charAt(0) >= 'A' && raw.charAt(0) <= 'D') {
            return raw.charAt(0) - 'A';
        }

        if (raw.matches("\\d+")) {
            int idx = Integer.parseInt(raw);
            if (idx >= 1 && idx <= 4) return idx - 1;
            throw new IllegalArgumentException("Correct index out of range: " + raw);
        }

        throw new IllegalArgumentException("Unknown correct answer format: " + raw);
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
}
