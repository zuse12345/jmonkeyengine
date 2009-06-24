package com.g3d.shader;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class DefineList {

    private final SortedMap<String, String> defines = new TreeMap<String, String>();
    private String compiled = null;

    public void clear() {
        defines.clear();
        compiled = "";
    }

    public String get(String key){
        compiled = null;
        return defines.get(key);
    }

    public void set(String key, String val){
        compiled = null;
        defines.put(key, val);
    }

    public void remove(String key){
        compiled = null;
        defines.remove(key);
    }

    public void addFrom(DefineList other){
        compiled = null;
        defines.putAll(other.defines);
    }

    public String getCompiled(){
        if (compiled == null){
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : defines.entrySet()){
                sb.append("#define ").append(entry.getKey()).append(" ");
                sb.append(entry.getValue()).append('\n');
            }
            compiled = sb.toString();
        }
        return compiled;
    }

}
