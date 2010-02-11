package com.g3d.font.plugins;

import com.g3d.font.*;
import com.g3d.material.Material;
import com.g3d.material.MaterialDef;
import com.g3d.asset.AssetInfo;
import com.g3d.asset.AssetKey;
import com.g3d.asset.AssetLoader;
import com.g3d.asset.TextureKey;
import com.g3d.texture.Texture;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BitmapFontLoader implements AssetLoader {

    public Object load(AssetInfo info) throws IOException {
        MaterialDef spriteMat = 
                (MaterialDef) info.getManager().loadContent(new AssetKey("font2d.j3md"));

        BitmapCharacterSet charSet = new BitmapCharacterSet();
        Material[] matPages = null;
        BitmapFont font = new BitmapFont();

        BufferedReader reader = new BufferedReader(new InputStreamReader(info.openStream()));
        String regex = "[\\s=]+";

        font.setCharSet(charSet);
        while (reader.ready()){
            String line = reader.readLine();
            String[] tokens = line.split(regex);
            if (tokens[0].equals("info")){
                // Get rendered size
                for (int i = 1; i < tokens.length; i++){
                    if (tokens[i].equals("size")){
                        charSet.setRenderedSize(Integer.parseInt(tokens[i + 1]));
                    }
                }
            }else if (tokens[0].equals("common")){
                // Fill out BitmapCharacterSet fields
                for (int i = 1; i < tokens.length; i++){
                    String token = tokens[i];
                    if (token.equals("lineHeight")){
                        charSet.setLineHeight(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("base")){
                        charSet.setBase(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("scaleW")){
                        charSet.setWidth(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("scaleH")){
                        charSet.setHeight(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("pages")){
                        // number of texture pages
                        matPages = new Material[Integer.parseInt(tokens[i + 1])];
                        font.setPages(matPages);
                    }
                }
            }else if (tokens[0].equals("page")){
                int index = -1;
                Texture tex = null;

                for (int i = 1; i < tokens.length; i++){
                    String token = tokens[i];
                    if (token.equals("id")){
                        index = Integer.parseInt(tokens[i + 1]);
                    }else if (token.equals("file")){
                        String file = tokens[i + 1];
                        if (file.startsWith("\"")){
                            file = file.substring(1, file.length()-1);
                        }
                        TextureKey key = new TextureKey(file, true);
                        key.setGenerateMips(false);
                        tex = info.getManager().loadTexture(key);
                    }
                }
                // set page
                if (index >= 0 && tex != null){
                    Material mat = new Material(spriteMat);
                    mat.setTexture("m_Texture", tex);
                    matPages[index] = mat;
                }
            }else if (tokens[0].equals("char")){
                // New BitmapCharacter
                BitmapCharacter ch = null;
                for (int i = 1; i < tokens.length; i++){
                    String token = tokens[i];
                    if (token.equals("id")){
                        int index = Integer.parseInt(tokens[i + 1]);
                        ch = new BitmapCharacter();
                        charSet.addCharacter(index, ch);
                    }else if (token.equals("x")){
                        ch.setX(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("y")){
                        ch.setY(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("width")){
                        ch.setWidth(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("height")){
                        ch.setHeight(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("xoffset")){
                        ch.setXOffset(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("yoffset")){
                        ch.setYOffset(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("xadvance")){
                        ch.setXAdvance(Integer.parseInt(tokens[i + 1]));
                    }
                }
            }else if (tokens[0].equals("kerning")){
                // Build kerning list
                int index = 0;
                Kerning k = new Kerning();

                for (int i = 1; i < tokens.length; i++){
                    if (tokens[i].equals("first")){
                        index = Integer.parseInt(tokens[i + 1]);
                    }else if (tokens[i].equals("second")){
                        k.setSecond(Integer.parseInt(tokens[i + 1]));
                    }else if (tokens[i].equals("amount")){
                        k.setAmount(Integer.parseInt(tokens[i + 1]));
                    }
                }

                BitmapCharacter ch = charSet.getCharacter(index);
                ch.getKerningList().add(k);
            }
        }
        reader.close();

        return font;
    }

}
