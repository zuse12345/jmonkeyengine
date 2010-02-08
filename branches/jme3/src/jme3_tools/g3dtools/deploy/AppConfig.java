package g3dtools.deploy;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class AppConfig extends Properties {

    private String name;

    public void save(){
        File output = new File(System.getProperty("user.dir"), "/apps/" + name + ".appconfig");
        output.getParentFile().mkdirs();

        FileOutputStream stream = null;
        try{
            stream = new FileOutputStream(output);
            store(stream, null);
            stream.flush();
        }catch (IOException ex){
            ex.printStackTrace();
        }finally{
            if (stream != null){
                try{
                    stream.close();
                }catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    public void load(InputStream in) throws IOException{
        super.load(in);
        name = getTitle();
    }

    public void loadFromXML(InputStream in) throws IOException{
        super.loadFromXML(in);
        name = getTitle();
    }

    public JnlpParams intoJnlp(){
        JnlpParams params = new JnlpParams();
        params.setVendor(getVendor());
        params.setTitle(getTitle());
        params.setDescription(getDescription());
        params.setShortDesc(getShortDesc());
        params.setHomepage(getHomepage());

        File output = new File(System.getProperty("user.dir"), "/apps/" + name + ".png");
        if (output.exists()){
            try{
                params.setIcon(output.toURI().toURL());
            }catch (MalformedURLException ex){
            }
        }

        return params;
    }

    public String toString(){
        return name;
    }

    public void setTitle(String title){
        setProperty("AppTitle", title);
        name = title;
    }

    public void setVendor(String vendor){
        setProperty("AppVendor", vendor);
    }

    public void setHomepage(URL homepage){
        setProperty("AppHomePage", homepage.toString());
    }

    public void setDescription(String desc){
        setProperty("AppDescription", desc);
    }
    
    public void setShortDesc(String desc){
        setProperty("AppShortDescription", desc);
    }

    public void setIcon(BufferedImage icon){
        File output = new File(System.getProperty("user.dir"), "/apps/" + name + ".png");
        output.getParentFile().mkdirs();

        FileOutputStream stream = null;
        try{
            stream = new FileOutputStream(output);
            ImageIO.write(icon, "png", stream);
        }catch (IOException ex){
            ex.printStackTrace();
        }finally{
            if (stream != null){
                try{
                    stream.close();
                }catch (IOException ex){
                }
            }
        }
    }

    public void setInputSources(File[] sources){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sources.length; i++){
            String str = sources[i].toString();
            if (str != null && !str.equals("")){
                sb.append(str);
                if (i != sources.length - 1){
                    sb.append(";");
                }
            }
        }
        setProperty("InputSources", sb.toString());
    }

    public void setFtpDeploy(String host, int port, String user, String pass){
        setProperty("FtpHost", host);
        setProperty("FtpPort", Integer.toString(port));
        setProperty("FtpUsername", user);
        setProperty("FtpPassword", pass);
    }

    public void setOutputPath(File outPath){
        setProperty("FileOutput", outPath.toString());
    }

    public String getTitle(){
        return getProperty("AppTitle");
    }

    public String getVendor(){
        return getProperty("AppVendor");
    }

    public URL getHomepage(){
        try{
            return new URL(getProperty("AppHomePage"));
        }catch (MalformedURLException ex){
        }
        return null;
    }

    public String getDescription(){
        return getProperty("AppDescription");
    }

    public String getShortDesc(){
        return getProperty("AppShortDescription");
    }

    public File[] getInputSources(){
        String sources = getProperty("InputSources");
        if (sources == null)
            return new File[0];

        String[] srcArray = sources.split(";");
        File[] srcFiles = new File[srcArray.length];
        for (int i = 0; i < srcArray.length; i++){
            srcFiles[i] = new File(srcArray[i]);
        }
        return srcFiles;
    }

    public BufferedImage getIcon(){
        File input = new File(System.getProperty("user.dir"), "/apps/" + name + ".png");
        if (input.exists() && input.isFile()){
            try{
                return ImageIO.read(input);
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
        return null;
    }

    public boolean isFTPOutput(){
        return getProperty("FtpPort") != null;
    }

    public String getOutputPath(){
        return getProperty("FileOutput");
    }

    public String[] getFtpParams(){
        return new String[]{
            getProperty("FtpHost"),
            getProperty("FtpPort"),
            getProperty("FtpUsername"),
            getProperty("FtpPassword")
        };
    }
}
