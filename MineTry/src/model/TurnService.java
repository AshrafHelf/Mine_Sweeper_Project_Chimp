package model;

import model.Game;

public class TurnService {

    public void next(Game game) {
        game.swapTurn();
    }
}
