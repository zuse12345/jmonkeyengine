package com.g3d.input;

public class KeyTrigger implements Trigger {

    private int keyCode;
    private String keyName;

    public KeyTrigger(int keyCode){
        if (keyCode < 0){
            this.keyCode = -1;
            keyName = "Invalid";
        }else{
//            if (KeyInput.isInited()){
//                keyName = KeyInput.get().getKeyName(keyCode);
//                this.keyCode = keyCode;
//            }
            if (keyName == null && keyName.equals(""))
                keyName = "Key " + keyCode;
        }
    }

    @Override
    public String getName(){
        return keyName;
    }

    @Override
    public float getValue(){
//        if (keyCode == -1)
            return 0f;

//        return KeyInput.get().isKeyDown(keyCode) ? 1f : 0f;
    }

}
