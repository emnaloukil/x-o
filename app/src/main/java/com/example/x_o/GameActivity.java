package com.example.x_o;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    // --- Grille de jeu ---
    // board[i] = "" (vide), "X" ou "O"
    private String[] board = new String[9];

    // --- IDs des cellules dans l'ordre ---
    private int[] cellIds = {
            R.id.cell_1, R.id.cell_2, R.id.cell_3,
            R.id.cell_4, R.id.cell_5, R.id.cell_6,
            R.id.cell_7, R.id.cell_8, R.id.cell_9
    };
    private Button[] cells = new Button[9];

    // --- Données du tournoi ---
    private String playerSymbol;   // Symbole choisi par le joueur 1 (X ou O)
    private String currentTurn;    // Symbole du joueur dont c'est le tour
    private int totalGames;        // Nombre total de parties
    private int currentGame;       // Partie actuelle (commence à 1)
    private int scoreX;
    private int scoreO;
    private int draws;

    // --- Vues ---
    private TextView tvRoundLabel;
    private TextView tvScoreX;
    private TextView tvScoreO;
    private TextView tvScoreDraw;
    private TextView tvCurrentTurn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Récupérer les extras de HomeActivity
        playerSymbol = getIntent().getStringExtra("selectedSymbol");
        totalGames   = getIntent().getIntExtra("totalGames", 5);

        // Initialisation
        currentGame  = 1;
        scoreX       = 0;
        scoreO       = 0;
        draws        = 0;
        currentTurn  = "X"; // X commence toujours

        // Lier les vues textuelles
        tvRoundLabel  = findViewById(R.id.tv_round_label);
        tvScoreX      = findViewById(R.id.tv_score_x_value);
        tvScoreO      = findViewById(R.id.tv_score_o_value);
        tvScoreDraw   = findViewById(R.id.tv_score_draw_value);
        tvCurrentTurn = findViewById(R.id.tv_current_turn);

        // Lier les cellules et assigner les listeners
        for (int i = 0; i < 9; i++) {
            cells[i] = findViewById(cellIds[i]);
            final int index = i;
            cells[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playMove(index);
                }
            });
        }

        // Bouton micro
        Button btnMic = findViewById(R.id.btn_mic);
        btnMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GameActivity.this,
                        "Commande vocale sera ajoutée plus tard",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Bouton quitter
        Button btnQuit = findViewById(R.id.btn_quit);
        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retour à l'accueil
                Intent intent = new Intent(GameActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Préparer la première partie
        demarrerNouvellePartie();
    }

    // -------------------------------------------------------
    // Initialiser la grille pour une nouvelle partie
    // -------------------------------------------------------
    private void demarrerNouvellePartie() {
        // Vider le tableau logique
        for (int i = 0; i < 9; i++) {
            board[i] = "";
        }

        // Réinitialiser les boutons
        for (Button cell : cells) {
            cell.setText("");
            cell.setEnabled(true);
        }

        currentTurn = "X"; // X commence toujours

        mettreAJourAffichage();
    }

    // -------------------------------------------------------
    // Mettre à jour les TextViews de score et de tour
    // -------------------------------------------------------
    private void mettreAJourAffichage() {
        tvRoundLabel.setText("Partie " + currentGame + " / " + totalGames);
        tvScoreX.setText(String.valueOf(scoreX));
        tvScoreO.setText(String.valueOf(scoreO));
        tvScoreDraw.setText(String.valueOf(draws));
        tvCurrentTurn.setText("Tour : " + currentTurn);
    }

    // -------------------------------------------------------
    // Jouer un coup à l'index cellIndex (0-8)
    // -------------------------------------------------------
    private void playMove(int cellIndex) {
        // Vérifier que la case est vide
        if (!board[cellIndex].isEmpty()) {
            return;
        }

        // Placer le symbole
        board[cellIndex] = currentTurn;
        cells[cellIndex].setText(currentTurn);
        cells[cellIndex].setEnabled(false);

        // Vérifier s'il y a un gagnant
        String gagnant = verifierGagnant();

        if (gagnant != null) {
            // Un joueur a gagné cette partie
            if (gagnant.equals("X")) {
                scoreX++;
            } else {
                scoreO++;
            }
            mettreAJourAffichage();
            afficherFinDePartie("Le joueur " + gagnant + " remporte cette partie !");

        } else if (estNul()) {
            // Partie nulle
            draws++;
            mettreAJourAffichage();
            afficherFinDePartie("Partie nulle !");

        } else {
            // Continuer : changer de tour
            currentTurn = currentTurn.equals("X") ? "O" : "X";
            mettreAJourAffichage();
        }
    }

    // -------------------------------------------------------
    // Vérifier s'il y a un gagnant
    // Retourne "X", "O" ou null
    // -------------------------------------------------------
    private String verifierGagnant() {
        // Combinaisons gagnantes (indices dans board[])
        int[][] combinaisons = {
                {0, 1, 2}, // ligne 1
                {3, 4, 5}, // ligne 2
                {6, 7, 8}, // ligne 3
                {0, 3, 6}, // colonne 1
                {1, 4, 7}, // colonne 2
                {2, 5, 8}, // colonne 3
                {0, 4, 8}, // diagonale
                {2, 4, 6}  // anti-diagonale
        };

        for (int[] combo : combinaisons) {
            String a = board[combo[0]];
            String b = board[combo[1]];
            String c = board[combo[2]];

            if (!a.isEmpty() && a.equals(b) && b.equals(c)) {
                return a; // "X" ou "O"
            }
        }
        return null; // Pas de gagnant
    }

    // -------------------------------------------------------
    // Vérifier si la partie est nulle (toutes cases remplies)
    // -------------------------------------------------------
    private boolean estNul() {
        for (String case_ : board) {
            if (case_.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // -------------------------------------------------------
    // Afficher un dialog de fin de partie
    // -------------------------------------------------------
    private void afficherFinDePartie(String message) {
        // Désactiver toutes les cellules
        for (Button cell : cells) {
            cell.setEnabled(false);
        }

        new AlertDialog.Builder(this)
                .setTitle("Fin de partie " + currentGame)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Partie suivante", (dialog, which) -> {
                    currentGame++;

                    if (currentGame > totalGames) {
                        // Tournoi terminé
                        lancerResultActivity();
                    } else {
                        // Prochaine partie
                        demarrerNouvellePartie();
                    }
                })
                .show();
    }

    // -------------------------------------------------------
    // Lancer ResultActivity avec les scores finaux
    // -------------------------------------------------------
    private void lancerResultActivity() {
        // Calculer le vainqueur du tournoi
        String winner;
        if (scoreX > scoreO) {
            winner = "Joueur X";
        } else if (scoreO > scoreX) {
            winner = "Joueur O";
        } else {
            winner = "Égalité";
        }

        Intent intent = new Intent(GameActivity.this, ResultActivity.class);
        intent.putExtra("scoreX",     scoreX);
        intent.putExtra("scoreO",     scoreO);
        intent.putExtra("draws",      draws);
        intent.putExtra("totalGames", totalGames);
        intent.putExtra("winner",     winner);
        startActivity(intent);
        finish();
    }
}