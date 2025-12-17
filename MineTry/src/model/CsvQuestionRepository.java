package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import enums.QuestionLevel;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;



public class CsvQuestionRepository implements QuestionRepository {

    private final List<Question> cache = new ArrayList<>();
    private final Random rnd = new Random();

    public CsvQuestionRepository(Path csvPath) {
        loadAll();
    }

    // Convenience constructor
    public CsvQuestionRepository(String csvPath) {
        this(Path.of(csvPath));
    }

    // =======================
    // QuestionRepository impl
    // =======================

    @Override
    public List<Question> findAll() {
        return new ArrayList<>(cache);
    }

    @Override
    public List<Question> findByLevel(QuestionLevel level) {
        List<Question> result = new ArrayList<>();
        for (Question q : cache) {
            if (q.getLevel() == level) {
                result.add(q);
            }
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
    // CSV loading
    // =======================

    private void loadAll() {
        cache.clear();

        try (InputStream in =
                     CsvQuestionRepository.class.getResourceAsStream("/Questions.csv")) {

            if (in == null) {
                throw new IllegalStateException("Questions.csv not found inside JAR");
            }

            try (BufferedReader br =
                         new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

                String line;
                boolean first = true;

                while ((line = br.readLine()) != null) {
                    // skip header
                    if (first) {
                        first = false;
                        continue;
                    }
                    if (line.isBlank()) continue;

                    Question q = parseLine(line);
                    if (q != null) {
                        cache.add(q);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Expected format:
     * ID,Question,Difficulty,A,B,C,D,Correct Answer
     *
     * Handles quoted fields with commas, e.g.:
     * 2,"In Minesweeper, what does ...",1,...
     */
    private Question parseLine(String line) {
        List<String> cols = splitCsvLine(line);

        if (cols.size() < 8) {
            System.err.println("Bad CSV row (expected 8+ columns): " + line);
            return null;
        }

        String id           = cols.get(0).trim();
        String text         = cols.get(1).trim();
        String difficultyRaw= cols.get(2).trim();
        String optA         = cols.get(3).trim();
        String optB         = cols.get(4).trim();
        String optC         = cols.get(5).trim();
        String optD         = cols.get(6).trim();
        String correctRaw   = cols.get(7).trim();

        QuestionLevel level = parseLevel(difficultyRaw);
        int correctIndex    = parseCorrectIndex(correctRaw);

        List<String> options = new ArrayList<>();
        options.add(optA);
        options.add(optB);
        options.add(optC);
        options.add(optD);

        return new Question(id, text, options, correctIndex, level);
    }

    /**
     * Difficulty is numeric 1..4 → QuestionLevel.
     */
    private QuestionLevel parseLevel(String raw) {
        return switch (raw) {
            case "1" -> QuestionLevel.EASY;
            case "2" -> QuestionLevel.MEDIUM;
            case "3" -> QuestionLevel.HARD;
            case "4" -> QuestionLevel.EXPERT;
            default  -> throw new IllegalArgumentException("Unknown question difficulty: " + raw);
        };
    }

    /**
     * Maps "A"/"B"/"C"/"D" or 1..4 → 0..3.
     */
    private int parseCorrectIndex(String raw) {
        raw = raw.trim().toUpperCase();

        // Case 1: letter A-D
        if (raw.length() == 1 && raw.charAt(0) >= 'A' && raw.charAt(0) <= 'D') {
            return raw.charAt(0) - 'A';
        }

        // Case 2: numeric "1".."4"
        if (raw.matches("\\d+")) {
            int idx = Integer.parseInt(raw);
            if (idx >= 1 && idx <= 4) {
                return idx - 1;
            }
            throw new IllegalArgumentException("Correct index out of range: " + raw);
        }

        throw new IllegalArgumentException("Unknown correct answer format: " + raw);
    }

    /**
     * Splits a CSV line into columns, handling quotes like Excel:
     * - Fields may be wrapped in "..."
     * - Commas inside quotes do NOT split.
     * - Double quotes inside a field are written as "".
     */
    private List<String> splitCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                // check for escaped quote ""
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++; // skip second quote
                } else {
                    inQuotes = !inQuotes; // open/close quotes
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
