package br.com.realcapreal.realcapvendas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

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
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Objects;

public class Criar_provisorio extends AppCompatActivity {

    private String codigo;
    private String chunica;
    private String modo;
    private String server;
    private AlertDialog alerta;
    private Toast toast;
    private long lastBackPressTime = 0;
    static final int Dialog_id = 1;

    public  boolean verificaConexao() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert conectivtyManager != null;
        conectado = conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected();
        return conectado;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_provisorio);

        //Checar ajuste
        SharedPreferences ajustes = getSharedPreferences("AJUSTAR_DISPOSITIVO", 0);
        boolean tipo_dispositivo = ajustes.getBoolean("ajuste", false);
        if(!tipo_dispositivo) {
            Intent intent = new Intent(Criar_provisorio.this, Ajustes.class);
            startActivity(intent);
        }

        //Checar Login
        SharedPreferences prefs = getSharedPreferences("LOGIN", 0);
        boolean logado = prefs.getBoolean("logado", false);
        if (!logado){
            Intent intent = new Intent(Criar_provisorio.this, Login.class);
            startActivity(intent);
        }else {
            server = getResources().getString(R.string.app_server);
            codigo = prefs.getString("codigo", "0");
            chunica = prefs.getString("chunica", "0");
            modo = prefs.getString("tipo", "0");

            EditText edtNome = findViewById(R.id.edtNome);
            EditText edtReferencia = findViewById(R.id.edtReferencia);
            edtNome.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            edtReferencia.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        }
    }

    public void criarSub (View view){
        showDialog(Dialog_id);
        if (verificaConexao()) {
            new Thread() {

                public void run() {
                    postHttp();
                }
            }.start();
        }else{
            modal(getResources().getString(R.string.ale_semconexao), getResources().getString(R.string.botao_ok), "inicio");
        }
    }

    public static String removerAcentos(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    //Envio de dados para o servidor
    public void postHttp() {

        EditText edtNome = findViewById(R.id.edtNome);
        EditText edtReferencia = findViewById(R.id.edtReferencia);
        String nome = edtNome.getText().toString();
        String referencia = edtReferencia.getText().toString();

        if (nome.length()>3 || referencia.length()>3) {

            RadioButton duracao = findViewById(R.id.rdHoje);

            String post_link = "cadastros/criar_provisorio.php";

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(server + post_link);

            try {

                ArrayList<NameValuePair> dados = new ArrayList<>();
                dados.add(new BasicNameValuePair("codigo", codigo));
                dados.add(new BasicNameValuePair("chunica", chunica));
                dados.add(new BasicNameValuePair("modo", modo));
                dados.add(new BasicNameValuePair("nome", removerAcentos(nome)));
                dados.add(new BasicNameValuePair("referencia", removerAcentos(referencia)));
                if (duracao.isChecked()){
                    dados.add(new BasicNameValuePair("duracao", "0"));
                }else{
                    dados.add(new BasicNameValuePair("duracao", "1"));
                }

                httpPost.setEntity(new UrlEncodedFormEntity(dados));
                final HttpResponse resposta = httpClient.execute(httpPost);
                final String retorno = EntityUtils.toString(resposta.getEntity());
                JSONObject reader = new JSONObject(retorno);
                JSONObject resultado = reader.getJSONObject("resultado");
                String status = resultado.getString("status");
                dismissDialog(Dialog_id);

                if (status.equals("logado")){

                    SharedPreferences pref = getSharedPreferences("LOGIN", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("chunica", resultado.getString("chunica"));
                    editor.apply();

                    if (!resultado.getString("provisorio").equals("0")){
                        Intent intent = new Intent(Criar_provisorio.this, Provisorio_qrcode.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("provisorio", resultado.getString("provisorio"));
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"));
                    }
                }else{
                    modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"));
                }

            } catch (IOException | JSONException ignored) {
            }
        }else{
            dismissDialog(Dialog_id);
            modal("PREENCHA CORRETAMENTE TODOS OS CAMPOS", getResources().getString(R.string.botao_ok), "0");
        }
    }

    //exibição do modal
    public void modal (String txtAlerta, String btAlerta, String acao){
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
                        if (acao.equals("inicio")) {
                            Intent intent = new Intent(Criar_provisorio.this, Inicio.class);
                            startActivity(intent);
                        }
                        if (acao.equals("logof")) {
                            Criar_provisorio.this.getSharedPreferences("LOGIN", 0).edit().clear().apply();
                            Intent intent = new Intent(Criar_provisorio.this, Login.class);
                            startActivity(intent);
                        }
                    }
                });

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Criar_provisorio.this);
                builder.setView(view);
                builder.setCancelable(false);
                alerta = builder.create();
                alerta.show();
            }
        });
    }

    public void voltar (View view){
        Intent intent = new Intent(Criar_provisorio.this, Inicio.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (this.lastBackPressTime < System.currentTimeMillis() - 4000) {
            toast = Toast.makeText(this, "Pressione o Botão Voltar novamente para fechar o Aplicativo.", Toast.LENGTH_LONG);
            toast.show();
            this.lastBackPressTime = System.currentTimeMillis();
        } else {
            if (toast != null) {
                toast.cancel();
                finishAffinity();
            }
            super.onBackPressed();
        }
    }
}