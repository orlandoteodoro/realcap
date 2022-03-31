package br.com.realcapreal.realcapvendas;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Checar extends AppCompatActivity {

    static final int Dialog_id = 1;
    private AlertDialog alerta;
    private String server;
    String chunica;
    String codigo;
    String modo;

    @RequiresApi(api = Build.VERSION_CODES.M)
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
        setContentView(R.layout.activity_checar);

        //Checar Login
        SharedPreferences prefs = getSharedPreferences("LOGIN", 0);
        boolean logado = prefs.getBoolean("logado", false);
        if (!logado){
            Intent intent = new Intent(Checar.this, Inicio.class);
            startActivity(intent);
        }else {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(broadcastReceiver, filter);
            server = getResources().getString(R.string.app_server);
            codigo  = prefs.getString("codigo", null);
            chunica = prefs.getString("chunica", null);
            modo = prefs.getString("tipo", null);

            scaner();
        }
    }

    //Função scanear QR code
    public void scaner(){
        new IntentIntegrator(Checar.this).setCaptureActivity(ScanerActivity.class).initiateScan();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showResultDialogue(final String result) {
        int resulI = result.length();
        if (resulI==19) {
            if (verificaConexao()) {
                showDialog(Dialog_id);
                new Thread() {

                    public void run() {
                        postHttp(result);
                    }
                }.start();
            }
        }
    }

    public void postHttp(String qrcode) {

        String post_link = "checagens/checar_recibo.php";

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(server + post_link);
        ArrayList<NameValuePair> dados = new ArrayList<>();
        dados.add(new BasicNameValuePair("codigo", codigo));
        dados.add(new BasicNameValuePair("chunica", chunica));
        dados.add(new BasicNameValuePair("modo", modo));
        dados.add(new BasicNameValuePair("qrcode", qrcode));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(dados));
            final HttpResponse resposta = httpClient.execute(httpPost);
            final String retorno = EntityUtils.toString(resposta.getEntity());
            JSONObject reader = new JSONObject(retorno);
            JSONObject resultado = reader.getJSONObject("resultado");
            String status = resultado.getString("status");

            if (status.equals("logado")){

                SharedPreferences pref = getSharedPreferences("LOGIN", 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("chunica", resultado.getString("chunica"));
                editor.apply();

                if (resultado.getString("checagem").equals("1")){
                    if (resultado.getString("impressao").equals("0")){
                        if (resultado.getString("status_venda").equals("confirmado")) {

                            JSONArray recibos_contribuicao_json = resultado.getJSONArray("recibos_contribuicao");
                            JSONArray recibos_gratis_json = resultado.getJSONArray("recibos_gratis");
                            ArrayList<String> recibos_contribuicao = new ArrayList<String>();
                            for (int i = 0; i < (recibos_contribuicao_json).length(); i++) {
                                recibos_contribuicao.add((recibos_contribuicao_json).getString(i));
                            }
                            ArrayList<String> recibos_gratis = new ArrayList<String>();
                            for (int i = 0; i < (recibos_gratis_json).length(); i++) {
                                recibos_gratis.add((recibos_gratis_json).getString(i));
                            }
                            Intent intent = new Intent(Checar.this, Impressao.class);
                            Bundle bundle = new Bundle();

                            bundle.putString("ddd", resultado.getString("ddd"));
                            bundle.putString("celular", resultado.getString("celular"));
                            bundle.putString("cliente", resultado.getString("cliente"));
                            bundle.putString("edicao", resultado.getString("edicao"));
                            bundle.putString("valor", resultado.getString("valor"));
                            bundle.putString("autenticacao", resultado.getString("autenticacao"));
                            bundle.putString("tipo", resultado.getString("tipo"));
                            bundle.putBoolean("reimpressao", true);
                            bundle.putBoolean("impressao", true);
                            Impressao.recibocf = recibos_contribuicao;
                            Impressao.recibogf = recibos_gratis;
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }else{
                            modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"),resultado.getString("img_pix"));
                        }
                    }else{
                        JSONArray recibos_contribuicao_json = resultado.getJSONArray("recibos_contribuicao");
                        JSONArray recibos_gratis_json = resultado.getJSONArray("recibos_gratis");
                        ArrayList<String> recibos_contribuicao = new ArrayList<String>();
                        for (int i = 0; i < (recibos_contribuicao_json).length(); i++) {
                            recibos_contribuicao.add((recibos_contribuicao_json).getString(i));
                        }
                        ArrayList<String> recibos_gratis = new ArrayList<String>();
                        for (int i = 0; i < (recibos_gratis_json).length(); i++) {
                            recibos_gratis.add((recibos_gratis_json).getString(i));
                        }

                        Intent intent = new Intent(Checar.this, Impressao.class);
                        Bundle bundle = new Bundle();

                        bundle.putString("ddd", resultado.getString("ddd"));
                        bundle.putString("celular", resultado.getString("celular"));
                        bundle.putString("cliente", resultado.getString("cliente"));
                        bundle.putString("edicao", resultado.getString("edicao"));
                        bundle.putString("valor", resultado.getString("valor"));
                        bundle.putString("autenticacao", resultado.getString("autenticacao"));
                        bundle.putString("tipo", resultado.getString("tipo"));
                        bundle.putBoolean("reimpressao", true);
                        bundle.putBoolean("impressao", false);
                        Impressao.recibocf = recibos_contribuicao;
                        Impressao.recibogf = recibos_gratis;
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }else{
                    modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"), "");
                }

            } else {
                modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"), "");
            }

        } catch (IOException | JSONException ignored) {
        }
    }
    //fim

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            Log.e ("CONNECTION","hasconnection:"+(netInfo != null && netInfo.isConnected()));
        }
    };

    //exibição do modal
    public void modal (String txtAlerta, String btAlerta, String acao, String img_pix){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater li = getLayoutInflater();
                View view = li.inflate(R.layout.modal, null);
                TextView aviCaD = view.findViewById(R.id.txAlerta);
                Button btAvi = view.findViewById(R.id.btAlerta);
                aviCaD.setText(txtAlerta);
                btAvi.setText(btAlerta);

                if (acao.equals("exibir_pix")){
                    ImageView advertencia = view.findViewById(R.id.imageView5);
                    advertencia.setVisibility(View.GONE);

                    ImageView qr_code_pix = view.findViewById(R.id.imageView6);
                    qr_code_pix.setVisibility(View.VISIBLE);

                    Picasso.with(Checar.this)
                            .load(img_pix)
                            .into(qr_code_pix);

                    aviCaD.setText(txtAlerta);
                }

                view.findViewById(R.id.btAlerta).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        alerta.dismiss();
                        if (acao.equals("exibir_pix")) {
                            Intent intent = new Intent(Checar.this, Inicio.class);
                            startActivity(intent);
                        }
                        if (acao.equals("inicio")) {
                            Intent intent = new Intent(Checar.this, Inicio.class);
                            startActivity(intent);
                        }
                        if (acao.equals("logof")) {
                            Checar.this.getSharedPreferences("LOGIN", 0).edit().clear().apply();
                            Intent intent = new Intent(Checar.this, Login.class);
                            startActivity(intent);
                        }
                    }
                });

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Checar.this);
                builder.setView(view);
                builder.setCancelable(false);
                alerta = builder.create();
                alerta.show();
            }
        });
    }
}