package br.com.realcapreal.realcapvendas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.pax.dal.IDAL;
import com.pax.dal.entity.EBeepMode;
import com.pax.neptunelite.api.NeptuneLiteUser;

import br.com.realcapreal.realcapvendas.uteis.SysTester;

public class Atualizar extends AppCompatActivity {

    private DownloadManager downloadManager;
    private Button buttonAtualizar;
    private static IDAL dal;
    private static Context appContext;
    public ProgressDialog progressDialog;
    private static final int PERMISSION_READ_STATE = 0;
    private final String target = "";
    private String vercaoat;

    public  boolean verificaConexao() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert conectivtyManager != null;
        conectado = conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected();
        return conectado;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atualizar);

        SharedPreferences prefs = getSharedPreferences("ATUALIZACAO", 0);
        boolean atualizar = prefs.getBoolean("atualizar", false);

        if (!atualizar){
            Intent intent = new Intent(Atualizar.this, Inicio.class);
            startActivity(intent);
        }else{
            vercaoat = prefs.getString("vercao", null);

            SharedPreferences pref = getSharedPreferences("ATUALIZACAO", 0);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("atualizar", false);
            editor.apply();
        }

        buttonAtualizar = findViewById(R.id.bttAtualizacao);
        downloadManager = (DownloadManager) this.getSystemService(DOWNLOAD_SERVICE);
        registerReceiver(onCompleteApp, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        buttonAtualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SysTester.getInstance().beep(EBeepMode.FREQUENCE_LEVEL_6, 100);
                if (ActivityCompat.checkSelfPermission(Atualizar.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Atualizar.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_READ_STATE);
                    return;
                }
                if (verificaConexao()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Atualizar.this);
                    builder.setTitle("Atualização");
                    builder.setMessage("Atualizar o Aplicativo?");
                    builder.setCancelable(false);
                    builder.setPositiveButton(
                            "Sim",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    SysTester.getInstance().beep(EBeepMode.FREQUENCE_LEVEL_6, 100);
                                    buttonAtualizar.setClickable(false);
                                    dialog.dismiss();
                                    progressDialog = new ProgressDialog(Atualizar.this);
                                    progressDialog.setTitle("Baixando autalização");
                                    progressDialog.setMessage("Aguarde...");
                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    progressDialog.setCancelable(false);
                                    progressDialog.show();
                                    iniciarDownloadApp();
                                }
                            });
                    builder.setNegativeButton(
                            "Não",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    SysTester.getInstance().beep(EBeepMode.FREQUENCE_LEVEL_6, 100);
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert11 = builder.create();
                    alert11.show();
                } else {
                    Toast.makeText(Atualizar.this, "Sem conexão com a internet!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void iniciarDownloadApp() {
        try {
            String url = "http://rcserver.com.br/app/update-realcap-v"+vercaoat+".apk";
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle("Download");
            request.setDescription("Baixando Atualização. Aguarde!");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "update-realcap-v"+vercaoat+".apk");
            downloadManager.enqueue(request);
            SysTester.getInstance().installApp(vercaoat);
        } catch (Exception e) {
            Log.d("Erro", e.getMessage());
            Toast.makeText(this, "Ops! Algo deu errado!", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            buttonAtualizar.setClickable(true);
        }
    }

    BroadcastReceiver onCompleteApp = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.endsWith(intent.getAction())) {
                Toast.makeText(Atualizar.this, "Download Completo!", Toast.LENGTH_LONG).show();
                buttonAtualizar.setClickable(true);
                SysTester.getInstance().installApp(vercaoat);
            }
            progressDialog.dismiss();
        }
    };

    public static IDAL getDal() {
        if (dal == null) {
            try {
                long start = System.currentTimeMillis();
                dal = NeptuneLiteUser.getInstance().getDal(appContext);
                Log.i("Teste", "get dal cost:" + (System.currentTimeMillis() - start) + " ms");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(appContext, "Erro Repita a Operação.", Toast.LENGTH_LONG).show();
            }
        }
        return dal;
    }
}