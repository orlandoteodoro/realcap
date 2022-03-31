package br.com.realcapreal.realcapvendas.uteis;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

public class DataObject {
    @SerializedName("edicao")
    private String edicao;

    public DataObject(){}

    public DataObject(String edicao) {
        this.edicao = edicao;
    }
    public String getName() {
        return edicao;
    }
    public void setName(String edicao) {
        this.edicao = edicao;
    }

    @NonNull
    public String toString(){
        return edicao;
    }
}
