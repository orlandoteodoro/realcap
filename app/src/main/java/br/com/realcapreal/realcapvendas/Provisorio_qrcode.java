package br.com.realcapreal.realcapvendas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class Provisorio_qrcode extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provisorio_qrcode);

        //Checar Login
        SharedPreferences prefs = getSharedPreferences("LOGIN", 0);
        boolean logado = prefs.getBoolean("logado", false);
        if (!logado){
            Intent intent = new Intent(Provisorio_qrcode.this, Login.class);
            startActivity(intent);
        }else {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            String provisorio = bundle.getString("provisorio");

            ImageView qr_code = findViewById(R.id.imgQrcode);
            Picasso.with(Provisorio_qrcode.this)
                    .load("https://chart.googleapis.com/chart?chs=250x250&cht=qr&chl="+provisorio)
                    .into(qr_code);

        }
    }

    public void voltar (View view){
        Intent intent = new Intent(Provisorio_qrcode.this, Inicio.class);
        startActivity(intent);
    }
}