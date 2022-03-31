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
import android.view.textservice.TextInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Relatorio_detalhado extends AppCompatActivity {

    private String server;
    private String post_link;
    private String codigo;
    private String chunica;
    private String modo;
    private String edicao_atual;
    private ArrayList<String> listdata;
    private ListView listView;
    private ArrayAdapter<String> listar_sub;
    private AlertDialog alerta;
    private long lastBackPressTime = 0;
    private Toast toast;
    static final int Dialog_id = 1;
    private String tmp;

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
        setContentView(R.layout.activity_relatorio_detalhado);
        //Checar Login
        SharedPreferences prefs = getSharedPreferences("LOGIN", 0);
        boolean logado = prefs.getBoolean("logado", false);
        if (!logado){
            Intent intent = new Intent(Relatorio_detalhado.this, Login.class);
            startActivity(intent);
        }else{
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            edicao_atual = bundle.getString("edicao");

            server = getResources().getString(R.string.app_server);
            codigo = prefs.getString("codigo", "0");
            chunica = prefs.getString("chunica", "0");
            modo = prefs.getString("tipo", "0");

            if(verificaConexao()) {
                new Thread() {

                    public void run() {
                        postHttp("g", "0", "");
                    }
                }.start();
            }else{
                modal(getResources().getString(R.string.ale_semconexao), getResources().getString(R.string.botao_ok),"0");
            }

            listView = findViewById(R.id.listview);
            listdata = new ArrayList<String>();
            listar_sub = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    listdata);
            listView.setAdapter(listar_sub);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @SuppressLint("InflateParams")
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    tmp = (String) parent.getItemAtPosition(position);
                    if(verificaConexao()) {
                        new Thread() {

                            public void run() {
                                postHttp("s",tmp, "v");
                            }
                        }.start();
                    }else{
                        modal(getResources().getString(R.string.ale_semconexao), getResources().getString(R.string.botao_ok),"0");
                    }
                }
            });
        }
    }

    public void voltar (View view){
        Intent intent = new Intent(Relatorio_detalhado.this, Inicio.class);
        startActivity(intent);
    }

    //Envio de dados para o servidor
    public void postHttp(String tipo, String parametrol, String parametroll) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    showDialog(Dialog_id);
            }
        });

        if (tipo.equals("s")){
            if (parametroll.equals("v")) {
                post_link = "relatorios/relatorio_subvendedor_individual.php";
            }
            if (parametroll.equals("p")){
                post_link = "relatorios/relatorio_subvendedor_individual_promo.php";
            }
        }

        if (tipo.equals("a")){
            post_link = "relatorios/relatorio_subvendedor_individual.php";
        }

        if (tipo.equals("g")) {
            post_link = "relatorios/relatorio_subvendedor.php";
        }

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(server + post_link);

        try {
            ArrayList<NameValuePair> dados = new ArrayList<>();
            dados.add(new BasicNameValuePair("codigo", codigo));
            dados.add(new BasicNameValuePair("chunica", chunica));
            dados.add(new BasicNameValuePair("modo", modo));
            dados.add(new BasicNameValuePair("edicao", edicao_atual));
            if (tipo.equals("s")) {
                dados.add(new BasicNameValuePair("subvendedor", parametrol));
            }
            if (tipo.equals("a")){
                dados.add(new BasicNameValuePair("subvendedor", parametrol));
                dados.add(new BasicNameValuePair("ativo", "1"));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(dados));
            final HttpResponse resposta = httpClient.execute(httpPost);
            final String retorno = EntityUtils.toString(resposta.getEntity());
            JSONObject reader = new JSONObject(retorno);
            JSONObject resultado = reader.getJSONObject("resultado");
            String status = resultado.getString("status");
            if (status.equals("logado")) {
                SharedPreferences pref = getSharedPreferences("LOGIN", 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("chunica", resultado.getString("chunica"));
                editor.apply();
                chunica = resultado.getString("chunica");
                if (tipo.equals("s")||tipo.equals("a")) {

                    final String nome_subvendedor = resultado.getString("subvendedor");
                    final String vendas_domingo = resultado.getString("domingo");
                    final String vendas_segunda = resultado.getString("segunda");
                    final String vendas_terca = resultado.getString("terca");
                    final String vendas_quarta = resultado.getString("quarta");
                    final String vendas_quinta = resultado.getString("quinta");
                    final String vendas_sexta = resultado.getString("sexta");
                    final String vendas_sabado = resultado.getString("sabado");
                    final String vendas_domingoc = resultado.getString("domingoc");
                    final String vendas_segundac = resultado.getString("segundac");
                    final String vendas_tercac = resultado.getString("tercac");
                    final String vendas_quartac = resultado.getString("quartac");
                    final String vendas_quintac = resultado.getString("quintac");
                    final String vendas_sextac = resultado.getString("sextac");
                    final String vendas_sabadoc = resultado.getString("sabadoc");
                    final String status_subvendedor = resultado.getString("status_subvendedor");
                    dismissDialog(Dialog_id);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LayoutInflater li = getLayoutInflater();
                            View view = li.inflate(R.layout.modal_subvendedor, null);
                            TextView nome = view.findViewById(R.id.txtN);
                            TextView domingo = view.findViewById(R.id.txtDom);
                            TextView segunda = view.findViewById(R.id.txtSeg);
                            TextView terca = view.findViewById(R.id.txtTer);
                            TextView quarta = view.findViewById(R.id.txtQua);
                            TextView quinta = view.findViewById(R.id.txtQui);
                            TextView sexta = view.findViewById(R.id.txtSex);
                            TextView sabado = view.findViewById(R.id.txtSab);
                            TextView domingoc = view.findViewById(R.id.txtDomC);
                            TextView segundac = view.findViewById(R.id.txtSegC);
                            TextView tercac = view.findViewById(R.id.txtTerC);
                            TextView quartac = view.findViewById(R.id.txtQuaC);
                            TextView quintac = view.findViewById(R.id.txtQuiC);
                            TextView sextac = view.findViewById(R.id.txtSexC);
                            TextView sabadoc = view.findViewById(R.id.txtSabC);
                            RadioButton rdVendas = view.findViewById(R.id.rdVendas);
                            RadioButton rdPromocao = view.findViewById(R.id.rdPromocao);

                            rdVendas.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View arg0) {
                                    alerta.dismiss();
                                    new Thread() {

                                        public void run() {
                                            postHttp("s",tmp, "v");
                                        }
                                    }.start();
                                }
                            });

                            rdPromocao.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View arg0) {
                                    alerta.dismiss();
                                    new Thread() {

                                        public void run() {
                                            postHttp("s",tmp, "p");
                                        }
                                    }.start();
                                }
                            });

                            if (parametroll.equals("v")) {
                                rdVendas.setChecked(true);
                            }
                            if (parametroll.equals("p")){
                                rdPromocao.setChecked(true);
                            }

                            nome.setText(nome_subvendedor);
                            domingo.setText(vendas_domingo);
                            segunda.setText(vendas_segunda);
                            terca.setText(vendas_terca);
                            quarta.setText(vendas_quarta);
                            quinta.setText(vendas_quinta);
                            sexta.setText(vendas_sexta);
                            sabado.setText(vendas_sabado);
                            domingoc.setText(vendas_domingoc);
                            segundac.setText(vendas_segundac);
                            tercac.setText(vendas_tercac);
                            quartac.setText(vendas_quartac);
                            quintac.setText(vendas_quintac);
                            sextac.setText(vendas_sextac);
                            sabadoc.setText(vendas_sabadoc);

                            TextView textView11 = view.findViewById(R.id.textView11);

                            Button btRelatorio = view.findViewById(R.id.btRelatorio);
                            ImageView imgFechar = view.findViewById(R.id.imageVfechar);
                            if (status_subvendedor.equals("ativo")) {
                                btRelatorio.setText(getResources().getString(R.string.botao_desativar_sub));
                                if (tipo.equals("a")){
                                    textView11.setVisibility(View.VISIBLE);
                                    textView11.setText(getResources().getString(R.string.log_reativado));
                                }
                            } else {
                                btRelatorio.setText(getResources().getString(R.string.botao_ativar_sub));
                                if (tipo.equals("a")) {
                                    textView11.setVisibility(View.VISIBLE);
                                    textView11.setText(getResources().getString(R.string.log_desativado));
                                }
                            }

                            imgFechar.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View arg0) {
                                    alerta.dismiss();
                                }
                            });

                            btRelatorio.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View arg0) {
                                    alerta.dismiss();
                                        new Thread() {
                                            public void run() {
                                                postHttp("a", parametrol, "");
                                            }
                                        }.start();
                                    }
                            });

                            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Relatorio_detalhado.this);
                            builder.setView(view);
                            builder.setCancelable(true);
                            alerta = builder.create();
                            alerta.show();
                        }
                    });
                }

                if (tipo.equals("g")) {

                    dismissDialog(Dialog_id);

                    JSONArray subvendedores = resultado.getJSONArray("subvendedores");
                    for (int i = 0; i < ((JSONArray) subvendedores).length(); i++) {
                        listdata.add(((JSONArray) subvendedores).getString(i));
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listView.setAdapter(listar_sub);
                        }
                    });
                }
            }else{
                modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"));
            }


        } catch (IOException | JSONException ignored) {
        }
    }

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
                            Intent intent = new Intent(Relatorio_detalhado.this, Inicio.class);
                            startActivity(intent);
                        }
                        if (acao.equals("logof")) {
                            Relatorio_detalhado.this.getSharedPreferences("LOGIN", 0).edit().clear().apply();
                            Intent intent = new Intent(Relatorio_detalhado.this, Login.class);
                            startActivity(intent);
                        }
                    }
                });

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Relatorio_detalhado.this);
                builder.setView(view);
                builder.setCancelable(false);
                alerta = builder.create();
                alerta.show();
            }
        });
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