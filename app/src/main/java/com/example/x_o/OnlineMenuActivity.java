package com.example.x_o;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OnlineMenuActivity extends AppCompatActivity {

    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_menu);

        db = FirebaseDatabase.getInstance().getReference();

        Button btnCreate = findViewById(R.id.btnCreate);
        Button btnJoin   = findViewById(R.id.btnJoin);
        EditText editCode = findViewById(R.id.editCode);

        // ── CRÉER une partie ──────────────────────────────
        btnCreate.setOnClickListener(v -> {
            String code = generateRoomCode();
            createGameInFirebase(code);

            Intent intent = new Intent(this, WaitingActivity.class);
            intent.putExtra("roomCode", code);
            intent.putExtra("mySymbol", "X");
            startActivity(intent);
        });

        // ── REJOINDRE une partie ──────────────────────────
        btnJoin.setOnClickListener(v -> {
            String code = editCode.getText().toString().toUpperCase().trim();

            if (code.length() != 4) {
                Toast.makeText(this, "Entre un code de 4 caractères", Toast.LENGTH_SHORT).show();
                return;
            }

            // Vérifier si la room existe dans Firebase
            db.child("games").child(code).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    // Mettre à jour Firebase : joueur 2 a rejoint
                    db.child("games").child(code).child("player2").setValue("O");
                    db.child("games").child(code).child("status").setValue("playing");

                    Intent intent = new Intent(this, OnlineGameActivity.class);
                    intent.putExtra("roomCode", code);
                    intent.putExtra("mySymbol", "O");
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Code introuvable !", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Génère un code aléatoire de 4 caractères ex: "XY4K"
    private String generateRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    // Crée la partie dans Firebase
    private void createGameInFirebase(String code) {
        Map<String, Object> gameData = new HashMap<>();
        gameData.put("player1", "X");
        gameData.put("player2", "");
        gameData.put("currentTurn", "X");
        gameData.put("status", "waiting");
        gameData.put("winner", "");

        // 9 cases vides
        Map<String, Object> board = new HashMap<>();
        for (int i = 0; i < 9; i++) {
            board.put(String.valueOf(i), "");
        }
        gameData.put("board", board);

        db.child("games").child(code).setValue(gameData);
    }
}