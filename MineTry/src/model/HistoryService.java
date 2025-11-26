package model;

import java.time.LocalDateTime;
import java.util.List;

public class HistoryService {

    private final HistoryRepository repo;

    public HistoryService(HistoryRepository repo) {
        this.repo = repo;
    }

    public List<GameRecord> all() {
        return repo.loadAll();
    }

    public void recordGame(Game game) {
        GameRecord rec = new GameRecord(
                LocalDateTime.now(),
                game.getDifficulty(),
                game.getPlayer1().getName(),
                game.getPlayer2().getName(),
                game.getTeamScore(),
                game.isWon()
        );
        repo.append(rec);
    }
}
