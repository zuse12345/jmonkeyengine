package com.g3d.res;

/**
 * This class should be immutable.
 */
public class ContentKey {

    protected final String name;

    public ContentKey(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other){
        if (!(other instanceof ContentKey)){
            return false;
        }
        return name.equals(((ContentKey)other).name);
    }

    @Override
    public int hashCode(){
        return name.hashCode();
    }

    @Override
    public String toString(){
        return name;
    }

}
