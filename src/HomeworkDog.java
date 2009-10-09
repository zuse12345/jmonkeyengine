
import com.g3d.math.Plane;
import com.g3d.math.Vector3f;

public class HomeworkDog {

    public static void main(String[] args){
        Plane p = new Plane();
        p.setPlanePoints(new Vector3f(0, -4, -3),
                         new Vector3f(3, -1, 2),
                         new Vector3f(-2, 4, -1));
        System.out.println(p);
    }

}
