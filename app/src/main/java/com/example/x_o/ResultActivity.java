package com.example.x_o;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private int scoreX;
    private int scoreO;
    private int draws;
    private int totalGames;
    private String winner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Récupérer les données depuis GameActivity
        scoreX     = getIntent().getIntExtra("scoreX",     0);
        scoreO     = getIntent().getIntExtra("scoreO",     0);
        draws      = getIntent().getIntExtra("draws",      0);
        totalGames = getIntent().getIntExtra("totalGames", 5);
        winner     = getIntent().getStringExtra("winner");

        // Lier les vues
        TextView tvFinalScoreX = findViewById(R.id.tv_result_score_x);
        TextView tvFinalScoreO = findViewById(R.id.tv_result_score_o);
        TextView tvFinalDraws  = findViewById(R.id.tv_result_score_draw);
        TextView tvFinalTotal  = findViewById(R.id.tv_final_total);
        TextView tvWinner      = findViewById(R.id.tv_result_winner);
        Button btnSave         = findViewById(R.id.btn_save_result);
        Button btnHome         = findViewById(R.id.btn_result_home);

        // Afficher les résultats
        tvFinalScoreX.setText(String.valueOf(scoreX));
        tvFinalScoreO.setText(String.valueOf(scoreO));
        tvFinalDraws.setText(String.valueOf(draws));
        tvFinalTotal.setText(String.valueOf(totalGames));
        tvWinner.setText(winner);

        // Bouton Sauvegarder
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sauvegarderTournoi();
            }
        });

        // Bouton Revenir à l'accueil
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void sauvegarderTournoi() {
        TournamentResult result = new TournamentResult(
                scoreX, scoreO, draws, totalGames, winner
        );
        SavedScoreHelper.saveResult(this, result);
        Toast.makeText(this, "Tournoi sauvegardé avec succès !", Toast.LENGTH_SHORT).show();
    }
}