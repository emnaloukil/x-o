package com.example.x_o;
import java.io.Serializable;

public class TournamentResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private int scoreX;
    private int scoreO;
    private int draws;
    private int totalGames;
    private String winner;

    public TournamentResult(int scoreX, int scoreO, int draws, int totalGames, String winner) {
        this.scoreX = scoreX;
        this.scoreO = scoreO;
        this.draws = draws;
        this.totalGames = totalGames;
        this.winner = winner;
    }

    public int getScoreX()     { return scoreX; }
    public int getScoreO()     { return scoreO; }
    public int getDraws()      { return draws; }
    public int getTotalGames() { return totalGames; }
    public String getWinner()  { return winner; }
}