package br.com.realcapreal.realcapvendas.uteis;

import com.pax.dal.ISys;
import com.pax.dal.entity.EBeepMode;
import com.pax.dal.entity.ETermInfoKey;

import java.util.Map;

import br.com.realcapreal.realcapvendas.Atualizar;

import static android.os.Environment.getExternalStorageDirectory;
import static com.pax.dal.entity.ETermInfoKey.AP_VER;
import static com.pax.dal.entity.ETermInfoKey.SN;

public class SysTester {

    private static br.com.realcapreal.realcapvendas.uteis.SysTester sysTester;
    private ISys iSys = null;
    public String txtResult;

    private SysTester() {
        iSys = Atualizar.getDal().getSys();
    }

    public static br.com.realcapreal.realcapvendas.uteis.SysTester getInstance() {
        if (sysTester == null) {
            sysTester = new br.com.realcapreal.realcapvendas.uteis.SysTester();
        }
        return sysTester;
    }

    public void beep(final EBeepMode beepMode, final int delayTime) {
        iSys.beep(beepMode, delayTime);
    }

    public String getPN() {
        return iSys.getPN();
    }

    public String getSn() {
        Map<ETermInfoKey, String> sn = iSys.getTermInfo();
        return sn.get(SN);
    }

    public String getOsVer() {
        Map<ETermInfoKey, String> os = iSys.getTermInfo();
        return os.get(AP_VER);
    }

    public void installApp(String vercao) {
        String appPath = getExternalStorageDirectory().getAbsolutePath() + "/Download/update-realcap-v"+vercao+".apk";
        int install = iSys.installApp(appPath);
        switch (install) {
            case 0:
                txtResult = "Realizando update";
                break;
            case 1:
                txtResult = "SERVICE_NOT_AVAILABLE";
                break;
            case 2:
                txtResult = "INSTALL_FAIL";
                break;
            case 3:
                txtResult = "TIMEOUT_ERR";
                break;
            case 4:
                txtResult = "READ_DATA_FAIL";
                break;
            case 5:
                txtResult = "NO_USBSECURITY_PERMISSION";
                break;
            case -1:
                txtResult = "UPDATE_PACKAGE_ERR";
                break;
            case -2:
                txtResult = "UPDATE_UNZIP_ERR";
                break;
            case -3:
                txtResult = "UPDATE_VERIFY_ERR";
                break;
            case -4:
                txtResult = "UPDATE_RPC_OPEN_ERR";
                break;
            case -5:
                txtResult = "UPDATE_WRITE_SP_IMG_ERR";
                break;
            case -6:
                txtResult = "UPDATE_WRITE_PUK_ERR";
                break;
            case -7:
                txtResult = "UPDATE_CUSTOMER_ERR";
                break;
            case -8:
                txtResult = "UPDATE_MODEM_ERR";
                break;
            case -11:
                txtResult = "GET_SUBID_ERROR";
                break;
            case -21:
                txtResult = "FILE_NOT_READ_EXIST";
                break;
            case -22:
                txtResult = "INSTALL_FAILED_VERIFICATION_FAILURE";
                break;
            case -25:
                txtResult = "INSTALL_FAILED_VERSION_DOWNGRADE";
                break;
            case -50:
                txtResult = "PKG_OR_CLASS_NAME_ERROR";
                break;
            case -99:
                txtResult = "UPDATE_PERMISSION_ERROR";
                break;
            case -101:
                txtResult = "UPDATE_UNKNOWN_ERR";
                break;
            default:
                break;
        }
    }
}