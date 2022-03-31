package br.com.realcapreal.realcapvendas;

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
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
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
import java.io.IOException;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import br.com.paxbr.easypaymentpos.POSConfig;
import br.com.paxbr.easypaymentpos.callback.CallBackUser;
import br.com.paxbr.easypaymentpos.common.CvvStatus;
import br.com.paxbr.easypaymentpos.common.ProductFormatKt;
import br.com.paxbr.easypaymentpos.common.ReceiptEnum;
import br.com.paxbr.easypaymentpos.common.Status;
import br.com.paxbr.easypaymentpos.controller.ReceiptPOS;
import br.com.paxbr.easypaymentpos.controller.TransactionPOS;
import br.com.paxbr.easypaymentpos.controller.TransactionPOSKt;
import br.com.paxbr.easypaymentpos.domain.Card;
import br.com.paxbr.easypaymentpos.domain.CardContent;
import br.com.paxbr.easypaymentpos.domain.InstalmentsRange;
import br.com.paxbr.easypaymentpos.domain.Product;
import br.com.paxbr.easypaymentpos.domain.ReceiptContent;
import br.com.paxbr.easypaymentpos.domain.ResponseEnum;
import br.com.paxbr.easypaymentpos.domain.TransactionInformation;
import br.com.paxbr.easypaymentpos.domain.TransactionResult;
import br.com.paxbr.easypaymentpos.domain.TypeOfTransactionEnum;
import br.com.paxbr.easypaymentpos.domain.User;
import br.com.paxbr.easypaymentpos.util.SharedPreferencesUtilKt;
import br.com.setis.bibliotecapinpad.definicoes.Menu;
import br.com.realcapreal.realcapvendas.uteis.Mask;

import br.com.setis.bibliotecapinpad.definicoes.Menu;

public class Finalizar extends AppCompatActivity implements CallBackUser<Object> {

    public static ArrayList<String> reciboc;
    public static ArrayList<String> recibog;
    ArrayList<String> recibocf;
    ArrayList<String> recibogf;
    public static String edicao;
    public static Double valor;
    Double valord;
    String edicaof;
    private AlertDialog alerta;
    private String cliente;
    private EditText campo_ddd;
    private EditText campo_fone;
    private EditText nome;
    private EditText cpf;
    private EditText endereco;
    private EditText complemento;
    private EditText bairro;
    private EditText cidade;
    private Spinner estado;
    static final int Dialog_id = 1;

    String imei;
    String chip;
    String serial;

    private long lastBackPressTime = 0;
    private Toast toast;
    private String server;
    private String cidade_cl;

    private String post_link;
    private String codigo;
    private String chunica;
    private String modo;
    private String imagem_pix;
    private String autenticacao;
    private boolean promocao;

    private POSConfig config;
    private ProgressBar pgraguarde;
    private TextView txtmensagem;
    private Button btt1;
    private Button btt2;
    private Button btpg3;
    private Integer protp;
    private Boolean prot;
    private EditText edtCamp;
    private String valorlib;
    private String AUTH_CODE;
    private String PAN_MASKED;
    private String PRODUCT_SELECTED;
    private String CARD_BRAND;
    private String AMOUNT;
    private String TID;

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
        setContentView(R.layout.activity_finalizar);

        //Checar Login
        SharedPreferences prefs = getSharedPreferences("LOGIN", 0);
        boolean logado = prefs.getBoolean("logado", false);
        if (!logado){
            Intent intent = new Intent(Finalizar.this, Login.class);
            startActivity(intent);
        }else{
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(broadcastReceiver, filter);
            server = getResources().getString(R.string.app_server);
            valord = Finalizar.valor;
            edicaof = Finalizar.edicao;
            recibocf = Finalizar.reciboc;
            Finalizar.reciboc = null;
            recibogf = Finalizar.recibog;
            Finalizar.recibog = null;
            ScrollView scCadastro = findViewById(R.id.scrCadastro);
            scCadastro.setVisibility(View.GONE);

            SharedPreferences prefp = getSharedPreferences("PROMOCAO", 0);
            promocao = prefp.getBoolean("promocao", false);

            SharedPreferences prefd = getSharedPreferences("DADOS_MAQUINA", 0);
            imei = prefd.getString("imei", "0");
            chip = prefd.getString("chip", "0");
            serial = prefd.getString("serial", "0");

            String nomeLogin = prefs.getString("nome", "0");
            codigo = prefs.getString("codigo", "0");
            chunica = prefs.getString("chunica", "0");
            modo = prefs.getString("tipo", "0");

            final TextView textVnome = findViewById(R.id.txtVendedor);
            textVnome.setText(nomeLogin);

            campo_ddd = findViewById(R.id.edtDdd);
            campo_ddd.addTextChangedListener(Mask.insert("##", campo_ddd));
            campo_ddd.requestFocus();

            final EditText et = findViewById(R.id.edtCel);
            et.setOnFocusChangeListener(new View.OnFocusChangeListener()

            {
                @Override
                public void onFocusChange(View v, boolean hasFocus)
                {
                    if (!hasFocus){
                        if(verificaConexao()) {

                            nome = findViewById(R.id.edtNome);
                            cpf = findViewById(R.id.edtCpf);
                            endereco = findViewById(R.id.edtEndereco);
                            complemento = findViewById(R.id.edtComplemento);
                            bairro = findViewById(R.id.edtBairro);
                            cidade = findViewById(R.id.edtCidade);
                            estado = findViewById(R.id.spestado);

                            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                                    et.getWindowToken(), 0);
                            showDialog(Dialog_id);
                            new Thread() {

                                public void run() {
                                    postHttp("c", campo_ddd.getText().toString(), et.getText().toString());
                                }
                            }.start();
                        }else{
                            modal(getResources().getString(R.string.ale_semconexao), getResources().getString(R.string.botao_ok),"0", "");
                        }
                    }
                }
            });

