package jme3test.input.combomoves;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial.CullHint;
import java.util.HashSet;

public class TestComboMoves extends SimpleApplication implements ActionListener {

    private HashSet<String> pressedMappings = new HashSet<String>();

    private ComboMove fireball;
    private ComboMoveExecution fireballExec;
    private BitmapText fireballText;

    private ComboMove shuriken;
    private ComboMoveExecution shurikenExec;
    private BitmapText shurikenText;

    private float time = 0;

    public static void main(String[] args){
        TestComboMoves app = new TestComboMoves();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        fpsText.setCullHint(CullHint.Always);
        statsView.setCullHint(CullHint.Always);

        // Create debug text
        BitmapText helpText = new BitmapText(guiFont);
        helpText.setLocalTranslation(0, settings.getHeight(), 0);
        helpText.setText("Moves:\n" +
                         "Fireball: Down, Down+Left, Left\n"+
                         "Shuriken: Left, Down, Attack1(Z)\n");
        guiNode.attachChild(helpText);

        fireballText = new BitmapText(guiFont);
        fireballText.setColor(ColorRGBA.Orange);
        fireballText.setLocalTranslation(0, fireballText.getLineHeight(), 0);
        guiNode.attachChild(fireballText);

        shurikenText = new BitmapText(guiFont);
        shurikenText.setColor(ColorRGBA.Cyan);
        shurikenText.setLocalTranslation(0, shurikenText.getLineHeight()*2f, 0);
        guiNode.attachChild(shurikenText);

        inputManager.addMapping("Left",    new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right",   new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Up",      new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Down",    new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Attack1", new KeyTrigger(KeyInput.KEY_Z));
        inputManager.addListener(this, "Left", "Right", "Up", "Down", "Attack1");

        fireball = new ComboMove("Fireball");
        fireball.press("Down").notPress("Right").done();
        fireball.press("Right", "Down").done();
        fireball.press("Right").notPress("Down").done();
        fireball.notPress("Right", "Down").done();
        fireball.setUseFinalState(false); // no waiting on final state

        shuriken = new ComboMove("Shuriken");
        shuriken.press("Left").notPress("Down", "Attack1").done();
        shuriken.press("Down").notPress("Attack1").timeElapsed(0.11f).done();
        shuriken.press("Attack1").notPress("Left").timeElapsed(0.11f).done();
        shuriken.notPress("Left", "Down", "Attack1").done();

        fireballExec = new ComboMoveExecution(fireball);
        shurikenExec = new ComboMoveExecution(shuriken);
    }

    @Override
    public void simpleUpdate(float tpf){
        time += tpf;
        secondCounter = 0;

        // check every frame if any executions are expired
        shurikenExec.updateExpiration(time);
        shurikenText.setText("Shuriken Exec: " + shurikenExec.getDebugString());

        fireballExec.updateExpiration(time);
        fireballText.setText("Fireball Exec: " + fireballExec.getDebugString());
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if (isPressed){
            pressedMappings.add(name);
        }else{
            pressedMappings.remove(name);
        }

        // the pressed mappings was changed. update combo executions
        if (shurikenExec.updateState(pressedMappings, time)){
            System.out.println("CASTING SHURIKEN!");
        }

        if (fireballExec.updateState(pressedMappings, time)){
            System.out.println("CASTING FIREBALL!");
        }
    }

}
