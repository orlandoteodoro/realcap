package br.com.realcapreal.realcapvendas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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

public class Relatorios extends AppCompatActivity {

    public  boolean verificaConexao() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert conectivtyManager != null;
        conectado = conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected();
        return conectado;
    }

    private Toast toast;
    private long lastBackPressTime = 0;
    private Spinner spinner;
    private String server;
    private ConstraintLayout exibindo;
    private RadioGroup aguardando;
    private androidx.appcompat.app.AlertDialog alerta;
    private String post_link;
    private String codigo;
    private String chunica;
    private String modo;

    private Integer edicao_atual;
    private String edicao_relatorio;
    private TextView domingo;
    private TextView domingoc;
    private TextView segunda;
    private TextView segundac;
    private TextView terca;
    private TextView tercac;
    private TextView quarta;
    private TextView quartac;
    private TextView quinta;
    private TextView quintac;
    private TextView sexta;
    private TextView sextac;
    private TextView sabado;
    private TextView sabadoc;
    private TextView total;
    private EditText edtRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorios);

        //Checar ajuste
        SharedPreferences ajustes = getSharedPreferences("AJUSTAR_DISPOSITIVO", 0);
        boolean tipo_dispositivo = ajustes.getBoolean("ajuste", false);
        if(!tipo_dispositivo) {
            Intent intent = new Intent(Relatorios.this, Ajustes.class);
            startActivity(intent);
        }

        //Checar Login
        SharedPreferences prefs = getSharedPreferences("LOGIN", 0);
        boolean logado = prefs.getBoolean("logado", false);
        if (!logado){
            Intent intent = new Intent(Relatorios.this, Login.class);
            startActivity(intent);
        }else {
            server = getResources().getString(R.string.app_server);
            exibindo = findViewById(R.id.layRel);
            aguardando = findViewById(R.id.rdoCar);
            String nomeLogin = prefs.getString("nome", "0");
            codigo = prefs.getString("codigo", "0");
            chunica = prefs.getString("chunica", "0");
            modo = prefs.getString("tipo", "0");
            String nome = prefs.getString("nome", null);
            String regional = prefs.getString("regional", null);
            TextView txNome = findViewById(R.id.txtN);
            TextView txRegional = findViewById(R.id.txtR);
            txNome.setText(nome);
            txRegional.setText(regional);

            edicao_atual = 0;
            edicao_relatorio = "1";
            domingo = findViewById(R.id.txtDom);
            domingoc = findViewById(R.id.txtDomC);
            segunda = findViewById(R.id.txtSeg);
            segundac = findViewById(R.id.txtSegC);
            terca = findViewById(R.id.txtTer);
            tercac = findViewById(R.id.txtTerC);
            quarta = findViewById(R.id.txtQua);
            quartac = findViewById(R.id.txtQuaC);
            quinta = findViewById(R.id.txtQui);
            quintac = findViewById(R.id.txtQuiC);
            sexta = findViewById(R.id.txtSex);
            sextac = findViewById(R.id.txtSexC);
            sabado = findViewById(R.id.txtSab);
            sabadoc = findViewById(R.id.txtSabC);
            total = findViewById(R.id.txtTov);


            if(verificaConexao()) {
                new Thread() {

                    public void run() {
                        postHttp("g","0", "v");
                    }
                }.start();
            }else{
                modal(getResources().getString(R.string.ale_semconexao), getResources().getString(R.string.botao_ok),"0");
            }

            spinner = findViewById(R.id.spinner);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @SuppressLint("InflateParams")
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (verificaConexao()) {
                        final RadioButton rl_vendas = findViewById(R.id.rdVendas);
                        new Thread() {

                            public void run() {
                                if (rl_vendas.isChecked()) {
                                    postHttp("g", "1", "v");
                                }else{
                                    postHttp("g", "1", "p");
                                }
                            }
                        }.start();
                    } else {
                        modal(getResources().getString(R.string.ale_semconexao), getResources().getString(R.string.botao_ok), "0");
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    aguardando.setVisibility(View.VISIBLE);
                    exibindo.setVisibility(View.GONE);
                }
            });
        }
    }

    public void relatorio_detalhado (View view){
        Intent intent = new Intent(Relatorios.this, Relatorio_detalhado.class);
        Bundle bundle = new Bundle();
        bundle.putString("edicao", String.valueOf(edicao_relatorio));
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void relatorio_vendas(View view) {
        edicao_atual = 0;
        if (verificaConexao()) {
            new Thread() {

                public void run() {
                    postHttp("g", "1", "v");
                }
            }.start();
        } else {
            modal(getResources().getString(R.string.ale_semconexao), getResources().getString(R.string.botao_ok), "0");
        }
    }

    public void relatorio_promocao(View view) {
        edicao_atual = 0;
        if (verificaConexao()) {
            new Thread() {

                public void run() {
                    postHttp("g", "1", "p");
                }
            }.start();
        } else {
            modal(getResources().getString(R.string.ale_semconexao), getResources().getString(R.string.botao_ok), "0");
        }
    }

    //Envio de dados para o servidor
    public void postHttp(String tipo, String parametrol, String parametroll) {

        if (tipo.equals("g")){
            if (parametroll.equals("v")) {
                post_link = "relatorios/relatorio_geral.php";
            }
            if (parametroll.equals("p")) {
                post_link = "relatorios/relatorio_geral_promo.php";
            }
        }

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(server + post_link);

        try {
            if (tipo.equals("g")) {

                if (parametrol.equals("1")) {
                    edicao_relatorio = spinner.getSelectedItem().toString();
                }
                Integer edicao_checagem = Integer.parseInt(edicao_relatorio);

                if (!edicao_atual.equals(edicao_checagem)) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            aguardando.setVisibility(View.VISIBLE);
                            exibindo.setVisibility(View.GONE);
                        }
                    });

                    ArrayList<NameValuePair> dados = new ArrayList<>();
                    dados.add(new BasicNameValuePair("codigo", codigo));
                    dados.add(new BasicNameValuePair("chunica", chunica));
                    dados.add(new BasicNameValuePair("modo", modo));
                    if (parametrol.equals("1")) {
                        dados.add(new BasicNameValuePair("edicao", edicao_relatorio));
                    } else {
                        dados.add(new BasicNameValuePair("edicao", "0"));
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

                        edicao_atual = resultado.getInt("edicao_atual");

                        JSONArray edicoes = resultado.getJSONArray("edicoes");
                        String sDomingo = resultado.getString("domingo");
                        String sSegunda = resultado.getString("segunda");
                        String sTerca = resultado.getString("terca");
                        String sQuarta = resultado.getString("quarta");
                        String sQuinta = resultado.getString("quinta");
                        String sSexta = resultado.getString("sexta");
                        String sSabado = resultado.getString("sabado");

                        String sDomingoC = resultado.getString("domingoc");
                        String sSegundaC = resultado.getString("segundac");
                        String sTercaC = resultado.getString("tercac");
                        String sQuartaC = resultado.getString("quartac");
                        String sQuintaC = resultado.getString("quintac");
                        String sSextaC = resultado.getString("sextac");
                        String sSabadoC = resultado.getString("sabadoc");
                        String sTotal = resultado.getString("total");
                        String checar_sub = resultado.getString("subvendedor");

                        ArrayList<String> listdata = new ArrayList<String>();
                        for (int i = 0; i < ((JSONArray) edicoes).length(); i++) {
                            listdata.add(((JSONArray) edicoes).getString(i));
                        }
                        String edicao_atuals = resultado.getString("edicao_atual");
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                                listdata);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                aguardando.setVisibility(View.GONE);
                                exibindo.setVisibility(View.VISIBLE);

                                domingo.setText(sDomingo);
                                segunda.setText(sSegunda);
                                terca.setText(sTerca);
                                quarta.setText(sQuarta);
                                quinta.setText(sQuinta);
                                sexta.setText(sSexta);
                                sabado.setText(sSabado);

                                domingoc.setText(sDomingoC);
                                segundac.setText(sSegundaC);
                                tercac.setText(sTercaC);
                                quartac.setText(sQuartaC);
                                quintac.setText(sQuintaC);
                                sextac.setText(sSextaC);
                                sabadoc.setText(sSabadoC);
                                total.setText(sTotal);

                                Button btRelatorio = findViewById(R.id.btRelatorio);
                                if (checar_sub.equals("0")){
                                    btRelatorio.setVisibility(View.GONE);
                                }

                                spinner.setAdapter(spinnerArrayAdapter);
                                spinner.setSelection(getIndex(spinner, edicao_atuals));

                            }
                        });
                    } else {
                        modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"));
                    }
                }
            }
        } catch (IOException | JSONException ignored) {
        }
    }

    //setar estado no spiner
    private int getIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }
        return 0;
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
                            Intent intent = new Intent(Relatorios.this, Inicio.class);
                            startActivity(intent);
                        }
                        if (acao.equals("logof")) {
                            Relatorios.this.getSharedPreferences("LOGIN", 0).edit().clear().apply();
                            Intent intent = new Intent(Relatorios.this, Login.class);
                            startActivity(intent);
                        }
                    }
                });

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Relatorios.this);
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

    public void voltar (View view){
        Intent intent = new Intent(Relatorios.this, Inicio.class);
        startActivity(intent);
    }
}