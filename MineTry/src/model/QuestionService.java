package model;

import java.util.List;
import java.util.Optional;

public class QuestionService {
    private final QuestionRepository repo;
    private final SysData sysData;

    public QuestionService(QuestionRepository repo, SysData sysData){
        this.repo = repo;
        this.sysData = sysData;
        reload();
    }

    public List<Question> getAll(){ return repo.findAll(); }
    public List<Question> byLevel(QuestionLevel lvl){ return repo.findByLevel(lvl); }
    public Optional<Question> random(QuestionLevel lvl){ return repo.randomByLevel(lvl); }

    public void addQuestion(Question q){
        repo.add(q);
        repo.save();
        reload();
    }

    public boolean updateQuestion(Question q){
        boolean ok = repo.update(q);
        if (ok) {
            repo.save();
            reload();
        }
        return ok;
    }

    public boolean deleteById(String id){
        boolean ok = repo.deleteById(id);
        if (ok) {
            repo.save();
            reload();
        }
        return ok;
    }

    public void reload(){
        repo.reload();
        sysData.setQuestions(repo.findAll());
    }
}
