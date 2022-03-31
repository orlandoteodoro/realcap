package br.com.realcapreal.realcapvendas;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.client.android.BuildConfig;
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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Objects;

import static java.lang.Integer.parseInt;

public class Carrinho extends AppCompatActivity {

    private AlertDialog alerta;
    ArrayList<String> reciboc;
    ArrayList<String> recibog;
    ArrayAdapter<String> arrayAdapter;
    ArrayAdapter<String> arrayAdapteg;
    Boolean contri;
    Boolean checa;
    String chunica;
    String codigo;
    String modo;
    private Double valor;
    TextView txtTot;
    private String edicao;
    private String server;
    private long lastBackPressTime = 0;
    private Toast toast;
    static final int Dialog_id = 1;
    private String post_link;
    private Boolean promorua;

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrinho);

        //Checar ajuste
        SharedPreferences ajustes = getSharedPreferences("AJUSTAR_DISPOSITIVO", 0);
        boolean tipo_dispositivo = ajustes.getBoolean("ajuste", false);
        if(!tipo_dispositivo) {
            Intent intent = new Intent(Carrinho.this, Ajustes.class);
            startActivity(intent);
        }

        //Desativar promoshow
        SharedPreferences prefp = getSharedPreferences("PROMOCAO", 0);
        promorua = prefp.getBoolean("promorua", false);
        SharedPreferences.Editor editor = prefp.edit();
        editor.putBoolean("promocao", false);
        editor.apply();

        //Checar Login
        SharedPreferences prefs = getSharedPreferences("LOGIN", 0);
        boolean logado = prefs.getBoolean("logado", false);
        if (!logado){
            Intent intent = new Intent(Carrinho.this, Inicio.class);
            startActivity(intent);
        }else{
            server = getResources().getString(R.string.app_server);
            contri = true;
            codigo  = prefs.getString("codigo", null);
            chunica = prefs.getString("chunica", null);
            modo = prefs.getString("tipo", null);

            if (verificaConexao()){
                showDialog(Dialog_id);
                new Thread() {

                    public void run() {
                        postHttp("e");
                    }
                }.start();
            }

            //Funçoes dos botões
            ImageView buttonBarCodeScan = findViewById(R.id.imageVadd);
            buttonBarCodeScan.setOnClickListener(view -> {
                if (!promorua) {
                    scaner();
                }else{
                    modal("VENDA PROMOCIONAL . FUNÇÃO DESATIVADA.", getResources().getString(R.string.ok), "");
                }
            });

            TextView textBarcodeSacan = findViewById(R.id.textVadd);
            textBarcodeSacan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!promorua) {
                        scaner();
                    }else{
                        modal("VENDA PROMOCIONAL . FUNÇÃO DESATIVADA.", getResources().getString(R.string.ok), "");
                    }
                }
            });
        }

        ListView listc = findViewById(R.id.listc);
        ListView listg = findViewById(R.id.listg);

        reciboc = new ArrayList<>();
        recibog = new ArrayList<>();
        txtTot = findViewById(R.id.textVtot);
        txtTot.setVisibility(View.INVISIBLE);

        arrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reciboc);
        listc.setAdapter(arrayAdapter);
        listc.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                String selectedmovie = reciboc.get(position);

                for(int i = 0; i < reciboc.size(); i ++){
                    if(reciboc.get(i).equals(selectedmovie)){
                        Toast.makeText(getApplicationContext(), "RECIBO", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "RECIBO N", Toast.LENGTH_LONG).show();

                    }
                }

            }
        });

        arrayAdapteg =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, recibog);
        listg.setAdapter(arrayAdapteg);
        listg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                String selectedmovie = recibog.get(position);

                for(int i = 0; i < recibog.size(); i ++){
                    if(recibog.get(i).equals(selectedmovie)){
                        Toast.makeText(getApplicationContext(), "RECIBO", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "RECIBO N", Toast.LENGTH_LONG).show();

                    }
                }
            }
        });
    }

    //Função scanear QR code
    public void scaner(){
        new IntentIntegrator(Carrinho.this).setCaptureActivity(ScanerActivity.class).initiateScan();
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

    @SuppressLint("SetTextI18n")
    public void showResultDialogue(final String result) {
        int resulI = result.length();
        if (resulI==19) {
            checa = false;

            String Edicao = result.substring(8, 11);

            if (Edicao.equals(edicao)) {

                String Recibo = result.substring(11, 18);
                String Digito = result.substring(18, 19);
                String RecibD = Recibo + " - " + Digito;

                for (int i = 0; i < reciboc.size(); i++) {
                    if (reciboc.get(i).equals(RecibD)) {
                        checa = true;
                    }
                }
                for (int i = 0; i < recibog.size(); i++) {
                    if (recibog.get(i).equals(RecibD)) {
                        checa = true;
                    }
                }

                if (!checa) {
                    if (contri) {
                        reciboc.add(RecibD);
                        arrayAdapter.notifyDataSetChanged();
                        contri = false;

                        if (reciboc.size() == 12) {
                            TextView tAdd = findViewById(R.id.textVadd);
                            ImageView iAdd = findViewById(R.id.imageVadd);
                            tAdd.setVisibility(View.GONE);
                            iAdd.setVisibility(View.GONE);
                        }
                        if (!promorua){
                            modal(getResources().getString(R.string.ale_escaneargratis), getResources().getString(R.string.ok), "scaner");
                        }else {
                            int recibosscaneados = reciboc.size()+recibog.size();
                            int resta = 20-recibosscaneados;
                            modal("ESCANEIE MAIS "+resta+" RECIBOS.", getResources().getString(R.string.ok), "scaner");
                        }
                    } else {
                        Button btEnviar = findViewById(R.id.btEnviar);
                        btEnviar.setEnabled(true);
                        recibog.add(RecibD);
                        arrayAdapteg.notifyDataSetChanged();
                        contri = true;
                        txtTot.setVisibility(View.VISIBLE);
                        if (reciboc.size()==10){
                            valor = 5.0;
                            txtTot.setText(getResources().getString(R.string.cartotal) + " " + NumberFormat.getCurrencyInstance().format(valor * reciboc.size()) + " - " + reciboc.size() + " " + getResources().getString(R.string.cartotun));
                            modal(getResources().getString(R.string.ale_promocao), getResources().getString(R.string.ok), "");
                        }else{
                            txtTot.setText(getResources().getString(R.string.cartotal) + " " + NumberFormat.getCurrencyInstance().format(valor * reciboc.size()) + " - " + reciboc.size() + " " + getResources().getString(R.string.cartotun));
                            if (promorua){
                                int recibosscaneados = reciboc.size()+recibog.size();
                                int resta = 20-recibosscaneados;
                                modal("ESCANEIE MAIS "+resta+" RECIBOS.", getResources().getString(R.string.ok), "scaner");
                            }
                        }
                    }
                } else {
                    modal(getResources().getString(R.string.ale_recibojaescaneado), getResources().getString(R.string.ok), "scaner");
                }
            } else {
                modal(getResources().getString(R.string.ale_edicaonaodisponivel), getResources().getString(R.string.ok), "scaner");
            }
        }
    }
    //fim

    //função de envio de dados para o servidor
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void cHecar (View view){
        if (verificaConexao()) {
            if (!promorua) {
                showDialog(Dialog_id);
                new Thread() {

                    public void run() {
                        postHttp("r");
                    }
                }.start();
            }else{
                if (reciboc.size()==10){
                    showDialog(Dialog_id);
                    new Thread() {

                        public void run() {
                            postHttp("r");
                        }
                    }.start();
                }else{
                    modal("VOCE DEVE ESCANEAR 20 RECIBOS", getResources().getString(R.string.botao_ok), "");
                }
            }
        }
    }

    public void postHttp(String tipo) {

        if (tipo.equals("e")){
            post_link = "checagens/checar_edicao.php";
        }else if (tipo.equals("r")){
            post_link = "checagens/checar_recibos.php";
        }
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(server + post_link);


        try {
            if (tipo.equals("r")) {
                String[] reciboC = new String[reciboc.size()];
                reciboC = reciboc.toArray(reciboC);
                String[] reciboG = new String[recibog.size()];
                reciboG = recibog.toArray(reciboG);
                Integer reciboq = reciboC.length;

                ArrayList<NameValuePair> valores = new ArrayList<>(reciboC.length);
                for (String s : reciboC) {
                    valores.add(new BasicNameValuePair("reciboC[]", s));
                }
                for (String s : reciboG) {
                    valores.add(new BasicNameValuePair("reciboG[]", s));
                }

                valores.add(new BasicNameValuePair("codigo", codigo));
                valores.add(new BasicNameValuePair("chunica", chunica));
                valores.add(new BasicNameValuePair("modo", modo));
                valores.add(new BasicNameValuePair("edicao", edicao));

                httpPost.setEntity(new UrlEncodedFormEntity(valores));
                final HttpResponse resposta = httpClient.execute(httpPost);
                final String retorno = EntityUtils.toString(resposta.getEntity());

                JSONObject reader = new JSONObject(retorno);
                JSONObject resultado  = reader.getJSONObject("resultado");
                String status = resultado.getString("status");

                if (status.equals("logado")){
                    SharedPreferences pref = getSharedPreferences("LOGIN", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("chunica", resultado.getString("chunica"));
                    editor.apply();
                    chunica = resultado.getString("chunica");
                    if (resultado.getString("registrados").equals("0")) {
                        Intent i = new Intent(this, Finalizar.class);
                        if (reciboq.equals(10)){
                            Finalizar.valor = 50.0;
                        }else{
                            Finalizar.valor = valor * reciboq;
                        }

                        Finalizar.edicao = edicao;
                        Finalizar.reciboc = reciboc;
                        Finalizar.recibog = recibog;
                        startActivity(i);
                    }else{
                        modal(getResources().getString(R.string.ale_recibojaregistrado),getResources().getString(R.string.botao_ok),"reload");
                    }
                }else{
                    dismissDialog(Dialog_id);
                    modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"));
                }

            }else if (tipo.equals("e")){
                ArrayList<NameValuePair> dados = new ArrayList<NameValuePair>();
                dados.add(new BasicNameValuePair("codigo", codigo));
                dados.add(new BasicNameValuePair("chunica", chunica));
                dados.add(new BasicNameValuePair("modo", modo));

                httpPost.setEntity(new UrlEncodedFormEntity(dados));
                final HttpResponse resposta = httpClient.execute(httpPost);
                final String retorno = EntityUtils.toString(resposta.getEntity());

                JSONObject reader = new JSONObject(retorno);
                JSONObject resultado  = reader.getJSONObject("resultado");
                String status = resultado.getString("status");

                if (status.equals("logado")){
                    SharedPreferences pref = getSharedPreferences("LOGIN", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("chunica", resultado.getString("chunica"));
                    editor.apply();
                    chunica = resultado.getString("chunica");

                    dismissDialog(Dialog_id);
                    String codstring = resultado.getString("vercao_pax");
                    int codinteg = parseInt(codstring);
                    if(codinteg>12){

                        SharedPreferences prea = getSharedPreferences("ATUALIZACAO", 0);
                        SharedPreferences.Editor editora = prea.edit();
                        editora.putBoolean("atualizar", true);
                        editora.putString("vercao", codstring);
                        editora.apply();

                        Intent intent = new Intent(Carrinho.this, Atualizar.class);
                        startActivity(intent);
                    }else {
                        dismissDialog(Dialog_id);
                        edicao = resultado.getString("edicao");
                        valor = resultado.getDouble("valor");
                        scaner();
                    }
                }else{
                    dismissDialog(Dialog_id);
                    modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"));
                }
            }

        } catch (IOException | JSONException ignored) {
        }
    }
    //fim

    //exibição de alertas
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
                        if (acao.equals("reload")){
                            Intent intent = new Intent(Carrinho.this, Carrinho.class);
                            startActivity(intent);
                        }

                        if (acao.equals("logof")){
                            Carrinho.this.getSharedPreferences("LOGIN", 0).edit().clear().apply();
                            Intent intent = new Intent(Carrinho.this, Login.class);
                            startActivity(intent);
                        }

                        if (acao.equals("inicio")){
                            Intent intent = new Intent(Carrinho.this, Inicio.class);
                            startActivity(intent);
                        }

                        if (acao.equals("scaner")){
                            scaner();
                        }
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(Carrinho.this);
                builder.setView(view);
                builder.setCancelable(false);
                alerta = builder.create();
                alerta.show();
            }
        });
    }
    //fim

    //outras funções
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

    public void vOltar (View view){
        Intent intent = new Intent(Carrinho.this, Inicio.class);
        startActivity(intent);
    }
}