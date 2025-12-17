package model;


import java.util.List;

import enums.QuestionLevel;


public class Question {
private final String id;
private final String text;
private final List<String> options; // size 4
private final int correctIndex; // 0..3
private final QuestionLevel level;


public Question(String id, String text, List<String> options, int correctIndex, QuestionLevel level) {
this.id = id; this.text = text; this.options = options; this.correctIndex = correctIndex; this.level = level;
}
public String getId(){ return id; }
public String getText(){ return text; }
public List<String> getOptions(){ return options; }
public int getCorrectIndex(){ return correctIndex; }
public QuestionLevel getLevel(){ return level; }
}