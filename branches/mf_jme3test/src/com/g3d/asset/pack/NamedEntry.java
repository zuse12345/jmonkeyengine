package com.g3d.asset.pack;

public class NamedEntry {

    final String name;
    final int hash;

    NamedEntry(String name){
        this.name = name;
        this.hash = name.hashCode();
    }

    NamedEntry(int hash){
        this.name = null;
        this.hash = hash;
    }

    @Override
    public int hashCode(){
        return hash;
    }

    @Override
    public boolean equals(Object other){
        if (!(other instanceof NamedEntry)){
            return false;
        }

        NamedEntry otherEnt = (NamedEntry) other;
        if (hash == otherEnt.hash){
            if (name != null)
                return name.equals(otherEnt.name);
            else
                return true;
        }else {
            return false;
        }
    }

    @Override
    public String toString(){
        return getClass().getSimpleName()
             + "[" + (name != null ? name : Integer.toHexString(hash)) + "]";
    }

}
