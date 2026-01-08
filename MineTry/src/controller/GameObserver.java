package controller;

import model.Game;

@FunctionalInterface
public interface GameObserver {
    void onGameChanged(Game game);
}
