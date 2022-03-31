package br.com.realcapreal.realcapvendas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pax.dal.IDAL;
import com.pax.dal.entity.EFontTypeAscii;
import com.pax.dal.entity.EFontTypeExtCode;
import com.pax.neptunelite.api.NeptuneLiteUser;

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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Objects;

import br.com.realcapreal.realcapvendas.uteis.PrinterTester;


public class Impressao extends AppCompatActivity {

    static final int Dialog_id = 1;
    private Boolean impressao;

    protected Dialog onCreateDialog(int id) {
        if (id == Dialog_id) {
            ProgressDialog pd = new ProgressDialog(this, R.style.styleProgressDialog);
            Objects.requireNonNull(pd.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            pd.setCancelable(false);
            return pd;
        }
        return null;
    }

    public static ArrayList<String> recibocf;
    public static ArrayList<String> recibogf;
    ArrayList<String> reciboc;
    ArrayList<String> recibog;
    private String server;
    private String codigo;
    private String chunica;
    private String modo;
    private String post_link;
    private String autenticacao_sms;
    private Button bInvoice;
    public static IDAL idal = null;

    private Toast toast;
    private long lastBackPressTime = 0;
    private AlertDialog alerta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impressao);

        try {
            idal = NeptuneLiteUser.getInstance().getDal(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == idal) {
            Toast.makeText(Impressao.this, "error occurred,DAL is null.", Toast.LENGTH_LONG).show();
        }
        server = getResources().getString(R.string.app_server);

        //Checar Login
        SharedPreferences prefs = getSharedPreferences("LOGIN", 0);
        boolean logado = prefs.getBoolean("logado", false);
        if (!logado) {
            Intent intent = new Intent(Impressao.this, Login.class);
            startActivity(intent);
        } else {

            codigo = prefs.getString("codigo", "0");
            chunica = prefs.getString("chunica", "0");
            modo = prefs.getString("tipo", "0");

            reciboc = Impressao.recibocf;
            Impressao.recibocf = null;
            recibog = Impressao.recibogf;
            Impressao.recibogf = null;

            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            assert bundle != null;
            impressao = bundle.getBoolean("impressao", false);
            final String dddT = bundle.getString("ddd");
            final String fonT = bundle.getString("celular");
            final String nomeC = bundle.getString("cliente");
            String foneC = "(" + dddT + ") " + fonT;
            String edicC = bundle.getString("edicao");
            String auteC = bundle.getString("autenticacao");
            autenticacao_sms = bundle.getString("autenticacao");
            String[] reciboC = new String[reciboc.size()];
            reciboC = reciboc.toArray(reciboC);
            String[] reciboG = new String[recibog.size()];
            reciboG = recibog.toArray(reciboG);

            String recibo_contribuicao = "";
            String recibo_gratis = "";

            for (String s : reciboC) {
                recibo_contribuicao = recibo_contribuicao + "\n" + s;
            }
            for (String s : reciboG) {
                recibo_gratis = recibo_gratis + "\n" + s;
            }

            TextView nomeCtv = findViewById(R.id.textVimpCli);
            nomeCtv.setText(nomeC);
            TextView foneCtv = findViewById(R.id.textVimpFon);
            foneCtv.setText(foneC);
            TextView edicCtv = findViewById(R.id.textVimpEdi);
            edicCtv.setText(edicC);
            TextView cartCtv = findViewById(R.id.textVimpCar);
            cartCtv.setText(recibo_contribuicao);
            TextView cargCtv = findViewById(R.id.textVimpCag);
            cargCtv.setText(recibo_gratis);
            TextView autenT = findViewById(R.id.textVimpAut);
            autenT.setText(auteC);

            Button btnPrint = findViewById(R.id.btnPrint);

            btnPrint.setOnClickListener(view -> {
                btnPrint.setVisibility(View.GONE);
                Imp ();
            });

            if (!impressao) {
                Button btnPrintr = findViewById(R.id.btnPrintr);
                btnPrintr.setVisibility(View.VISIBLE);
                btnPrintr.setEnabled(false);
                btnPrint.setVisibility(View.GONE);
            }
        }
    }

