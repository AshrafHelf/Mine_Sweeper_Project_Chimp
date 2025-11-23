package model;


import model.HistoryRepository;

import model.GameRecord;

import java.util.List;


public class HistoryService {
private final HistoryRepository repo;
public HistoryService(HistoryRepository repo){ this.repo = repo; }
public void save(GameRecord rec){ repo.append(rec); }
public List<GameRecord> all(){ return repo.findAll(); }
}