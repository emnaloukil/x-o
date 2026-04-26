package com.example.x_o;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OnlineGameActivity extends AppCompatActivity {

    private DatabaseReference gameRef;
    private String roomCode, mySymbol, currentTurn;

    private String[] board = new String[9];
    private Button[] cells = new Button[9];

    private int scoreX = 0;
    private int scoreO = 0;
    private int draws  = 0;
    private int currentGame = 1;
    private int totalGames  = 5;

    private boolean gameOver    = false;
    private boolean dialogShown = false;

    private TextView tvCurrentTurn, tvTourJoueur, tvRoomCode,
            tvScoreX, tvScoreO, tvScoreDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game);

        roomCode   = getIntent().getStringExtra("roomCode");
        mySymbol   = getIntent().getStringExtra("mySymbol");
        totalGames = getIntent().getIntExtra("totalGames", 5);

        // Lier les vues
        tvCurrentTurn = findViewById(R.id.tvCurrentTurn);
        tvTourJoueur  = findViewById(R.id.tvTourJoueur);
        tvRoomCode    = findViewById(R.id.tvRoomCode);
        tvScoreX      = findViewById(R.id.tvScoreX);
        tvScoreO      = findViewById(R.id.tvScoreO);
        tvScoreDraw   = findViewById(R.id.tvScoreDraw);

        tvRoomCode.setText("Room : " + roomCode);
        tvCurrentTurn.setText("Partie 1 / " + totalGames);
        updateScores();

        // Bouton quitter
        Button btnQuit = findViewById(R.id.btn_quit_online);
        btnQuit.setOnClickListener(v -> {
            gameRef.removeValue();
            Intent intent = new Intent(OnlineGameActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Lier les 9 boutons
        cells[0] = findViewById(R.id.cell0); cells[1] = findViewById(R.id.cell1);
        cells[2] = findViewById(R.id.cell2); cells[3] = findViewById(R.id.cell3);
        cells[4] = findViewById(R.id.cell4); cells[5] = findViewById(R.id.cell5);
        cells[6] = findViewById(R.id.cell6); cells[7] = findViewById(R.id.cell7);
        cells[8] = findViewById(R.id.cell8);

        for (int i = 0; i < 9; i++) {
            final int index = i;
            cells[i].setOnClickListener(v -> onCellClicked(index));
        }

        gameRef = FirebaseDatabase.getInstance().getReference("games/" + roomCode);

        if (mySymbol.equals("X")) {
            reinitialiserPartieFirebase();
        }

        listenToFirebase();
    }

    private void reinitialiserPartieFirebase() {
        Map<String, Object> newBoard = new HashMap<>();
        for (int i = 0; i < 9; i++) {
            newBoard.put(String.valueOf(i), "");
        }
        gameRef.child("board").setValue(newBoard);
        gameRef.child("currentTurn").setValue("X");
        gameRef.child("status").setValue("playing");
        gameRef.child("gameOver").setValue(false);
    }

    private void onCellClicked(int index) {
        if (gameOver) return;
        if (!currentTurn.equals(mySymbol)) {
            Toast.makeText(this, "Ce n'est pas votre tour !", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!board[index].isEmpty()) {
            Toast.makeText(this, "Case déjà prise !", Toast.LENGTH_SHORT).show();
            return;
        }
        gameRef.child("board").child(String.valueOf(index)).setValue(mySymbol);
        gameRef.child("currentTurn").setValue(mySymbol.equals("X") ? "O" : "X");
    }

    private void listenToFirebase() {
        gameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (dialogShown) return;

                for (int i = 0; i < 9; i++) {
                    String val = snapshot.child("board")
                            .child(String.valueOf(i))
                            .getValue(String.class);
                    board[i] = (val != null) ? val : "";
                }

                currentTurn = snapshot.child("currentTurn").getValue(String.class);
                if (currentTurn == null) currentTurn = "X";

                updateGrid();

                // Mettre à jour numéro de partie
                tvCurrentTurn.setText("Partie " + currentGame + " / " + totalGames);

                // Mettre à jour indicateur de tour
                if (currentTurn.equals(mySymbol)) {
                    tvTourJoueur.setText("Votre tour (" + mySymbol + ")");
                    tvTourJoueur.setTextColor(
                            mySymbol.equals("X")
                                    ? getResources().getColor(R.color.color_player_x, null)
                                    : getResources().getColor(R.color.color_player_o, null)
                    );
                } else {
                    tvTourJoueur.setText("Adversaire...");
                    tvTourJoueur.setTextColor(
                            getResources().getColor(R.color.color_text_secondary, null)
                    );
                }

                String winner = checkWinner();
                if (winner != null && !gameOver) {
                    gameOver    = true;
                    dialogShown = true;

                    if (winner.equals("X"))      scoreX++;
                    else if (winner.equals("O")) scoreO++;
                    else                         draws++;

                    updateScores();

                    tvCurrentTurn.postDelayed(
                            () -> afficherFinDePartie(winner), 1000);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void updateGrid() {
        for (int i = 0; i < 9; i++) {
            cells[i].setText(board[i]);
            cells[i].setEnabled(board[i].isEmpty() && !gameOver);
        }
    }

    private void updateScores() {
        tvScoreX.setText(String.valueOf(scoreX));
        tvScoreO.setText(String.valueOf(scoreO));
        tvScoreDraw.setText(String.valueOf(draws));
    }

    private String checkWinner() {
        int[][] wins = {
                {0,1,2}, {3,4,5}, {6,7,8},
                {0,3,6}, {1,4,7}, {2,5,8},
                {0,4,8}, {2,4,6}
        };
        for (int[] combo : wins) {
            String a = board[combo[0]];
            String b = board[combo[1]];
            String c = board[combo[2]];
            if (!a.isEmpty() && a.equals(b) && b.equals(c)) return a;
        }
        for (String cell : board) {
            if (cell.isEmpty()) return null;
        }
        return "Nul";
    }

    private void afficherFinDePartie(String winner) {
        String message = winner.equals("Nul")
                ? "Match nul !"
                : "Le joueur " + winner + " remporte cette partie !";

        if (currentGame >= totalGames) {
            new AlertDialog.Builder(this)
                    .setTitle("Partie " + currentGame + "/" + totalGames)
                    .setMessage(message + "\n\nFin du tournoi !")
                    .setCancelable(false)
                    .setPositiveButton("Voir le résultat final",
                            (d, w) -> afficherResultatFinal())
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Partie " + currentGame + "/" + totalGames)
                    .setMessage(message + "\n\nScore : X=" + scoreX + "  O=" + scoreO)
                    .setCancelable(false)
                    .setPositiveButton("Partie suivante", (d, w) -> {
                        currentGame++;
                        gameOver    = false;
                        dialogShown = false;
                        Arrays.fill(board, "");
                        updateGrid();
                        updateScores();
                        if (mySymbol.equals("X")) {
                            reinitialiserPartieFirebase();
                        }
                    })
                    .show();
        }
    }

    private void afficherResultatFinal() {
        String vainqueur;
        if (scoreX > scoreO)      vainqueur = "Joueur X gagne le tournoi ! 🏆";
        else if (scoreO > scoreX) vainqueur = "Joueur O gagne le tournoi ! 🏆";
        else                      vainqueur = "Égalité ! 🤝";

        String message = "Score final :\n"
                + "X : "    + scoreX + "\n"
                + "O : "    + scoreO + "\n"
                + "Nuls : " + draws  + "\n\n"
                + vainqueur;

        new AlertDialog.Builder(this)
                .setTitle("Fin du tournoi")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Retour à l'accueil", (d, w) -> {
                    gameRef.removeValue();
                    Intent intent = new Intent(
                            OnlineGameActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}