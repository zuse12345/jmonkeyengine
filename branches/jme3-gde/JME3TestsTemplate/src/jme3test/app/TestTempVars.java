package jme3test.app;

import com.jme3.util.TempVars;

public class TestTempVars {

    public static void methodThatUsesTempVars(){
        TempVars vars = TempVars.get();

        assert vars.lock();
        {
            vars.vect1.set(123,999,-55);
        }
        assert vars.unlock();
    }

    public static void main(String[] args){
        TempVars vars = TempVars.get();

        assert vars.lock();
        {
            // do something with temporary vars
            vars.vect1.addLocal(vars.vect2);
        }
        assert vars.unlock();



        assert vars.lock();
        {
            // set a value
            vars.vect1.set(1,1,1);

            // method that currupts the value
            methodThatUsesTempVars();

            // read the value
            System.out.println(vars.vect1);
        }
        assert vars.unlock();
    }

}
