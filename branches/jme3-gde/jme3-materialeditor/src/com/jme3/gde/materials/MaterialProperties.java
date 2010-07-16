/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.materials;

import com.jme3.gde.core.assets.ProjectAssetManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author normenhansen
 */
public class MaterialProperties {

    private String name;
    private String matDefName;
    FileObject material;
    FileObject matDef;
    Map<String, MaterialProperty> values = new HashMap<String, MaterialProperty>();
    ProjectAssetManager manager;

    public MaterialProperties(FileObject material, ProjectAssetManager manager) {
        this.material = material;
        this.manager = manager;
    }

    public void read() {
        values.clear();
        int level = 0;
        boolean params = false;
        try {
            //material
            for (String line : material.asLines()) {
                line = line.trim();
                if (line.startsWith("Material ") || line.startsWith("Material\t") && level == 0) {
                    readMaterialProperties(line);
                }
                if (line.startsWith("MaterialParameters ") || line.startsWith("MaterialParameters\t") || line.startsWith("MaterialParameters{") && level == 1) {
                    params = true;
                }
                if (line.indexOf("{") != -1) {
                    level++;
                }
                if (line.indexOf("}") != -1) {
                    level--;
                    if (params) {
                        params = false;
                    }
                }
                if (level == 2 && params) {
                    int colonIdx = line.indexOf(":");
                    if (colonIdx != -1) {
                        String[] lines = line.split(":");
                        MaterialProperty prop = new MaterialProperty();
                        prop.setName(lines[0].trim());
                        prop.setValue(lines[1].trim());
                        values.put(prop.getName(), prop);
                    }
                }
            }
            //matdef
            if (matDef != null && matDef.isValid()) {
                for (String line : matDef.asLines()) {
                    line = line.trim();
                    if (line.startsWith("MaterialParameters ") || line.startsWith("MaterialParameters\t") || line.startsWith("MaterialParameters{") && level == 1) {
                        params = true;
                    }
                    if (line.indexOf("{") != -1) {
                        level++;
                    }
                    if (line.indexOf("}") != -1) {
                        level--;
                        if (params) {
                            params = false;
                        }
                    }
                    if (level == 2 && params) {
                        int colonIdx = line.indexOf(":");
                        if (colonIdx != -1) {
                            String[] lines = line.split(":");
                            String type = lines[0].trim();
                            String name = lines[1].trim();
                            MaterialProperty prop = values.get(name);
                            if (prop == null) {
                                prop = new MaterialProperty();
                                prop.setName(name);
                                prop.setValue("");
                                values.put(prop.getName(), prop);
                            }
                            prop.setType(type);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void readMaterialProperties(String line) {
        int colonIdx = line.indexOf(":");
        if (colonIdx != -1) {
            line = line.replaceFirst("Material", "");
            line = line.replace("{", "");
            String[] lines = line.split(":");
            setName(lines[0].trim());
            setMatDefName(lines[1].trim());
            matDef = FileUtil.toFileObject(new File(manager.getFolderName() + File.separator + getMatDefName()).getAbsoluteFile());
            //try to read from classpath if not in assets folder
            if (matDef == null || !matDef.isValid()) {
                try {
                    FileSystem fs = FileUtil.createMemoryFileSystem();
                    matDef = fs.getRoot().createData(name, "j3md");
                    OutputStream out = matDef.getOutputStream();
                    InputStream in = getClass().getResourceAsStream("/" + getMatDefName());
                    if (in != null) {
                        int input = in.read();
                        while (input != -1) {
                            out.write(input);
                            input = in.read();
                        }
                        in.close();
                    }
                    out.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    public void write() {
    }

    public Map<String, MaterialProperty> getMap() {
        return values;
    }

    public void setValue(String key, MaterialProperty value) {
        values.put(key, value);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the matDefName
     */
    public String getMatDefName() {
        return matDefName;
    }

    /**
     * @param matDefName the matDefName to set
     */
    public void setMatDefName(String matDefName) {
        this.matDefName = matDefName;
    }
}
