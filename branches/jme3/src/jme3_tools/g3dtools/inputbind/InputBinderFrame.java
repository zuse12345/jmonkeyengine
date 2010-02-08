package g3dtools.inputbind;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;
import org.lwjgl.LWJGLException;

public class InputBinderFrame extends JFrame {

    private static abstract class Binding {
        public abstract String getValue();
    }

    private static class KeyBinding extends Binding {

        int keyCode;

        public KeyBinding(int keyCode) {
            this.keyCode = keyCode;
        }

        public String getValue(){
            return "Key " + KeyEvent.getKeyText(keyCode);
        }
    }

    private static class MouseAxisBinding extends Binding {

        int axisId;
        boolean pos;

        public String getValue(){
            return "Mouse Axis "+axisId+(pos?"+":"-");
        }

    }

    private static class MouseButtonBinding extends Binding {

        int buttonId;

        public String getValue(){
            return "Mouse Button "+buttonId;
        }
    }

    private static class JoystickButtonBinding extends Binding {

        static ControllerEnvironment env
                = ControllerEnvironment.getDefaultEnvironment();

        int joystickId;
        int joystickButton;

        public String getValue() {
            Controller c = env.getControllers()[joystickId];
            return c.getComponents()[joystickButton].getName();
        }

    }

    private static class EmptyBinding extends Binding {
        public String getValue(){
            return "(empty)";
        }
    }

    private KeyListener keyListener = new KeyAdapter() {
        public void keyPressed(KeyEvent e){
            bindingInvoked(new KeyBinding(e.getKeyCode()));
        }
    };

    private Map<String, Binding> bindings = new HashMap<String, Binding>();
    private String listeningTo = null;

    private static class JoyListener implements ControllerListener {

        public void controllerRemoved(ControllerEvent e) {
            System.out.println(e);
        }

        public void controllerAdded(ControllerEvent e) {
            System.out.println(e);
        }
    }

    private void bindingInvoked(Binding b){

    }

    private void listenForBindings(String name){
        if (listeningTo != null){
            stopListening();
        }

        listeningTo = name;
        ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
        env.addControllerListener(new JoyListener());
    }

    private void stopListening(){
        listeningTo = null;
    }

    private void addBinding(final String name, Binding b){
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel(name + ": ");
        label.setBorder(new EmptyBorder(0, 0, 0, 15));
        panel.add(label);
        final JButton btn = new JButton(b!=null?b.getValue():"(empty)");
        panel.add(btn);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listenForBindings(name);
                btn.setText("Listening..");
            }
        });

        getContentPane().add(panel);
    }

    public InputBinderFrame(){
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container c = getContentPane();
        c.setLayout(new BoxLayout(c, BoxLayout.PAGE_AXIS));
        addBinding("Strafe Left", null);
        addBinding("Strafe Right", null);
        addBinding("Forward", null);
        addBinding("Backward", null);
        addBinding("Use Weapon", null);
        addBinding("Melee", null);
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                new InputBinderFrame().setVisible(true);
            }
        });
    }

}
