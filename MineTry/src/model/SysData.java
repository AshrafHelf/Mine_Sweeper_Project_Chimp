package model;


import java.util.ArrayList;
import java.util.List;


public class SysData {
private static final SysData INSTANCE = new SysData();
public static SysData getInstance(){ return INSTANCE; }
private SysData(){}


private List<Question> questions = new ArrayList<>();
private List<GameRecord> history = new ArrayList<>();


public List<Question> getQuestions(){ return questions; }
public void setQuestions(List<Question> qs){ this.questions = new ArrayList<>(qs); }
public List<GameRecord> getHistory(){ return history; }
public void setHistory(List<GameRecord> history){ this.history = new ArrayList<>(history); }
}