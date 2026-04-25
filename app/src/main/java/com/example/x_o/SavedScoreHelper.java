package com.example.x_o;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SavedScoreHelper {

    private static final String FILE_NAME = "last_tournament.ser";

    /**
     * Sauvegarde le résultat du tournoi dans un fichier interne.
     */
    public static void saveResult(Context context, TournamentResult result) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(result);
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Charge le dernier résultat sauvegardé.
     * Retourne null si aucun fichier n'existe.
     */
    public static TournamentResult loadResult(Context context) {
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            TournamentResult result = (TournamentResult) ois.readObject();
            ois.close();
            fis.close();
            return result;
        } catch (Exception e) {
            // Fichier inexistant ou erreur de lecture
            return null;
        }
    }
}