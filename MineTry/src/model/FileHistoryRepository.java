package model;


import java.util.ArrayList;
import java.util.List;

import model.GameRecord;


public class FileHistoryRepository implements HistoryRepository {
private final String path;
public FileHistoryRepository(String path){ this.path = path; }
@Override public void append(GameRecord record) { /* TODO: write CSV */ }
@Override public List<GameRecord> findAll() { return new ArrayList<>(); }
}