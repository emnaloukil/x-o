package com.example.x_o;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO = 101;
    private static final int REQUEST_SPEECH = 200;

    private TextView tvListening;

    private String[] board = new String[9];

    private int[] cellIds = {
            R.id.cell_1, R.id.cell_2, R.id.cell_3,
            R.id.cell_4, R.id.cell_5, R.id.cell_6,
            R.id.cell_7, R.id.cell_8, R.id.cell_9
    };

    private Button[] cells = new Button[9];

    private String playerSymbol;
    private String currentTurn;
    private int totalGames;
    private int currentGame;
    private int scoreX;
    private int scoreO;
    private int draws;

    private TextView tvRoundLabel;
    private TextView tvScoreX;
    private TextView tvScoreO;
    private TextView tvScoreDraw;
    private TextView tvCurrentTurn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        playerSymbol = getIntent().getStringExtra("selectedSymbol");
        totalGames = getIntent().getIntExtra("totalGames", 5);

        if (playerSymbol == null || (!playerSymbol.equals("X") && !playerSymbol.equals("O"))) {
            playerSymbol = "X";
        }

        currentGame = 1;
        scoreX = 0;
        scoreO = 0;
        draws = 0;
        currentTurn = playerSymbol;

        tvRoundLabel = findViewById(R.id.tv_round_label);
        tvScoreX = findViewById(R.id.tv_score_x_value);
        tvScoreO = findViewById(R.id.tv_score_o_value);
        tvScoreDraw = findViewById(R.id.tv_score_draw_value);
        tvCurrentTurn = findViewById(R.id.tv_current_turn);

        tvListening = findViewById(R.id.tv_listening);
        tvListening.setVisibility(View.GONE);

        for (int i = 0; i < 9; i++) {
            cells[i] = findViewById(cellIds[i]);
            final int index = i;
            cells[i].setOnClickListener(v -> playMove(index));
        }

        Button btnMic = findViewById(R.id.btn_mic);
        btnMic.setOnClickListener(v -> checkAudioPermission());

        Button btnQuit = findViewById(R.id.btn_quit);
        btnQuit.setOnClickListener(v -> {
            Intent intent = new Intent(GameActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        demarrerNouvellePartie();
    }

    private void demarrerNouvellePartie() {
        for (int i = 0; i < 9; i++) {
            board[i] = "";
            cells[i].setText("");
            cells[i].setEnabled(true);
        }

        currentTurn = playerSymbol;
        mettreAJourAffichage();
    }

    private void mettreAJourAffichage() {
        tvRoundLabel.setText("Partie " + currentGame + " / " + totalGames);
        tvScoreX.setText(String.valueOf(scoreX));
        tvScoreO.setText(String.valueOf(scoreO));
        tvScoreDraw.setText(String.valueOf(draws));
        tvCurrentTurn.setText("Tour : " + currentTurn);
    }

    private void playMove(int cellIndex) {
        if (cellIndex < 0 || cellIndex > 8) {
            Toast.makeText(this, "Case invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!board[cellIndex].isEmpty()) {
            Toast.makeText(this, "Cette case est déjà occupée", Toast.LENGTH_SHORT).show();
            return;
        }

        board[cellIndex] = currentTurn;
        cells[cellIndex].setText(currentTurn);
        cells[cellIndex].setEnabled(false);

        String gagnant = verifierGagnant();

        if (gagnant != null) {
            if (gagnant.equals("X")) {
                scoreX++;
            } else {
                scoreO++;
            }

            mettreAJourAffichage();
            afficherFinDePartie("Le joueur " + gagnant + " remporte cette partie !");

        } else if (estNul()) {
            draws++;

            mettreAJourAffichage();
            afficherFinDePartie("Partie nulle !");

        } else {
            currentTurn = currentTurn.equals("X") ? "O" : "X";
            mettreAJourAffichage();
        }
    }

    private String verifierGagnant() {
        int[][] combinaisons = {
                {0, 1, 2},
                {3, 4, 5},
                {6, 7, 8},
                {0, 3, 6},
                {1, 4, 7},
                {2, 5, 8},
                {0, 4, 8},
                {2, 4, 6}
        };

        for (int[] combo : combinaisons) {
            String a = board[combo[0]];
            String b = board[combo[1]];
            String c = board[combo[2]];

            if (!a.isEmpty() && a.equals(b) && b.equals(c)) {
                return a;
            }
        }

        return null;
    }

    private boolean estNul() {
        for (String value : board) {
            if (value.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void afficherFinDePartie(String message) {
        for (Button cell : cells) {
            cell.setEnabled(false);
        }

        new AlertDialog.Builder(this)
                .setTitle("Fin de partie " + currentGame)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Continuer", (dialog, which) -> {
                    currentGame++;

                    if (currentGame > totalGames) {
                        lancerResultActivity();
                    } else {
                        demarrerNouvellePartie();
                    }
                })
                .show();
    }

    private void lancerResultActivity() {
        String winner;

        if (scoreX > scoreO) {
            winner = "Joueur X";
        } else if (scoreO > scoreX) {
            winner = "Joueur O";
        } else {
            winner = "Égalité";
        }

        Intent intent = new Intent(GameActivity.this, ResultActivity.class);
        intent.putExtra("scoreX", scoreX);
        intent.putExtra("scoreO", scoreO);
        intent.putExtra("draws", draws);
        intent.putExtra("totalGames", totalGames);
        intent.putExtra("winner", winner);

        startActivity(intent);
        finish();
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO
            );

        } else {
            startSpeechIntent();
        }
    }

    private void startSpeechIntent() {
        tvListening.setVisibility(View.VISIBLE);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fr-FR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dites une case : un, deux, trois...");

        try {
            startActivityForResult(intent, REQUEST_SPEECH);
        } catch (Exception e) {
            tvListening.setVisibility(View.GONE);
            Toast.makeText(this,
                    "Reconnaissance vocale non disponible sur cet émulateur",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        tvListening.setVisibility(View.GONE);

        if (requestCode == REQUEST_SPEECH) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> results =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                if (results == null || results.isEmpty()) {
                    Toast.makeText(this,
                            "Aucune commande détectée",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String text = results.toString();

                Toast.makeText(this,
                        "Vous avez dit : " + text,
                        Toast.LENGTH_LONG).show();

                int cellNumber = extractCellNumber(text);

                if (cellNumber == -1) {
                    Toast.makeText(this,
                            "Commande non reconnue. Dites par exemple : deux",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,
                            "Commande reconnue : case " + cellNumber,
                            Toast.LENGTH_SHORT).show();

                    playMove(cellNumber - 1);
                }

            } else {
                Toast.makeText(this,
                        "Aucune commande détectée",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int extractCellNumber(String text) {
        if (text == null) {
            return -1;
        }

        text = text.toLowerCase(Locale.FRANCE).trim();

        text = text.replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("à", "a")
                .replace("ù", "u");

        if (text.contains("1")) return 1;
        if (text.contains("2")) return 2;
        if (text.contains("3")) return 3;
        if (text.contains("4")) return 4;
        if (text.contains("5")) return 5;
        if (text.contains("6")) return 6;
        if (text.contains("7")) return 7;
        if (text.contains("8")) return 8;
        if (text.contains("9")) return 9;

        if (text.contains("un") || text.contains("une")) return 1;

        if (text.contains("deux")
                || text.contains("numero deux")
                || text.contains("numero de")
                || text.equals("de")
                || text.contains(" de ")) {
            return 2;
        }

        if (text.contains("trois")) return 3;
        if (text.contains("quatre")) return 4;

        if (text.contains("cinq")
                || text.contains("cinque")
                || text.contains("saint")
                || text.contains("sein")
                || text.contains("sank")) {
            return 5;
        }

        if (text.contains("six")) return 6;
        if (text.contains("sept") || text.contains("set")) return 7;
        if (text.contains("huit")) return 8;
        if (text.contains("neuf") || text.contains("neuve")) return 9;

        return -1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startSpeechIntent();

            } else {
                Toast.makeText(this,
                        "Permission microphone refusée",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}