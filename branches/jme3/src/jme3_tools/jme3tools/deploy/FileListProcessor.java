package jme3tools.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class FileListProcessor {

    private static JarOutputStream jarOut;
    private static long time;

    private static void addToJar(String path, InputStream stream) throws IOException {
        try{
//            if (source.isDirectory()){
//                String name = source.getPath().replace("\\", "/");
//                if (!name.isEmpty()){
//                    if (!name.endsWith("/")){
//                        name += "/";
//                    }
//                    JarEntry entry = new JarEntry(name);
//                    entry.setTime(source.lastModified());
//                    target.putNextEntry(entry);
//                    target.closeEntry();
//                }
//                for (File nestedFile : source.listFiles()){
//                    add(nestedFile, target);
//                }
//                return;
//            }

            JarEntry entry = new JarEntry(path);
            entry.setTime(time);

            // begin entry
            jarOut.putNextEntry(entry);
            byte[] buffer = new byte[1024];
            while (true){
                int count = stream.read(buffer);
                if (count == -1){
                    break;
                }
                jarOut.write(buffer, 0, count);
            }
            jarOut.closeEntry();

            System.out.println("JARADD: "+path);
        }finally{
            if (stream != null){
                stream.close();
            }
        }
    }

    public static final void iterateFile(String path, long size, InputStream stream){
        try{
            // this is a class, put in codefile
            addToJar(path, stream);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public static final void iterateDir(File root, File dir){
        for (File f : dir.listFiles()){
            if (f.getName().equals(".svn"))
                continue;
            
            if (f.isDirectory()){
                iterateDir(root, f);
            }else if (f.isFile()){
                URI rootUri = root.toURI();
                URI dirUri = dir.toURI();
                URI relative = rootUri.relativize(dirUri);
                try {
                    iterateFile(relative + f.getName(), f.length(), new FileInputStream(f));
                } catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    public static final void iterateZip(File zipF){
        try{
            ZipFile zf = new ZipFile(zipF);
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || entry.getName().equals("META-INF/MANIFEST.MF"))
                    continue;
                
                iterateFile(entry.getName(), entry.getSize(), zf.getInputStream(entry));
            }
        }catch (ZipException ex){
            Logger.getLogger(FileListProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }catch (IOException ex){
            Logger.getLogger(FileListProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void finishJar(File inJar, File outPklz){
        try{
            jarOut.close();
        }catch (IOException ex){
            ex.printStackTrace();
        }

        System.out.println("Finishing jar..");
        FileInputStream in = null;
        try{
            in = new FileInputStream(inJar);
            JarInputStream jis = new JarInputStream(in);
            FileOutputStream out = new FileOutputStream(outPklz);
            JarCompressor.pklz(jis, out);
            in.close();
            out.close();
        }catch (IOException ex){
            ex.printStackTrace();
        }finally{
            try{
                if (in != null)
                    in.close();
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    public static final void process(AppConfig cfg){
        if (!cfg.isFTPOutput()){
            File tmp = new File(cfg.getOutputPath(), "code.jar.tmp");
            tmp.getParentFile().mkdirs();
            try{
                Manifest manifest = new Manifest();
                manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
                manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "hello"); // TODO: read from cfg

                time = System.currentTimeMillis();
                jarOut = new JarOutputStream(new FileOutputStream(tmp), manifest);
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }

        File[] fs = cfg.getInputSources();
        for (File f : fs){
            if (f.isDirectory()){
                iterateDir(f, f);
            }else if (f.isFile()){
                iterateZip(f);
            }
        }

        if (!cfg.isFTPOutput()){
            File tmp = new File(cfg.getOutputPath(), "code.jar.tmp");
            File pklz = new File(cfg.getOutputPath(), "code.pklz");
            finishJar(tmp, pklz);
            tmp.delete();
        }
    }

    public static void main(String[] args){
        File dir = new File("E:\\jME3\\src");
        File jr  = new File("E:\\jME3\\dist\\lib\\lwjgl.jar");
        File jr2 = new File("E:\\jME3\\dist\\lib\\jogl.jar");
        File jr3 = new File("E:\\jME3\\dist\\lib\\gluegen-rt.jar");
        File jr4 = new File("E:\\jME3\\dist\\lib\\jinput.jar");
        File jr5 = new File("E:\\jME3\\dist\\lib\\j-ogg-vorbisd.jar");
        File jr6 = new File("E:\\jME3\\dist\\lib\\j-ogg-oggd.jar");

        AppConfig cfg = new AppConfig();
        cfg.setInputSources(new File[]{ dir, jr, jr2, jr3, jr4, jr5, jr6 });
        cfg.setOutputPath(new File("C:\\output\\"));
        process(cfg);
    }

}
