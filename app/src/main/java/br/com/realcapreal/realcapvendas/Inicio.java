package br.com.realcapreal.realcapvendas;

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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

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
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Inicio extends AppCompatActivity {

    private long lastBackPressTime = 0;
    static final int Dialog_id = 1;
    private Toast toast;
    private String chunica;
    private String codigo;
    private String modo;
    private String server;
    private AlertDialog alerta;
    public static ArrayList<String> reciboc;
    public static ArrayList<String> recibog;
    private String post_link;

    ArrayList<String> recibocf;
    ArrayList<String> recibogf;

    protected Dialog onCreateDialog(int id) {
        if (id == Dialog_id) {
            ProgressDialog pd = new ProgressDialog(this, R.style.styleProgressDialog);
            Objects.requireNonNull(pd.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            pd.setCancelable(false);
            return pd;
        }
        return null;
    }

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
        setContentView(R.layout.activity_inicio);

        //Checar Login
        SharedPreferences prefs = getSharedPreferences("LOGIN", 0);
        boolean logado = prefs.getBoolean("logado", false);
        if (!logado){
            Intent intent = new Intent(Inicio.this, Login.class);
            startActivity(intent);
        }else {
            String nome = prefs.getString("nome", null);
            String regional = prefs.getString("regional", null);
            TextView txNome = findViewById(R.id.txtN);
            TextView txRegional = findViewById(R.id.txtR);
            txNome.setText(nome);
            txRegional.setText(regional);
            ImageView buttonCriPro = findViewById(R.id.imgPro);
            TextView textProvisorio = findViewById(R.id.txtPro);
            ImageView buttonPromocao = findViewById(R.id.imgPrc);
            TextView textPromocao = findViewById(R.id.txtPrc);
            codigo = prefs.getString("codigo", "0");
            chunica = prefs.getString("chunica", "0");
            modo = prefs.getString("tipo", "0");
            if (modo.equals("provisorio")) {
                buttonCriPro.setVisibility(View.GONE);
                textProvisorio.setVisibility(View.GONE);
                //buttonPromocao.setVisibility(View.GONE);
                //textPromocao.setVisibility(View.GONE);
            }
            server = getResources().getString(R.string.app_server);
            reciboc = new ArrayList<>();
            recibog = new ArrayList<>();
            reciboc.add("0");
            recibog.add("0");

            SharedPreferences transacao_pendente = getSharedPreferences("TRANSACAO_PENDENTE", 0);
            boolean pendente = transacao_pendente.getBoolean("pendente", false);

            if (pendente){
                if (verificaConexao()) {
                    showDialog(Dialog_id);
                    new Thread() {

                        public void run() {
                                postHttp("d");
                        }
                    }.start();
                } else {
                    modal(getResources().getString(R.string.ale_semconexao), getResources().getString(R.string.botao_ok), "0");
                }
            }

            //Funçoes dos botões
            buttonCriPro.setOnClickListener(view -> {
                Intent intent = new Intent(Inicio.this, Criar_provisorio.class);
                startActivity(intent);
            });

            textProvisorio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Inicio.this, Criar_provisorio.class);
                    startActivity(intent);
                }
            });

            SharedPreferences prefp = getSharedPreferences("PROMOCAO", 0);
            SharedPreferences.Editor editor = prefp.edit();

            ImageView buttonBarCodeScan = findViewById(R.id.imgScan);
            buttonBarCodeScan.setOnClickListener(view -> {
                editor.putBoolean("promorua", false);
                editor.apply();
                Intent intent = new Intent(Inicio.this, Carrinho.class);
                startActivity(intent);
            });

            TextView textBarcodeSacan = findViewById(R.id.txtEcanear);
            textBarcodeSacan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editor.putBoolean("promorua", false);
                    editor.apply();
                    Intent intent = new Intent(Inicio.this, Carrinho.class);
                    startActivity(intent);
                }
            });

            ImageView buttonRelatorio = findViewById(R.id.imgRel);
            buttonRelatorio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Inicio.this, Relatorios.class);
                    startActivity(intent);
                }
            });

            TextView textRelatorio = findViewById(R.id.txtRel);
            textRelatorio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Inicio.this, Relatorios.class);
                    startActivity(intent);
                }
            });

            ImageView buttonReimprimir = findViewById(R.id.imgRei);
            buttonReimprimir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Inicio.this, Checar.class);
                    startActivity(intent);
                }
            });

            TextView textReimprimir = findViewById(R.id.txtRei);
            textReimprimir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Inicio.this, Checar.class);
                    startActivity(intent);
                }
            });

            buttonPromocao.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (verificaConexao()) {
                        showDialog(Dialog_id);
                        new Thread() {

                            public void run() {
                                postHttp("c");
                            }
                        }.start();
                    }
                }
            });

            textPromocao.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (verificaConexao()) {
                        showDialog(Dialog_id);
                        new Thread() {

                            public void run() {
                                postHttp("c");
                            }
                        }.start();
                    }
                }
            });

            ImageView buttonSair = findViewById(R.id.imgSair);
            buttonSair.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Inicio.this.getSharedPreferences("LOGIN", 0).edit().clear().apply();
                    Intent intent = new Intent(Inicio.this, Login.class);
                    startActivity(intent);
                }
            });
            //fim
        }
    }

    //Envio de dados para o servidor
    public void postHttp(String tipo) {

        if (tipo.equals("c")) {
            post_link = "checagens/checar_promocao.php";
        }

        if (tipo.equals("d")){
            post_link = "vendas/venda_cartao_pendente.php";
        }

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(server+post_link);

        try {

            if (tipo.equals("d")){

                SharedPreferences pret = getSharedPreferences("TRANSACAO_PENDENTE", 0);

                ArrayList<NameValuePair> dados = new ArrayList<>();
                dados.add(new BasicNameValuePair("codigo", codigo));
                dados.add(new BasicNameValuePair("chunica", chunica));
                dados.add(new BasicNameValuePair("modo", modo));
                dados.add(new BasicNameValuePair("cliente", pret.getString("cliente", null)));
                dados.add(new BasicNameValuePair("ddd", pret.getString("ddd", null)));
                dados.add(new BasicNameValuePair("fone", pret.getString("fone", null)));
                dados.add(new BasicNameValuePair("nome", pret.getString("nome", null)));
                dados.add(new BasicNameValuePair("cpf", pret.getString("cpf", null)));
                dados.add(new BasicNameValuePair("endereco", pret.getString("endereco", null)));
                dados.add(new BasicNameValuePair("complemento", pret.getString("complemento", null)));
                dados.add(new BasicNameValuePair("bairro", pret.getString("bairro", null)));
                dados.add(new BasicNameValuePair("cidade", pret.getString("cidade", null)));
                dados.add(new BasicNameValuePair("reciboc", pret.getString("reciboc", null)));
                dados.add(new BasicNameValuePair("recibog", pret.getString("recibog", null)));
                dados.add(new BasicNameValuePair("PAN_MASKED", pret.getString("PAN_MASKED", null)));
                dados.add(new BasicNameValuePair("PRODUCT_SELECTED", pret.getString("PRODUCT_SELECTED", null)));
                dados.add(new BasicNameValuePair("AUTH_CODE", pret.getString("AUTH_CODE", null)));
                dados.add(new BasicNameValuePair("CARD_BRAND", pret.getString("CARD_BRAND", null)));
                dados.add(new BasicNameValuePair("AMOUNT", pret.getString("AMOUNT", null)));
                dados.add(new BasicNameValuePair("TID", pret.getString("TID", null)));

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

                    if (resultado.getString("mensagem").equals("SUCESSO")) {

                        Inicio.this.getSharedPreferences("TRANSACAO_PENDENTE", 0).edit().clear().apply();

                        Intent intent = new Intent(Inicio.this, Impressao.class);
                        Bundle bundle = new Bundle();

                        bundle.putString("ddd", resultado.getString("ddd"));
                        bundle.putString("celular", resultado.getString("fone"));
                        bundle.putString("cliente", resultado.getString("nome"));
                        bundle.putString("datahora", resultado.getString("datahora"));
                        bundle.putString("edicao", resultado.getString("edicao"));
                        bundle.putDouble("valor", resultado.getDouble("valor"));
                        bundle.putString("autenticacao", resultado.getString("autenticacao"));
                        bundle.putBoolean("reimpressao", false);
                        bundle.putBoolean("impressao", true);
                        bundle.putString("tipo", "Cartao");
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

                        Impressao.recibocf = recibos_contribuicao;
                        Impressao.recibogf = recibos_gratis;

                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        dismissDialog(Dialog_id);
                        SharedPreferences pre = getSharedPreferences("LOGIN", 0);
                        SharedPreferences.Editor editore = pre.edit();
                        editore.putString("chunica", resultado.getString("chunica"));
                        editore.apply();
                        chunica = resultado.getString("chunica");
                        modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"));
                    }
                }else{
                    dismissDialog(Dialog_id);
                    modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"));
                }
            }

            if (tipo.equals("c")) {

                ArrayList<NameValuePair> dados = new ArrayList<>();
                dados.add(new BasicNameValuePair("codigo", codigo));
                dados.add(new BasicNameValuePair("chunica", chunica));
                dados.add(new BasicNameValuePair("modo", modo));
                dados.add(new BasicNameValuePair("versao", BuildConfig.VERSION_NAME));

                httpPost.setEntity(new UrlEncodedFormEntity(dados));
                final HttpResponse resposta = httpClient.execute(httpPost);
                final String retorno = EntityUtils.toString(resposta.getEntity());
                JSONObject reader = new JSONObject(retorno);
                dismissDialog(Dialog_id);
                JSONObject resultado = reader.getJSONObject("resultado");
                final String status = resultado.getString("status");
                if (status.equals("logado")) {

                    SharedPreferences pref = getSharedPreferences("LOGIN", 0);
                    SharedPreferences.Editor edt = pref.edit();
                    edt.putString("chunica", resultado.getString("chunica"));
                    edt.apply();
                    chunica = resultado.getString("chunica");

                    if (resultado.getString("promocao").equals("1")) {

                        SharedPreferences prefs = getSharedPreferences("PROMOCAO", 0);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("promorua", true);
                        editor.apply();

                        Intent intent = new Intent(Inicio.this, Carrinho.class);
                        startActivity(intent);

                    /*
                    String valor_promo = resultado.getString("valor_promo");
                    double valor = Double.parseDouble(valor_promo);
                    String edicao = resultado.getString("edicao");
                    Intent i = new Intent(this, Finalizar.class);
                    Finalizar.valor = valor;
                    Finalizar.edicao = edicao;
                    Finalizar.reciboc = reciboc;
                    Finalizar.recibog = recibog;
                    startActivity(i);
                    */

                    } else {
                        final String resultado_mensagem = resultado.getString("mensagem");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LayoutInflater li = getLayoutInflater();
                                @SuppressLint("InflateParams") View view = li.inflate(R.layout.modal, null);
                                TextView aViso = view.findViewById(R.id.txAlerta);
                                aViso.setText(resultado_mensagem);
                                Button bTalerta = view.findViewById(R.id.btAlerta);
                                bTalerta.setText(getResources().getString(R.string.botao_ok));
                                bTalerta.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View arg0) {
                                        alerta.dismiss();
                                    }
                                });
                                AlertDialog.Builder builder = new AlertDialog.Builder(Inicio.this);
                                builder.setView(view);
                                builder.setCancelable(false);
                                alerta = builder.create();
                                alerta.show();
                            }
                        });
                    }
                } else {
                    final String resultado_mensagem = resultado.getString("mensagem");
                    final String acao = resultado.getString("acao");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LayoutInflater li = getLayoutInflater();
                            @SuppressLint("InflateParams") View view = li.inflate(R.layout.modal, null);
                            TextView aViso = view.findViewById(R.id.txAlerta);
                            aViso.setText(resultado_mensagem);
                            Button bTalerta = view.findViewById(R.id.btAlerta);
                            bTalerta.setText(getResources().getString(R.string.botao_ok));
                            bTalerta.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View arg0) {
                                    alerta.dismiss();
                                    if (acao.equals("logof")) {
                                        Inicio.this.getSharedPreferences("LOGIN", 0).edit().clear().apply();
                                        Intent intent = new Intent(Inicio.this, Login.class);
                                        startActivity(intent);
                                    }
                                }
                            });
                            AlertDialog.Builder builder = new AlertDialog.Builder(Inicio.this);
                            builder.setView(view);
                            builder.setCancelable(false);
                            alerta = builder.create();
                            alerta.show();
                        }
                    });
                }
            }
        } catch (IOException | JSONException ignored) {
        }
    }

    //exibição de modal
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
                        if (acao.equals("reload")) {
                            Intent intent = new Intent(Inicio.this, Carrinho.class);
                            startActivity(intent);
                        }
                        if (acao.equals("deslogar")) {
                            Inicio.this.getSharedPreferences("LOGIN", 0).edit().clear().apply();
                            Intent intent = new Intent(Inicio.this, Login.class);
                            startActivity(intent);
                        }
                        if (acao.equals("inicio")) {
                            Intent intent = new Intent(Inicio.this, Inicio.class);
                            startActivity(intent);
                        }
                    }
                });

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Inicio.this);
                builder.setView(view);
                builder.setCancelable(false);
                alerta = builder.create();
                alerta.show();
            }
        });
    }
    //fim

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