package br.com.realcapreal.realcapvendas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class Ajustes extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);
    }

    public void celular (View view){
        SharedPreferences pref = getSharedPreferences("AJUSTAR_DISPOSITIVO", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("ajuste", true);
        editor.putString("tipo", "celular");
        editor.apply();

        Intent intent = new Intent(Ajustes.this, Inicio.class);
        startActivity(intent);
    }

    public void maquina (View view){
        SharedPreferences pref = getSharedPreferences("AJUSTAR_DISPOSITIVO", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("ajuste", true);
        editor.putString("tipo", "maquina");
        editor.apply();

        Intent intent = new Intent(Ajustes.this, Inicio.class);
        startActivity(intent);
    }

    public void voltar (View view){
        Intent intent = new Intent(Ajustes.this, Inicio.class);
        startActivity(intent);
    }
}