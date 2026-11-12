package com.pedalgaiden.application.port;

public interface AudioOutput {
    void menu();
    void countdown();
    void race(boolean paused);
    void resetRace();
    void click();
    void pickup();
    void recycled();
    void pause();
}
