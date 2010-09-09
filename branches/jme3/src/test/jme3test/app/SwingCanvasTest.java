package jme3test.app;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.texture.Texture;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * you can embed a jme canvas inside a swing application
 * @author pgi
 */
public class SwingCanvasTest extends SimpleApplication {

  @Override
  public void simpleInitApp() {
    // activate windowed input behaviour
    flyCam.setDragToRotate(true);
    // display a cube
    Box boxshape1 = new Box(new Vector3f(-3f,1.1f,0f), 1f,1f,1f);
    Geometry cube = new Geometry("My Textured Box", boxshape1);
    Material mat_stl = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
    Texture tex_ml = assetManager.loadTexture("Interface/Logo/Monkey.jpg");
    mat_stl.setTexture("m_ColorMap", tex_ml);
    cube.setMaterial(mat_stl);
    rootNode.attachChild(cube);
  }

  public static void main(String[] args) {
    java.awt.EventQueue.invokeLater(new Runnable() {

      public void run() {
        // create new JME appsettings
        AppSettings settings = new AppSettings(true);
        settings.setWidth(640);
        settings.setHeight(480);

        // create new canvas application
        SwingCanvasTest canvasApplication = new SwingCanvasTest();
        canvasApplication.setSettings(settings);
        canvasApplication.createCanvas(); // create canvas!
        JmeCanvasContext ctx = (JmeCanvasContext) canvasApplication.getContext();
        ctx.setSystemListener(canvasApplication);
        Dimension dim = new Dimension(640, 480);
        ctx.getCanvas().setPreferredSize(dim);

        // Create Swing window
        JFrame window = new JFrame("Swing Application");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Fill Swing window with canvas and swing components
        JPanel panel = new JPanel(new FlowLayout()); // a panel
        panel.add(ctx.getCanvas());                  // add JME canvas
        panel.add(new JButton("Swing Component"));      // add some Swing
        window.add(panel);
        window.pack();

        // Display Swing window including JME canvas!
        window.setVisible(true);
        canvasApplication.startCanvas();
      }
    });
  }
}
