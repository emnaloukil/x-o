package com.example.x_o;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private RadioGroup rgSymbol;
    private Spinner spinnerRounds;
    private Button btnPlay;
    private Button btnRules;
    private Button btnLastScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Lier les vues
        rgSymbol     = findViewById(R.id.rg_symbol);
        spinnerRounds = findViewById(R.id.spinner_rounds);
        btnPlay      = findViewById(R.id.btn_play);
        btnRules     = findViewById(R.id.btn_rules);
        btnLastScore = findViewById(R.id.btn_last_score);

        // Configurer le Spinner (5, 10, 15 parties)
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new Integer[]{5, 10, 15}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRounds.setAdapter(adapter);

        // Bouton Jouer
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lancerJeu();
            }
        });

        // Bouton Règles
        btnRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, RulesActivity.class);
                startActivity(intent);
            }
        });

        // Bouton Retrouver les scores
        btnLastScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                afficherDernierScore();
            }
        });
    }

    private void lancerJeu() {
        // Lire le symbole choisi
        int selectedRadioId = rgSymbol.getCheckedRadioButtonId();
        String selectedSymbol;

        if (selectedRadioId == R.id.rb_symbol_x) {
            selectedSymbol = "X";
        } else if (selectedRadioId == R.id.rb_symbol_o) {
            selectedSymbol = "O";
        } else {
            // Aucune sélection : par défaut X
            selectedSymbol = "X";
        }

        // Lire le nombre de parties depuis le Spinner
        int totalGames = (int) spinnerRounds.getSelectedItem();

        // Lancer GameActivity
        Intent intent = new Intent(HomeActivity.this, GameActivity.class);
        intent.putExtra("selectedSymbol", selectedSymbol);
        intent.putExtra("totalGames", totalGames);
        startActivity(intent);
    }

    private void afficherDernierScore() {
        TournamentResult result = SavedScoreHelper.loadResult(this);

        if (result == null) {
            Toast.makeText(this, "Aucun tournoi sauvegardé", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construire le message
        String message =
                "Score X : " + result.getScoreX() + "\n" +
                        "Score O : " + result.getScoreO() + "\n" +
                        "Parties nulles : " + result.getDraws() + "\n" +
                        "Total parties : " + result.getTotalGames() + "\n" +
                        "Vainqueur : " + result.getWinner();

        new AlertDialog.Builder(this)
                .setTitle("Dernier tournoi sauvegardé")
                .setMessage(message)
                .setPositiveButton("Fermer", null)
                .show();
    }
}