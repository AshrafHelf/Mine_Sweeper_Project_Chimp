package model;

import enums.QuestionLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuestionService {
    private final QuestionRepository repo;
    private final SysData sysData;

    public QuestionService(QuestionRepository repo, SysData sysData) {
        this.repo = repo;
        this.sysData = sysData;
        reload(); // load into SysData at start
    }

    public List<Question> getAll() { return new ArrayList<>(sysData.getQuestions()); }

    public List<Question> byLevel(QuestionLevel lvl) {
        // can use SysData or repo; SysData is fine
        List<Question> res = new ArrayList<>();
        for (Question q : sysData.getQuestions()) {
            if (q.getLevel() == lvl) res.add(q);
        }
        return res;
    }

    public Optional<Question> random(QuestionLevel lvl) {
        return repo.randomByLevel(lvl);
    }

    public void reload() {
        sysData.setQuestions(repo.findAll());
    }

    // ---------------- CRUD (persist) ----------------

    public void add(Question q) {
        List<Question> list = new ArrayList<>(sysData.getQuestions());
        list.add(q);
        save(list);
    }

    public void update(Question updated) {
        List<Question> list = new ArrayList<>(sysData.getQuestions());
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updated.getId())) {
                list.set(i, updated);
                break;
            }
        }
        save(list);
    }

    public void deleteById(String id) {
        List<Question> list = new ArrayList<>(sysData.getQuestions());
        list.removeIf(q -> q.getId().equals(id));
        save(list);
    }

    private void save(List<Question> list) {
        sysData.setQuestions(list);

        if (repo instanceof CsvQuestionRepository csv) {
            csv.saveAll(list); // ✅ writes to data/Questions.csv
        }
    }

    // ---------------- ID generator ----------------
    public String nextId() {
        int max = 0;
        for (Question q : sysData.getQuestions()) {
            try {
                max = Math.max(max, Integer.parseInt(q.getId().trim()));
            } catch (Exception ignored) {}
        }
        return String.valueOf(max + 1);
    }
}
