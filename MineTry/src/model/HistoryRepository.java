package model;

import java.util.List;

public interface HistoryRepository {
    List<GameRecord> loadAll();
    void append(GameRecord record);
}
