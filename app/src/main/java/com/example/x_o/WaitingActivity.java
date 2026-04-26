package com.example.x_o;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WaitingActivity extends AppCompatActivity {

    private DatabaseReference gameRef;
    private String roomCode, mySymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        roomCode = getIntent().getStringExtra("roomCode");
        mySymbol = getIntent().getStringExtra("mySymbol");

        // Afficher le code sur l'écran
        TextView tvCode = findViewById(R.id.tvRoomCode);
        tvCode.setText("Code : " + roomCode);

        // Appuyer sur le code pour le copier
        tvCode.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("roomCode", roomCode);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Code copié !", Toast.LENGTH_SHORT).show();
        });

        // Écouter Firebase : quand status devient "playing" → l'ami a rejoint
        gameRef = FirebaseDatabase.getInstance().getReference("games/" + roomCode);
        gameRef.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if ("playing".equals(status)) {
                    // L'ami a rejoint ! Lancer le jeu
                    Intent intent = new Intent(WaitingActivity.this, OnlineGameActivity.class);
                    intent.putExtra("roomCode", roomCode);
                    intent.putExtra("mySymbol", mySymbol);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}