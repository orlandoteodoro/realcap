package br.com.realcapreal.realcapvendas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import br.com.realcapreal.realcapvendas.uteis.SysTester;

public class Login extends AppCompatActivity {

    private AlertDialog alerta;
    private String server;
    String imei;
    String chip;
    String serial;
    static final Integer PHONESTATS = 0x1;
    static final int Dialog_id = 1;
    private String post_link;

    public  boolean verificaConexao() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            conectado = true;
        } else {
            conectado = false;
        }
        return conectado;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences prefa = getSharedPreferences("AJUSTAR_DISPOSITIVO", 0);
        SharedPreferences.Editor editora = prefa.edit();
        editora.putBoolean("ajuste", true);
        editora.putString("tipo", "maquina");
        editora.apply();

        //Checar Login
        SharedPreferences prefs = getSharedPreferences("LOGIN", 0);
        boolean uLogado = prefs.getBoolean("logado", false);
        if(uLogado) {
            Intent intent = new Intent(Login.this, Inicio.class);
            startActivity(intent);
            finish();
        }else {
            server = getResources().getString(R.string.app_server);
            serial = SysTester.getInstance().getSn();

            consultarPermiso();
            SharedPreferences pref = getSharedPreferences("DADOS_MAQUINA", 0);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("imei", imei);
            editor.putString("chip", chip);
            editor.putString("serial", serial);
            editor.apply();

            TextView txtVercao = findViewById(R.id.txtVercao);
            String versionName = BuildConfig.VERSION_NAME;
            txtVercao.setText(versionName);
        }
    }

    protected Dialog onCreateDialog(int id) {
        if (id == Dialog_id) {
            ProgressDialog pd = new ProgressDialog(this, R.style.styleProgressDialog);
            Objects.requireNonNull(pd.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            pd.setCancelable(false);
            return pd;
        }
        return null;
    }

    //Realizando login no sistema

    public void Logar (View view){
        final EditText codigo = findViewById(R.id.editTCodigo);
        final EditText senha = findViewById(R.id.editTSenha);

        if (codigo.length() == 4 && senha.length() > 3){
            if (verificaConexao()){
                showDialog(Dialog_id);

                new Thread() {
                    public void run() {

                        postHttp( codigo.getText().toString(), senha.getText().toString(), "vendedor" );
                    }
                }.start();
            }
        }else{
            modal(getResources().getString(R.string.txt_senha_incorreta), getResources().getString(R.string.botao_ok));
        }
    }

    public void postHttp( String codigo, String senha, String tipo){

        if (tipo.equals("vendedor")) {
            post_link = "checagens/checar_login.php";
        }else{
            post_link = "checagens/checar_login_provisorio.php";
        }

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(server + post_link);

        try{
            ArrayList<NameValuePair> valores = new ArrayList<NameValuePair>();
            valores.add(new BasicNameValuePair("codigo", codigo));
            if (tipo.equals("vendedor")) {
                valores.add(new BasicNameValuePair("senha", senha));
            }
            valores.add(new BasicNameValuePair("imei", imei));
            valores.add(new BasicNameValuePair("chip", chip));
            valores.add(new BasicNameValuePair("serial", serial));

            httpPost.setEntity(new UrlEncodedFormEntity(valores));
            final HttpResponse resposta = httpClient.execute(httpPost);
            final String retorno = EntityUtils.toString(resposta.getEntity());

            JSONObject reader = new JSONObject(retorno);
            JSONObject resultado  = reader.getJSONObject("resultado");
            String logado = resultado.getString("logado");
            dismissDialog(Dialog_id);

            if (logado.equals("1")){

                SharedPreferences pref = getSharedPreferences("LOGIN", 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("logado", true);
                editor.putBoolean("encerrado", false);
                editor.putString("tipo", "vendedor");
                editor.putString("codigo", resultado.getString("codigo"));
                editor.putString("nome", resultado.getString("nome"));
                editor.putString("regional", resultado.getString("regional"));
                editor.putString("chunica", resultado.getString("chunica"));

                editor.apply();

                Intent intent = new Intent(Login.this, Inicio.class);
                startActivity(intent);

                dismissDialog(Dialog_id);
            }else if (logado.equals("2")){
                SharedPreferences pref = getSharedPreferences("LOGIN", 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("logado", true);
                editor.putBoolean("encerrado", false);
                editor.putString("tipo", "provisorio");
                editor.putString("codigo", resultado.getString("codigo"));
                editor.putString("nome", resultado.getString("nome"));
                editor.putString("regional", resultado.getString("regional"));
                editor.putString("chunica", resultado.getString("chunica"));

                editor.apply();

                Intent intent = new Intent(Login.this, Inicio.class);
                startActivity(intent);

                dismissDialog(Dialog_id);
            }else{
                modal(resultado.getString("mensagem"), resultado.getString("botao"));
                dismissDialog(Dialog_id);
            }
        } catch (IOException ignored){} catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //fim da autenticação no sistema

    //exibição de alertas
    public void modal (String txtAlerta, String btAlerta){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater li = getLayoutInflater();

                View view = li.inflate(R.layout.modal, null);
                TextView aviCaD = view.findViewById(R.id.txAlerta);
                Button btAvi = view.findViewById(R.id.btAlerta);
                aviCaD.setText(txtAlerta);
                btAvi.setText(btAlerta);

                view.findViewById(R.id.btAlerta).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        alerta.dismiss();
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                builder.setView(view);
                builder.setCancelable(false);
                alerta = builder.create();
                alerta.show();
            }
        });
    }
    //fim da exibição de alertas

    //Obtendo dados da máquina
    private void consultarPermiso() {
        if (ContextCompat.checkSelfPermission(Login.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Login.this, Manifest.permission.READ_PHONE_STATE)) {
                ActivityCompat.requestPermissions(Login.this, new String[]{Manifest.permission.READ_PHONE_STATE}, Login.PHONESTATS);
            } else {
                ActivityCompat.requestPermissions(Login.this, new String[]{Manifest.permission.READ_PHONE_STATE}, Login.PHONESTATS);
            }
        } else {
            imei = obterIMEI();
            chip = obterCHIP();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imei = obterIMEI();
                chip = obterCHIP();
            } else {
                Toast.makeText(Login.this, "Conceda permissão ao Aplicativo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    private String obterIMEI() {
        final TelephonyManager telephonyManager= (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assert telephonyManager != null;
            return telephonyManager.getImei();
        }else {
            assert telephonyManager != null;
            return telephonyManager.getDeviceId();
            //return Build.USER;
        }

    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    private String obterCHIP() {
        final TelephonyManager telephonyManager= (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assert telephonyManager != null;
            return telephonyManager.getSimSerialNumber();
        }else {
            assert telephonyManager != null;
            return telephonyManager.getSimSerialNumber();
        }

    }
    //fim da obtensão de dados

    //Função scanear QR code
    public void scaner(View view){
        new IntentIntegrator(Login.this).setCaptureActivity(ScanerActivity.class).initiateScan();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Intent intent = new Intent(this, Inicio.class);
                startActivity(intent);
            } else {
                showResultDialogue(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void ajustes (View view){
        Intent intent = new Intent(Login.this, Ajustes.class);
        startActivity(intent);
    }
    public void showResultDialogue(final String result) {
        int resulI = result.length();
        if (resulI==11) {
            if (verificaConexao()) {
                showDialog(Dialog_id);
                String codigo = result.substring(7, 11);
                new Thread() {
                    public void run() {
                        postHttp(codigo, "0", "provisorio");
                    }
                }.start();
            }
        }else{
            modal(getResources().getString(R.string.ale_conexao), getResources().getString(R.string.botao_ok));
        }
    }
}