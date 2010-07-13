package jme3test.collision;

import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RayTrace {

    private BufferedImage image;
    private Camera cam;
    private Spatial scene;
    private CollisionResults results = new CollisionResults();

    public RayTrace(Spatial scene, Camera cam, int width, int height){
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.scene = scene;
        this.cam = cam;
    }

    public void trace(){
        int h = image.getHeight();
        for (int y = 0; y < image.getHeight(); y++){
            for (int x = 0; x < image.getWidth(); x++){
                Vector2f v = new Vector2f(x,y);
                Vector3f pos = cam.getWorldCoordinates(v, 0.0f);
                Vector3f dir = cam.getWorldCoordinates(v, 0.3f);
                dir.subtractLocal(pos).normalizeLocal();

                Ray r = new Ray(pos, dir);

                results.clear();
                scene.collideWith(r, results);
                if (results.size() > 0){
                    image.setRGB(x, h - y - 1, 0xFFFFFFFF);
                }else{
                    image.setRGB(x, h - y - 1, 0xFF000000);
                }
            }
        }
        
        JFrame frame = new JFrame("HDR View");
        JLabel label = new JLabel(new ImageIcon(image));
        frame.getContentPane().add(label);
        frame.setLayout(new FlowLayout());
        frame.pack();
        frame.setVisible(true);

//            ImageIO.write(image, "png", new File("C:\\hello.png"));
    }

}
