package model;


import java.util.List;
import java.util.Optional;

import enums.QuestionLevel;


public class QuestionService {
private final QuestionRepository repo;
private final SysData sysData;
public QuestionService(QuestionRepository repo, SysData sysData){ this.repo=repo; this.sysData=sysData; }
public List<Question> getAll(){ return repo.findAll(); }
public List<Question> byLevel(QuestionLevel lvl){ return repo.findByLevel(lvl); }
public Optional<Question> random(QuestionLevel lvl){ return repo.randomByLevel(lvl); }
public void reload(){ sysData.setQuestions(repo.findAll()); }
}