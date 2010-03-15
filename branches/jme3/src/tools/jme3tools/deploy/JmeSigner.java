package jme3tools.deploy;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import sun.security.tools.KeyTool;

public class JmeSigner {

    public static class AppInfo {
        String project;
        char[] password;

        String ownerName;
        String orgUnit;
        String organization;
        String locality;
        String state;
        String country;
        String email;

        public String getDName(){
            StringBuilder sb = new StringBuilder();
            sb.append("CN=").append(ownerName).append(",");
            sb.append("OU=").append(orgUnit).append(",");
            sb.append("O=").append(organization).append(",");
            sb.append("L=").append(locality).append(",");
            sb.append("ST=").append(state).append(",");
            sb.append("C=").append(country);
            return sb.toString();
        }
    }

    public static void main(String[] args) throws Throwable {
        AppInfo info = new AppInfo();
        //..

        File storefile = new File(".jmekeystore").getAbsoluteFile();

        KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
        store.load(null, info.password);
        store.store(new FileOutputStream(storefile), info.password);

        String[] keytoolargs = new String[]{
            "-genkey",
            "-storepass", String.valueOf(info.password),
            "-keypass", String.valueOf(info.password),
            "-alias", info.project,
            "-dname", info.getDName(),
            "-validity", "999"
        };

        KeyTool.main(keytoolargs);
    }

}
