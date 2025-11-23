package model;


import model.QuestionRepository;
import model.SysData;

import model.Question;
import model.QuestionLevel;

import java.util.List;
import java.util.Optional;


public class QuestionService {
private final QuestionRepository repo;
private final SysData sysData;
public QuestionService(QuestionRepository repo, SysData sysData){ this.repo=repo; this.sysData=sysData; }
public List<Question> getAll(){ return repo.findAll(); }
public List<Question> byLevel(QuestionLevel lvl){ return repo.findByLevel(lvl); }
public Optional<Question> random(QuestionLevel lvl){ return repo.randomByLevel(lvl); }
public void reload(){ sysData.setQuestions(repo.findAll()); }
}