            campo_fone = findViewById(R.id.edtCel);
            campo_fone.addTextChangedListener(Mask.insert("#####-####", campo_fone));

            final EditText campo_cpf = findViewById(R.id.edtCpf);
            campo_cpf.addTextChangedListener(Mask.insert("###.###.###-##", campo_cpf));

            final EditText campo_nome = findViewById(R.id.edtNome);
            campo_nome.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

            EditText campo_ende = findViewById(R.id.edtEndereco);
            campo_ende.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

            EditText campo_comp = findViewById(R.id.edtComplemento);
            campo_comp.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

            EditText campo_bairro = findViewById(R.id.edtBairro);
            campo_bairro.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

            EditText campo_cidade = findViewById(R.id.edtCidade);
            campo_cidade.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

            campo_ddd.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void afterTextChanged(Editable s) {
                    if (campo_ddd.length()==2){
                        campo_fone.requestFocus();
                    }
                }
            });

            campo_fone.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void afterTextChanged(Editable s) {
                    if (campo_fone.length()==10){
                        campo_nome.requestFocus();
                    }
                }
            });
            TextView txtVlr = findViewById(R.id.txtVlr);
            txtVlr.setText(getResources().getString(R.string.ven_valor) + NumberFormat.getCurrencyInstance().format(valord));

            ImageView buttonBarCodeScan = findViewById(R.id.imageVcartao);
            buttonBarCodeScan.setOnClickListener(view -> {
                venGsurf();
            });

            TextView textBarcodeSacan = findViewById(R.id.txtCartao);
            textBarcodeSacan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    venGsurf();
                }
            });
        }
    }

    //Envio de dados para o servidor
    public void postHttp(String tipo, String parametrol, String parametroll) {

        if (tipo.equals("c")){
            post_link = "checagens/checar_cadastro.php";
        }

        if (tipo.equals("s")){
            post_link = "spinners/spinner_cidades.php";
        }

        if (tipo.equals("p")){
            post_link = "vendas/venda_pix.php";
        }

        if (tipo.equals("d")){
            post_link = "vendas/venda_dinheiro.php";
        }

        if (tipo.equals("ct")){
            post_link = "vendas/venda_cartao.php";
        }

        if (tipo.equals("cc")){
            post_link = "vendas/venda_cartao.php";
        }

        if (tipo.equals("ch")){
            post_link = "checagens/checar_pix.php";
        }

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(server + post_link);

        try {
            if (tipo.equals("c")) {
                ArrayList<NameValuePair> dados = new ArrayList<>();
                dados.add(new BasicNameValuePair("codigo", codigo));
                dados.add(new BasicNameValuePair("chunica", chunica));
                dados.add(new BasicNameValuePair("modo", modo));
                dados.add(new BasicNameValuePair("ddd", parametrol));
                dados.add(new BasicNameValuePair("fone", parametroll));
                dados.add(new BasicNameValuePair("imei", imei));
                dados.add(new BasicNameValuePair("chip", chip));
                dados.add(new BasicNameValuePair("serial", serial));

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

                    if (!resultado.getString("cliente").equals("0")) {
                        cliente = resultado.getString("cliente");
                        cadastro(nome, resultado.getString("nome"));
                        cadastro(cpf, resultado.getString("cpf"));
                        cadastro(endereco, resultado.getString("endereco"));
                        cadastro(complemento, resultado.getString("complemento"));
                        cadastro(bairro, resultado.getString("bairro"));
                        cadastro(cidade, resultado.getString("cidade"));
                        exibircad();
                        dismissDialog(Dialog_id);
                    }else{
                        cliente = "0";
                        exibircad();
                        dismissDialog(Dialog_id);
                    }
                }else{
                    dismissDialog(Dialog_id);
                    modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"), "");
                }
            }

            if (tipo.equals("p")){

                ArrayList<NameValuePair> dados = new ArrayList<>();
                dados.add(new BasicNameValuePair("codigo", codigo));
                dados.add(new BasicNameValuePair("chunica", chunica));
                dados.add(new BasicNameValuePair("modo", modo));
                dados.add(new BasicNameValuePair("cliente", cliente));
                dados.add(new BasicNameValuePair("ddd", campo_ddd.getText().toString()));
                dados.add(new BasicNameValuePair("fone", campo_fone.getText().toString()));
                dados.add(new BasicNameValuePair("nome", removerAcentos(nome.getText().toString())));
                dados.add(new BasicNameValuePair("cpf", cpf.getText().toString()));
                dados.add(new BasicNameValuePair("endereco", removerAcentos(endereco.getText().toString())));
                dados.add(new BasicNameValuePair("complemento", removerAcentos(complemento.getText().toString())));
                dados.add(new BasicNameValuePair("bairro", removerAcentos(bairro.getText().toString())));
                dados.add(new BasicNameValuePair("cidade", removerAcentos(cidade.getText().toString())));
                dados.add(new BasicNameValuePair("imei", imei));
                dados.add(new BasicNameValuePair("chip", chip));
                dados.add(new BasicNameValuePair("serial", serial));

                if(promocao){
                    dados.add(new BasicNameValuePair("promocao", "1"));
                }else{
                    dados.add(new BasicNameValuePair("promocao", "0"));
                }
                dados.add(new BasicNameValuePair("edicao", edicaof));
                String[] reciboC = new String[recibocf.size()];
                reciboC = recibocf.toArray(reciboC);
                String[] reciboG = new String[recibogf.size()];
                reciboG = recibogf.toArray(reciboG);

                for (String s : reciboC) {
                    dados.add(new BasicNameValuePair("reciboC[]", s));
                }
                for (String s : reciboG) {
                    dados.add(new BasicNameValuePair("reciboG[]", s));
                }

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

                    if (resultado.getString("acao").equals("exibir_pix")) {
                        dismissDialog(Dialog_id);
                        imagem_pix = resultado.getString("imagem_pix");
                        autenticacao = resultado.getString("autenticacao");
                        modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_checar_pix), resultado.getString("acao"), resultado.getString("imagem_pix"));
                    }else{
                        dismissDialog(Dialog_id);
                        //modal(getResources().getString(R.string.ale_semconexao), getResources().getString(R.string.botao_ok),"inicio", "");
                        modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok),resultado.getString("acao"), "");
                    }
                }else{
                    dismissDialog(Dialog_id);
                    modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"), "");
                }
            }

            if (tipo.equals("d")){

                ArrayList<NameValuePair> dados = new ArrayList<>();
                dados.add(new BasicNameValuePair("codigo", codigo));
                dados.add(new BasicNameValuePair("chunica", chunica));
                dados.add(new BasicNameValuePair("modo", modo));
                dados.add(new BasicNameValuePair("cliente", cliente));
                dados.add(new BasicNameValuePair("ddd", campo_ddd.getText().toString()));
                dados.add(new BasicNameValuePair("fone", campo_fone.getText().toString()));
                dados.add(new BasicNameValuePair("nome", removerAcentos(nome.getText().toString())));
                dados.add(new BasicNameValuePair("cpf", cpf.getText().toString()));
                dados.add(new BasicNameValuePair("endereco", removerAcentos(endereco.getText().toString())));
                dados.add(new BasicNameValuePair("complemento", removerAcentos(complemento.getText().toString())));
                dados.add(new BasicNameValuePair("bairro", removerAcentos(bairro.getText().toString())));
                dados.add(new BasicNameValuePair("cidade", removerAcentos(cidade.getText().toString())));
                dados.add(new BasicNameValuePair("imei", imei));
                dados.add(new BasicNameValuePair("chip", chip));
                dados.add(new BasicNameValuePair("serial", serial));

                if(promocao){
                    dados.add(new BasicNameValuePair("promocao", "1"));
                }else{
                    dados.add(new BasicNameValuePair("promocao", "0"));
                }
                dados.add(new BasicNameValuePair("edicao", edicaof));
                String[] reciboC = new String[recibocf.size()];
                reciboC = recibocf.toArray(reciboC);
                String[] reciboG = new String[recibogf.size()];
                reciboG = recibogf.toArray(reciboG);

                for (String s : reciboC) {
                    dados.add(new BasicNameValuePair("reciboC[]", s));
                }
                for (String s : reciboG) {
                    dados.add(new BasicNameValuePair("reciboG[]", s));
                }

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

                        Intent intent = new Intent(Finalizar.this, Impressao.class);
                        Bundle bundle = new Bundle();

                        bundle.putString("ddd", resultado.getString("ddd"));
                        bundle.putString("celular", resultado.getString("fone"));
                        bundle.putString("cliente", resultado.getString("nome"));
                        bundle.putString("datahora", resultado.getString("datahora"));
                        bundle.putString("edicao", edicaof);
                        bundle.putDouble("valor", valord);
                        bundle.putString("autenticacao", resultado.getString("autenticacao"));
                        bundle.putBoolean("reimpressao", false);
                        bundle.putBoolean("impressao", true);
                        bundle.putString("tipo", "Dinheiro");
                        if (!promocao) {
                            Impressao.recibocf = recibocf;
                            Impressao.recibogf = recibogf;
                        } else {
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
                        }
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        dismissDialog(Dialog_id);
                        SharedPreferences pre = getSharedPreferences("LOGIN", 0);
                        SharedPreferences.Editor editore = pre.edit();
                        editore.putString("chunica", resultado.getString("chunica"));
                        editore.apply();
                        chunica = resultado.getString("chunica");
                        modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"), "");
                    }
                }else{
                    dismissDialog(Dialog_id);
                    modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"), "");
                }
            }

            if (tipo.equals("ct")){

                ArrayList<NameValuePair> dados = new ArrayList<>();
                dados.add(new BasicNameValuePair("codigo", codigo));
                dados.add(new BasicNameValuePair("chunica", chunica));
                dados.add(new BasicNameValuePair("modo", modo));
                dados.add(new BasicNameValuePair("cliente", cliente));
                dados.add(new BasicNameValuePair("ddd", campo_ddd.getText().toString()));
                dados.add(new BasicNameValuePair("fone", campo_fone.getText().toString()));
                dados.add(new BasicNameValuePair("nome", removerAcentos(nome.getText().toString())));
                dados.add(new BasicNameValuePair("cpf", cpf.getText().toString()));
                dados.add(new BasicNameValuePair("endereco", removerAcentos(endereco.getText().toString())));
                dados.add(new BasicNameValuePair("complemento", removerAcentos(complemento.getText().toString())));
                dados.add(new BasicNameValuePair("bairro", removerAcentos(bairro.getText().toString())));
                dados.add(new BasicNameValuePair("cidade", removerAcentos(cidade.getText().toString())));
                dados.add(new BasicNameValuePair("imei", imei));
                dados.add(new BasicNameValuePair("chip", chip));
                dados.add(new BasicNameValuePair("serial", serial));

                if(promocao){
                    dados.add(new BasicNameValuePair("promocao", "1"));
                }else{
                    dados.add(new BasicNameValuePair("promocao", "0"));
                }
                dados.add(new BasicNameValuePair("edicao", edicaof));
                String[] reciboC = new String[recibocf.size()];
                reciboC = recibocf.toArray(reciboC);
                String[] reciboG = new String[recibogf.size()];
                reciboG = recibogf.toArray(reciboG);

                for (String s : reciboC) {
                    dados.add(new BasicNameValuePair("reciboC[]", s));
                }
                for (String s : reciboG) {
                    dados.add(new BasicNameValuePair("reciboG[]", s));
                }

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

                    if (resultado.getString("acao").equals("efetuar_cobranca")) {
                        dismissDialog(Dialog_id);
                        autenticacao = resultado.getString("autenticacao");
                        SharedPreferences pret = getSharedPreferences("TRANSACAO_PENDENTE", 0);
                        SharedPreferences.Editor editor_transacao = pret.edit();
                        editor_transacao.putBoolean("pendente", true);
                        editor_transacao.putString("cliente", cliente);
                        editor_transacao.putString("autenticacao", autenticacao);
                        editor_transacao.putString("ddd", campo_ddd.getText().toString());
                        editor_transacao.putString("fone", campo_fone.getText().toString());
                        editor_transacao.putString("nome", removerAcentos(nome.getText().toString()));
                        editor_transacao.putString("cpf", cpf.getText().toString());
                        editor_transacao.putString("endereco", removerAcentos(endereco.getText().toString()));
                        editor_transacao.putString("complemento", removerAcentos(complemento.getText().toString()));
                        editor_transacao.putString("bairro", removerAcentos(bairro.getText().toString()));
                        editor_transacao.putString("cidade", removerAcentos(cidade.getText().toString()));
                        editor_transacao.putString("reciboc", String.valueOf(recibocf));
                        editor_transacao.putString("recibog", String.valueOf(recibogf));
                        editor_transacao.apply();

                        venGsurf();
                    }else{
                        dismissDialog(Dialog_id);
                        //modal(getResources().getString(R.string.ale_semconexao), getResources().getString(R.string.botao_ok),"inicio", "");
                        modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok),resultado.getString("acao"), "");
                    }
                }else{
                    dismissDialog(Dialog_id);
                    modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"), "");
                }
            }

            if (tipo.equals("cc")){

                ArrayList<NameValuePair> dados = new ArrayList<>();
                dados.add(new BasicNameValuePair("codigo", codigo));
                dados.add(new BasicNameValuePair("chunica", chunica));
                dados.add(new BasicNameValuePair("modo", modo));
                dados.add(new BasicNameValuePair("cliente", cliente));
                dados.add(new BasicNameValuePair("ddd", campo_ddd.getText().toString()));
                dados.add(new BasicNameValuePair("fone", campo_fone.getText().toString()));
                dados.add(new BasicNameValuePair("nome", removerAcentos(nome.getText().toString())));
                dados.add(new BasicNameValuePair("cpf", cpf.getText().toString()));
                dados.add(new BasicNameValuePair("endereco", removerAcentos(endereco.getText().toString())));
                dados.add(new BasicNameValuePair("complemento", removerAcentos(complemento.getText().toString())));
                dados.add(new BasicNameValuePair("bairro", removerAcentos(bairro.getText().toString())));
                dados.add(new BasicNameValuePair("cidade", removerAcentos(cidade.getText().toString())));
                dados.add(new BasicNameValuePair("PAN_MASKED", PAN_MASKED));
                dados.add(new BasicNameValuePair("PRODUCT_SELECTED", PRODUCT_SELECTED));
                dados.add(new BasicNameValuePair("AUTH_CODE", AUTH_CODE));
                dados.add(new BasicNameValuePair("CARD_BRAND", CARD_BRAND));
                dados.add(new BasicNameValuePair("AMOUNT", AMOUNT));
                dados.add(new BasicNameValuePair("TID", TID));
                dados.add(new BasicNameValuePair("imei", imei));
                dados.add(new BasicNameValuePair("chip", chip));
                dados.add(new BasicNameValuePair("serial", serial));

                if(promocao){
                    dados.add(new BasicNameValuePair("promocao", "1"));
                }else{
                    dados.add(new BasicNameValuePair("promocao", "0"));
                }
                dados.add(new BasicNameValuePair("edicao", edicaof));
                String[] reciboC = new String[recibocf.size()];
                reciboC = recibocf.toArray(reciboC);
                String[] reciboG = new String[recibogf.size()];
                reciboG = recibogf.toArray(reciboG);

                for (String s : reciboC) {
                    dados.add(new BasicNameValuePair("reciboC[]", s));
                }
                for (String s : reciboG) {
                    dados.add(new BasicNameValuePair("reciboG[]", s));
                }

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

                        Finalizar.this.getSharedPreferences("TRANSACAO_PENDENTE", 0).edit().clear().apply();

                        Intent intent = new Intent(Finalizar.this, Impressao.class);
                        Bundle bundle = new Bundle();

                        bundle.putString("ddd", resultado.getString("ddd"));
                        bundle.putString("celular", resultado.getString("fone"));
                        bundle.putString("cliente", resultado.getString("nome"));
                        bundle.putString("datahora", resultado.getString("datahora"));
                        bundle.putString("edicao", edicaof);
                        bundle.putDouble("valor", valord);
                        bundle.putString("autenticacao", resultado.getString("autenticacao"));
                        bundle.putBoolean("reimpressao", false);
                        bundle.putBoolean("impressao", true);
                        bundle.putString("tipo", "Cartao");
                        if (!promocao) {
                            Impressao.recibocf = recibocf;
                            Impressao.recibogf = recibogf;
                        } else {
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
                        }
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        dismissDialog(Dialog_id);
                        SharedPreferences pre = getSharedPreferences("LOGIN", 0);
                        SharedPreferences.Editor editore = pre.edit();
                        editore.putString("chunica", resultado.getString("chunica"));
                        editore.apply();
                        chunica = resultado.getString("chunica");
                        modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"), "");
                    }
                }else{
                    dismissDialog(Dialog_id);
                    modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"), "");
                }
            }

            if (tipo.equals("ch")){
                ArrayList<NameValuePair> dados = new ArrayList<>();
                dados.add(new BasicNameValuePair("codigo", codigo));
                dados.add(new BasicNameValuePair("chunica", chunica));
                dados.add(new BasicNameValuePair("modo", modo));
                dados.add(new BasicNameValuePair("autenticacao", autenticacao));
                if(promocao){
                    dados.add(new BasicNameValuePair("promocao", "1"));
                }else{
                    dados.add(new BasicNameValuePair("promocao", "0"));
                }

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
                    if (resultado.getString("status_pagamento").equals("confirmado")) {
                        Intent intent = new Intent(Finalizar.this, Impressao.class);
                        Bundle bundle = new Bundle();

                        bundle.putString("ddd", resultado.getString("ddd"));
                        bundle.putString("celular", resultado.getString("fone"));
                        bundle.putString("cliente", resultado.getString("nome"));
                        bundle.putString("datahora", resultado.getString("datahora"));
                        bundle.putString("edicao", edicaof);
                        bundle.putDouble("valor", valord);
                        bundle.putString("autenticacao", autenticacao);
                        bundle.putBoolean("reimpressao", false);
                        bundle.putBoolean("impressao", true);
                        bundle.putString("tipo", "PIX");
                        if (!promocao) {
                            Impressao.recibocf = recibocf;
                            Impressao.recibogf = recibogf;
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
                            Impressao.recibocf = recibos_contribuicao;
                            Impressao.recibogf = recibos_gratis;
                        }
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_checar_pix), resultado.getString("acao"), imagem_pix);
                    }
                }else{
                    dismissDialog(Dialog_id);
                    modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"), "");
                }
            }
        } catch (IOException | JSONException ignored) {
        }
    }

    public void cadastro (EditText edvar, String texto){
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                //edvar.setFocusableInTouchMode(false);
                //edvar.clearFocus();
                edvar.setText(texto);
                //edvar.setEnabled(false);
                //estado.setEnabled(false);
            }
        });
    }

    public void exibircad(){
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                RadioGroup rdAguarde = findViewById(R.id.rdgAguarde);
                rdAguarde.setVisibility(View.GONE);

                ScrollView scCadastro = findViewById(R.id.scrCadastro);
                scCadastro.setVisibility(View.VISIBLE);
            }
        });
    }


    //exibição de alertas
    public void aguarde(String string){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (string.equals("aguarde")) {
                    showDialog(Dialog_id);
                }else{
                    dismissDialog(Dialog_id);
                }
            }
        });
    }

    public void modal_duplo(String txtAlerta, String tipo){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater li = getLayoutInflater();

                View view = li.inflate(R.layout.modal_confirma, null);
                TextView aviCaD = view.findViewById(R.id.txAlerta);
                Button btAvi = view.findViewById(R.id.btAlerta);
                aviCaD.setText(txtAlerta);

                if (!tipo.equals("fc")) {

                    view.findViewById(R.id.btAlertal).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View arg0) {
                            alerta.dismiss();
                            aguarde("aguarde");
                            new Thread() {
                                public void run() {
                                    postHttp(tipo, "0", "0");
                                }
                            }.start();
                        }
                    });
                    view.findViewById(R.id.btAlertall).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View arg0) {
                            alerta.dismiss();
                        }
                    });
                }
                if (tipo.equals("fc")){
                    view.findViewById(R.id.btAlertal).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View arg0) {
                            alerta.dismiss();
                        }
                    });
                    view.findViewById(R.id.btAlertall).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View arg0) {
                            alerta.dismiss();
                            Intent intent = new Intent(Finalizar.this, Inicio.class);
                            startActivity(intent);
                        }
                    });
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(Finalizar.this);
                builder.setView(view);
                builder.setCancelable(false);
                alerta = builder.create();
                alerta.show();
            }
        });
    }

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

                    Picasso.with(Finalizar.this)
                            .load(img_pix)
                            .into(qr_code_pix);

                    aviCaD.setText(txtAlerta);
                }

                view.findViewById(R.id.btAlerta).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        alerta.dismiss();
                        if (acao.equals("reload")) {
                            Intent intent = new Intent(Finalizar.this, Carrinho.class);
                            startActivity(intent);
                        }
                        if (acao.equals("deslogar")) {
                            Finalizar.this.getSharedPreferences("LOGIN", 0).edit().clear().apply();
                            Intent intent = new Intent(Finalizar.this, Login.class);
                            startActivity(intent);
                        }
                        if (acao.equals("inicio")) {
                            Intent intent = new Intent(Finalizar.this, Inicio.class);
                            startActivity(intent);
                        }

                        if (acao.equals("exibir_pix")){
                            aguarde("aguarde");
                            new Thread() {
                                public void run() {
                                    postHttp("ch", "0", "0");
                                }
                            }.start();
                        }
                    }
                });

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Finalizar.this);
                builder.setView(view);
                builder.setCancelable(false);
                alerta = builder.create();
                alerta.show();
            }
        });
    }
    //fim

    //Cancelar venda
    public void cancelar (View view){
        modal(getResources().getString(R.string.ale_cancelarvenda), getResources().getString(R.string.botao_ok), "inicio", "");
    }

    //Venda a dinheiro
    public void venDin(View view){
        modal_duplo(getResources().getString(R.string.ale_confirmardinheiro), "d");
    }

    //Venda pix
    public void venPix(View view){
        modal_duplo(getResources().getString(R.string.ale_confirmarpix), "p");
    }

    public static String removerAcentos(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    //Operação de cartão
    public void venGsurf (){
        LayoutInflater li = getLayoutInflater();
        View view = li.inflate(R.layout.modal_pagamento, null);
        TextView valor = view.findViewById((R.id.txtValor));
        valor.setText(NumberFormat.getCurrencyInstance().format(valord));
        txtmensagem = view.findViewById(R.id.txtMensagem);
        pgraguarde = view.findViewById(R.id.progressBar);
        edtCamp = view.findViewById(R.id.edtCamp);
        btpg3 = view.findViewById(R.id.btpg3);
        btt1 = view.findViewById(R.id.btpg1);
        btt2 = view.findViewById(R.id.btpg2);
        view.findViewById(R.id.btpg1).setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                protp   = 1;
                btt1.setVisibility(View.GONE);
                btt2.setVisibility(View.GONE);
            }
        });
        view.findViewById(R.id.btpg2).setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                protp   = 2;
                btt1.setVisibility(View.GONE);
                btt2.setVisibility(View.GONE);
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(Finalizar.this);
        builder.setView(view);
        builder.setCancelable(false);
        alerta = builder.create();
        alerta.show();
        transact();
    }

    //Pagamento cartão LIB
    private void transact() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                config = POSConfig.Companion.getInstance();
                User user = new User("23544429000145", "https://52.191.175.100:1447"); //PRODUÇÃO
                //User user = new User("11660068754", "https://52.168.167.13"); //HOMOLOGAÇÃO

                config.initialize(Finalizar.this, user, new CallBackUser<Status>() {
                    @Override
                    public void onRequest(Status status) {

                        config.register(Finalizar.this);
                        final boolean splitMode = getIntent().getBooleanExtra("split", false);
                        TransactionInformation transactionInformation = new TransactionInformation(new Locale("pt", "BR"), splitMode);//Sua localidade
                        final TransactionPOS transaction = new TransactionPOS(Finalizar.this, transactionInformation);
                        transaction.start(new CallBackUser<Status>() {
                            @Override
                            public void onRequest(Status status) {
                                if (status == Status.APPROVED) {
                                    TransactionResult transactionResult = transaction.getTransactionResult();//Informacoes da ultima transacao
                                    assert transactionResult != null;

                                    PAN_MASKED = transaction.getTransactionResult().getPanMasked();
                                    PRODUCT_SELECTED = transaction.getTransactionResult().getProductSelected();
                                    AUTH_CODE = transaction.getTransactionResult().getAuthCode();
                                    CARD_BRAND = transaction.getTransactionResult().getCardBrand();
                                    AMOUNT = transaction.getTransactionResult().getAmount();
                                    TID = transaction.getTransactionResult().getTid();

                                    SharedPreferences pret = getSharedPreferences("TRANSACAO_PENDENTE", 0);
                                    SharedPreferences.Editor editor_transacao = pret.edit();
                                    editor_transacao.putBoolean("pendente", true);
                                    editor_transacao.putString("cliente", cliente);
                                    editor_transacao.putString("ddd", campo_ddd.getText().toString());
                                    editor_transacao.putString("fone", campo_fone.getText().toString());
                                    editor_transacao.putString("nome", removerAcentos(nome.getText().toString()));
                                    editor_transacao.putString("cpf", cpf.getText().toString());
                                    editor_transacao.putString("endereco", removerAcentos(endereco.getText().toString()));
                                    editor_transacao.putString("complemento", removerAcentos(complemento.getText().toString()));
                                    editor_transacao.putString("bairro", removerAcentos(bairro.getText().toString()));
                                    editor_transacao.putString("cidade", removerAcentos(cidade.getText().toString()));
                                    editor_transacao.putString("reciboc", String.valueOf(recibocf));
                                    editor_transacao.putString("recibog", String.valueOf(recibogf));
                                    editor_transacao.putString("PAN_MASKED", PAN_MASKED);
                                    editor_transacao.putString("PRODUCT_SELECTED", PRODUCT_SELECTED);
                                    editor_transacao.putString("AUTH_CODE", AUTH_CODE);
                                    editor_transacao.putString("CARD_BRAND", CARD_BRAND);
                                    editor_transacao.putString("AMOUNT", AMOUNT);
                                    editor_transacao.putString("TID", TID);
                                    editor_transacao.apply();

                                    new Thread() {
                                        public void run() {
                                            postHttp("cc", "0", "0");
                                        }
                                    }.start();

                                } else {
                                    alerta.dismiss();
                                    Finalizar.this.getSharedPreferences("TRANSACAO_PENDENTE", 0).edit().clear().apply();
                                    modal_duplo(getResources().getString(R.string.ven_cartfalha), "fc");
                                }
                            }
                        });

                    }
                });
            }
        });

    }
    /**
     * Solicitaçao de dados dinamicos e feedbacks da transacao
     */
    @Override
    public void onRequest(final Object o) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (o instanceof TransactionPOS.POSObject) {
                    TransactionPOS.POSObject posObject = (TransactionPOS.POSObject) o;
                    switch (posObject.getPosInteraction()) {
                        case INSERT_AMOUNT:
                            valorlib = String.valueOf(valord)+"0";
                            Long valorLong = Long.parseLong(valorlib.replaceAll("\\D+",""));
                            config.response(valorLong);
                            break;

                        case SHOW_MESSAGE:
                            String alerta = posObject.toString().toLowerCase().substring(0, 2);
                            switch (alerta) {
                                case "pr":
                                    pgraguarde.setVisibility(View.VISIBLE);
                                    txtmensagem.setText("Processando...");
                                    break;
                                case "ag":
                                    pgraguarde.setVisibility(View.GONE);
                                    txtmensagem.setText("Digite sua senha");
                                    break;
                                case "re":
                                    pgraguarde.setVisibility(View.GONE);
                                    txtmensagem.setText("Retire seu cartão");
                                    break;
                                default:
                                    txtmensagem.setText(posObject.toString());
                                    break;
                            }
                            break;

                        case LAST_DIGITS:
                            edtCamp.setVisibility(View.VISIBLE);
                            txtmensagem.setText("Digite os 4 últimos digitos do cartão");
                            break;

                        case REQUEST_CVV:
                            txtmensagem.setText("Digite o CVV do cartão");
                            edtCamp.setVisibility(View.VISIBLE);

                            boolean needOptionCVV = (boolean) posObject.getAny();
                            CardContent.Cvv cvv = null;
                            if (!needOptionCVV) {
                                cvv = new CardContent.Cvv();
                                cvv.setCvvStatus(CvvStatus.NO_EXISTS);
                            } else {
                                txtmensagem.setText("Digite o CVV do Cartão");
                            }
                            break;

                        case PRINT_RECEIPT:
                            config.response(ResponseEnum.OK);
                            break;

                        case SELECT_APPLICATION:
                            txtmensagem.setText("Selecione");
                            pgraguarde.setVisibility(View.GONE);

                            Menu menu = (Menu) posObject.getAny();
                            assert menu != null;
                            List<String> applications = menu.obtemOpcoesMenu();

                            if (applications.size()>1) {
                                btt1.setVisibility(View.VISIBLE);
                                btt2.setVisibility(View.VISIBLE);
                                btt1.setText(applications.get(0));
                                btt2.setText(applications.get(1));
                                btt1.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View arg0) {
                                        config.select(1);
                                        btt1.setVisibility(View.GONE);
                                        btt2.setVisibility(View.GONE);
                                    }
                                });
                                btt2.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View arg0) {
                                        config.select(2);
                                        btt1.setVisibility(View.GONE);
                                        btt2.setVisibility(View.GONE);
                                    }
                                });
                            }else{
                                config.select(1);
                            }
                            break;

                        case SELECT_PRODUCT:
                            List<Product> products = (List<Product>) posObject.getAny();
                            assert products != null;
                            Set<TypeOfTransactionEnum> types = ProductFormatKt.types(products);
                            final List<Product> products1 = ProductFormatKt.filterType(products, types.iterator().next());
                            config.response(products1.get(0));
                            break;

                        case FALLBACK:
                            pgraguarde.setVisibility(View.GONE);
                            String alertas = posObject.toString();
                            if (alertas.equals("Passe o cartão")){
                                txtmensagem.setText(alertas);
                                prot = true;
                            }
                            break;

                        case STRIPE_READ:
                            txtmensagem.setText("passe o cartão");
                            break;

                        case CARD_NUMBER:
                            pgraguarde.setVisibility(View.GONE);
                            edtCamp.setVisibility(View.VISIBLE);
                            txtmensagem.setText("Digite o Número do Cartão");
                            break;

                        case CARD_REQUEST:
                            pgraguarde.setVisibility(View.GONE);
                            txtmensagem.setText("Insira ou Passe o cartao");
                            break;

                        case INSTALLMENTS:
                            txtmensagem.setText("Digite a Quantidade de Parcelas");
                            InstalmentsRange range = (InstalmentsRange) posObject.getAny(); //minimo e maximo de parcelas permitido
                            assert range != null;
                            range.getRange().getMax();//Max installment range
                            range.getRange().getMin();//Min installment range
                            config.response("1");
                            break;

                        case PROCESSING:
                            txtmensagem.setText("Processando");
                            break;

                        case OPERATION_REJECTED:
                            pgraguarde.setVisibility(View.GONE);
                            txtmensagem.setText("Transacao Rejeitada");
                            break;

                        case OPERATION_APPROVED:
                            pgraguarde.setVisibility(View.GONE);
                            txtmensagem.setText("Transacao Aprovada");
                            break;

                        case REMOVE_CARD:
                            pgraguarde.setVisibility(View.GONE);
                            txtmensagem.setText("Retire o Cartão");
                            break;

                        case REQUEST_EXPIRATION_DATE:
                            txtmensagem.setText("Digite a Data de Validade do Cartão");
                            break;

                        case TRANSACTION_STARTED:
                            pgraguarde.setVisibility(View.VISIBLE);
                            txtmensagem.setText("Iniciando Transação, aguarde ...");
                            break;

                        case SENDING_TRANSACTION:
                            txtmensagem.setText("Enviado Transação, aguarde ...");
                            pgraguarde.setVisibility(View.VISIBLE);
                            break;

                        case PIN_INSERTED:
                            txtmensagem.setText("Digite sua senha");
                            break;

                        default:
                            Toast.makeText(Finalizar.this, posObject.getPosInteraction().toString(), Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            Log.e ("CONNECTION","hasconnection:"+(netInfo != null && netInfo.isConnected()));
        }
    };

    //FIM

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