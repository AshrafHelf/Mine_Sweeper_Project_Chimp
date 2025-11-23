package model;


import java.util.List;

import model.GameRecord;


public interface HistoryRepository {
void append(GameRecord record);
List<GameRecord> findAll();
}