package br.com.realcapreal.realcapvendas.uteis;

import android.util.Log;

import br.com.setis.ppcompandroid.util.GetObj;

public class TestLog {

    private String childName = "";

    public TestLog() {
        
    }

    public void logTrue(String method) {
        childName = getClass().getSimpleName() + ".";
        String trueLog = childName + method;
        Log.i("IPPITest", trueLog);
        //clear();
        GetObj.logStr += ("true:"+trueLog + "\n");
    }

    public void logErr(String method, String errString) {
        childName = getClass().getSimpleName() + ".";
        String errorLog = childName + method + "   errorMessage：" + errString;
        Log.e("IPPITest", errorLog);
        //clear();
        GetObj.logStr += ("error:"+errorLog + "\n");
    }

    public void clear() {
        GetObj.logStr = "";
    }

    public String getLog() {
        return GetObj.logStr.equals("") ? "Log" : GetObj.logStr;
    }

}