    public static String removerAcentos(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    //Impressão
    public void Imp () {
        new Thread(new Runnable() {
            public void run() {
                Intent intent = getIntent();
                Bundle bundle = intent.getExtras();
                assert bundle != null;
                final String dddT = bundle.getString("ddd");
                final String fonT = bundle.getString("celular");
                String nomeC = bundle.getString("cliente");
                String foneC = "("+dddT+") "+fonT;
                String edicC = bundle.getString("edicao");
                String auteC = bundle.getString("autenticacao");
                Double valor = bundle.getDouble("valor");
                String datac = bundle.getString("datahora");
                String tipo = bundle.getString("tipo");
                boolean reimpressao = bundle.getBoolean("reimpressao", false);

                //Imagem
                PrinterTester.getInstance().init();
                PrinterTester.getInstance().setGray(100);
                PrinterTester.getInstance().leftIndents(160);
                PrinterTester.getInstance().printBitmap(
                        BitmapFactory.decodeResource(getResources(), R.mipmap.logoimpij));
                PrinterTester.getInstance().start();
                //Texto
                PrinterTester.getInstance().init();
                PrinterTester.getInstance().setGray(500);
                PrinterTester.getInstance().leftIndents(30);
                PrinterTester.getInstance().fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_32_32);
                if (reimpressao){
                    PrinterTester.getInstance().printStr("        " + "REIMPRESSAO\n\n", null);
                }
                PrinterTester.getInstance().printStr(getResources().getString(R.string.imp_titu), null);
                PrinterTester.getInstance().leftIndents(40);
                PrinterTester.getInstance().fontSet(EFontTypeAscii.FONT_8_16, EFontTypeExtCode.FONT_48_48);
                PrinterTester.getInstance().printStr("\n   "+getResources().getString(R.string.imp_segu), null);
                PrinterTester.getInstance().fontSet(EFontTypeAscii.FONT_8_32, EFontTypeExtCode.FONT_32_16);
                PrinterTester.getInstance().printStr("\n"+getResources().getString(R.string.imp_trac), null);
                PrinterTester.getInstance().leftIndents(40);
                PrinterTester.getInstance().fontSet(EFontTypeAscii.FONT_8_16, EFontTypeExtCode.FONT_48_48);
                PrinterTester.getInstance().printStr("REALCAP REGISTRADO PARA:\n"+removerAcentos(nomeC)+"\n", null);

                String[] reciboC = new String[reciboc.size()];
                reciboC = reciboc.toArray(reciboC);
                String[] reciboG = new String[recibog.size()];
                reciboG = recibog.toArray(reciboG);
                PrinterTester.getInstance().printStr("\n" + "RECIBO CONT.:", null);
                for (String s : reciboC) {
                    PrinterTester.getInstance().printStr("\n" + s, null);
                }
                PrinterTester.getInstance().printStr("\n" + "RECIBO GRAT..:", null);
                for (String s : reciboG) {
                    PrinterTester.getInstance().printStr("\n" + s, null);
                }

                PrinterTester.getInstance().printStr("\n"+"ED.:" + "                                "+ edicC, null);
                PrinterTester.getInstance().printStr("\n"+"CELULAR: " + "               "+ foneC + "\n", null);
                PrinterTester.getInstance().fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_32_32);
                PrinterTester.getInstance().printStr("\n"+"     AUT. ELETRONICA\n" + "         "+ auteC + "\n", null);
                PrinterTester.getInstance().fontSet(EFontTypeAscii.FONT_8_16, EFontTypeExtCode.FONT_48_48);
                PrinterTester.getInstance().printStr("\nTOTAL PAGO:   " + NumberFormat.getCurrencyInstance().format(valor) + "\n", null);
                PrinterTester.getInstance().printStr("\n"+ tipo +"\n", null);
                PrinterTester.getInstance().printStr("\n"+ datac +"\n", null);
                PrinterTester.getInstance().fontSet(EFontTypeAscii.FONT_8_32, EFontTypeExtCode.FONT_32_16);
                PrinterTester.getInstance().spaceSet(Byte.parseByte("0"),
                        Byte.parseByte("0"));
                PrinterTester.getInstance().printStr(getResources().getString(R.string.imp_trac)+"\n", null);
                PrinterTester.getInstance().spaceSet(Byte.parseByte("0"),
                        Byte.parseByte("0"));
                PrinterTester.getInstance().fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_32_32);
                if (reimpressao){
                    PrinterTester.getInstance().printStr("        " + "REIMPRESSAO", null);
                }else {
                    PrinterTester.getInstance().printStr("         " + getResources().getString(R.string.imp_boas), null);
                }
                PrinterTester.getInstance().step(Integer.parseInt("150"));
                PrinterTester.getInstance().start();

                postHttp(auteC, "i");
            }
        }).start();
    }

     //Fim

    public void postHttp(String autenticacao, String tipo) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    showDialog(Dialog_id);
            }
        });

        if (tipo.equals("i")) {
            post_link = "impressao/gravar_impressao.php";
        }

        if (tipo.equals("s")){
            post_link = "sms/enviar_comprovante_sms";
        }

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(server + post_link);
        ArrayList<NameValuePair> dados = new ArrayList<>();
        dados.add(new BasicNameValuePair("codigo", codigo));
        dados.add(new BasicNameValuePair("chunica", chunica));
        dados.add(new BasicNameValuePair("modo", modo));
        dados.add(new BasicNameValuePair("autenticacao", autenticacao));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(dados));
            final HttpResponse resposta = httpClient.execute(httpPost);
            final String retorno = EntityUtils.toString(resposta.getEntity());
            JSONObject reader = new JSONObject(retorno);
            JSONObject resultado = reader.getJSONObject("resultado");
            String status = resultado.getString("status");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissDialog(Dialog_id);
                }
            });

            if (status.equals("logado")){
                SharedPreferences pref = getSharedPreferences("LOGIN", 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("chunica", resultado.getString("chunica"));
                editor.apply();
                chunica = resultado.getString("chunica");
                modal(getResources().getString(R.string.ale_sucessoimpressao), getResources().getString(R.string.botao_ok), resultado.getString("inicio"), "");
            }else{
                modal(resultado.getString("mensagem"), getResources().getString(R.string.botao_ok), resultado.getString("acao"), "");
            }
        } catch (IOException | JSONException ignored) {
        }
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

                view.findViewById(R.id.btAlerta).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        alerta.dismiss();
                        if (acao.equals("reload")) {
                            Intent intent = new Intent(Impressao.this, Carrinho.class);
                            startActivity(intent);
                        }
                        if (acao.equals("deslogar")) {
                            Impressao.this.getSharedPreferences("LOGIN", 0).edit().clear().apply();
                            Intent intent = new Intent(Impressao.this, Login.class);
                            startActivity(intent);
                        }
                        if (acao.equals("inicio")) {
                            Intent intent = new Intent(Impressao.this, Inicio.class);
                            startActivity(intent);
                        }
                    }
                });

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Impressao.this);
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

    public void voltar_inicio (View view){
        Intent intent = new Intent(Impressao.this, Inicio.class);
        startActivity(intent);
    }

    public void inpresso (){
        Intent intent = new Intent(Impressao.this, Inicio.class);
        startActivity(intent);
    }

}