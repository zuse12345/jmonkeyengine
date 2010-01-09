package g3dgame.cubefield;

import java.util.ArrayList;

import com.g3d.app.SimpleApplication;
import com.g3d.bounding.BoundingVolume;
import com.g3d.font.BitmapFont;
import com.g3d.font.BitmapText;
import com.g3d.input.KeyInput;
import com.g3d.input.binding.BindingListener;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.math.FastMath;
import com.g3d.math.Quaternion;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.Node;
import com.g3d.scene.shape.Box;
import com.g3d.scene.shape.Dome;

/**
 * @author Kyle "bonechilla" Williams
 */
public class CubeField extends SimpleApplication implements BindingListener {

    public static void main(String[] args) {
        CubeField app = new CubeField();
        
        app.start();
    }

    private boolean START;
    private int difficulty, Score, colorInt, highCap, lowCap,diffHelp;
    private Node player;
    private Geometry fcube;
    private ArrayList<Geometry> cubeField;
    private ArrayList<ColorRGBA> obstacleColors;
    private float speed, coreTime,coreTime2;
    private float camAngle = 0;
    private BitmapText fpsScoreText, pressStart;
    private String boxSolid;

    /**
     * Initializes game 
     */
    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);

        Keys();

        BitmapFont font = manager.loadFont("cooper.fnt");
        pressStart = new BitmapText(font, false);
        fpsScoreText = new BitmapText(font, false);

        loadText(fpsScoreText, "Current Score: 0", font, 0, 2, 0);
        loadText(pressStart, "PRESS ENTER", font, 0, 5, 0);
        
        player = createPlayer();
        rootNode.attachChild(player);
        cubeField = new ArrayList<Geometry>();
        obstacleColors = new ArrayList<ColorRGBA>();

        gameReset();
    }
    /**
     * Used to reset cubeField 
     */
    private void gameReset(){
        Score = 0;
        lowCap = 10;
        colorInt = 0;
        highCap = 40;
        difficulty = highCap;

        for (Geometry cube : cubeField){
            cube.removeFromParent();
        }
        cubeField.clear();

        if (fcube != null){
            fcube.removeFromParent();
        }
        fcube = createFirstCube();

        obstacleColors.clear();
        obstacleColors.add(ColorRGBA.Orange);
        obstacleColors.add(ColorRGBA.Red);
        obstacleColors.add(ColorRGBA.Yellow);
        renderer.setBackgroundColor(ColorRGBA.White);
        boxSolid = "solid_color.j3md";
        speed = lowCap / 400f;
        coreTime = 20.0f;
        coreTime2 = 10.0f;
        diffHelp=lowCap;
        player.setLocalTranslation(0,0,0);
    }

    @Override
    public void simpleUpdate(float tpf) {
        camTakeOver(tpf);
        if (START){
            gameLogic();
        }
        colorLogic();
    }
    /**
     * Forcefully takes over Camera adding functionality and placing it behind the character
     * @param tpf Tickes Per Frame
     */
    private void camTakeOver(float tpf) {
        cam.setLocation(player.getLocalTranslation().add(-8, 2, 0));
        cam.lookAt(player.getLocalTranslation(), Vector3f.UNIT_Y);
        
        Quaternion rot = new Quaternion();
        rot.fromAngleNormalAxis(camAngle, Vector3f.UNIT_Z);
        cam.setRotation(cam.getRotation().mult(rot));
        camAngle *= (.99f + tpf * .01f);
    }

    @Override
    public void requestClose(boolean esc) {
        if (!esc){
            System.out.println("Thier could be an issue!!");
        }else{
            System.out.println("Player has Collided. Final Score is " + Score);
        }
        context.destroy();
    }
    /**
     * Randomly Places a cube on the map between 30 and 90 paces away from player
     */
    private void randomizeCube() {
        Geometry cube = fcube.clone();
        int playerX = (int) player.getLocalTranslation().getX();
        int playerZ = (int) player.getLocalTranslation().getZ();
//        float x = FastMath.nextRandomInt(playerX + difficulty + 10, playerX + difficulty + 150);
        float x = FastMath.nextRandomInt(playerX + difficulty + 30, playerX + difficulty + 90);
        float z = FastMath.nextRandomInt(playerZ - difficulty - 50, playerZ + difficulty + 50);
        cube.getLocalTranslation().set(x, 0, z);

//        playerX+difficulty+30,playerX+difficulty+90


        Material mat = new Material(manager, boxSolid);
        mat.setColor("m_Color", obstacleColors.get(FastMath.nextRandomInt(0, obstacleColors.size() - 1)));
        cube.setMaterial(mat);

        rootNode.attachChild(cube);
        cubeField.add(cube);
    }

    private Geometry createFirstCube() {
        Vector3f loc = player.getLocalTranslation();
        loc.addLocal(4, 0, 0);
        Box b = new Box(loc, 1, 1, 1);

        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(manager, "solid_color.j3md");
        mat.setColor("m_Color", ColorRGBA.Blue);
        geom.setMaterial(mat);

        return geom;
    }

    private Node createPlayer() {
        Dome b = new Dome(Vector3f.ZERO, 10, 100, 1);
        Geometry playerMesh = new Geometry("Box", b);
        playerMesh.updateModelBound();
        Material mat = new Material(manager, "solid_color.j3md");
        mat.setColor("m_Color", ColorRGBA.Red);
        playerMesh.setMaterial(mat);
        playerMesh.setName("player");

        Box floor = new Box(Vector3f.ZERO.add(playerMesh.getLocalTranslation().getX(),
                playerMesh.getLocalTranslation().getY() - 1, 0), 100, 0, 100);
        Geometry floorMesh = new Geometry("Box", floor);
        floorMesh.updateModelBound();
        mat = new Material(manager, "solid_color.j3md");
        mat.setColor("m_Color", ColorRGBA.LightGray);
        floorMesh.setMaterial(mat);
        floorMesh.setName("floor");

        Node playerNode = new Node();
        playerNode.attachChild(playerMesh);
        playerNode.attachChild(floorMesh);

        return playerNode;
    }

    /**
     * If Game is Lost display Score and Reset the Game
     */
    private void gameLost(){
        START = false;
        BitmapFont font = manager.loadFont("cooper.fnt");
        loadText(pressStart, "You lost! Press enter to try again.", font, 0, 5, 0);
        gameReset();
    }
    /**
     * Core Game Logic
     */
    private void gameLogic(){
    	//Subtract difficulty level in accordance to speed every 10 seconds
    	if(timer.getTimeInSeconds()>=coreTime2){
			coreTime2=timer.getTimeInSeconds()+10;
			if(difficulty<=lowCap){
				difficulty=lowCap;
			}
			else if(difficulty>lowCap){
				difficulty-=5;
				diffHelp+=1;
			}
		}
    	
        if(speed<.1f){
            speed+=.000001f;
        }

        player.move(speed, 0, 0);
        if (cubeField.size() > difficulty){
            cubeField.remove(0);
        }else if (cubeField.size() != difficulty){
            randomizeCube();
        }

        if (cubeField.isEmpty()){
            requestClose(false);
        }else{
            for (int i = 0; i < cubeField.size(); i++){
            	
            	//better way to check collision
                Geometry playerModel = (Geometry) player.getChild(0);
                Geometry cubeModel = cubeField.get(i);
                cubeModel.updateGeometricState();

                BoundingVolume pVol = playerModel.getWorldBound();
                BoundingVolume vVol = cubeModel.getWorldBound();

                if (pVol.intersects(vVol)){
                    gameLost();
                    return;
                }
                //Remove cube if 10 paces behind player
                if (cubeField.get(i).getLocalTranslation().getX() + 10 < player.getLocalTranslation().getX()){
                    cubeField.get(i).removeFromParent();
                    cubeField.remove(cubeField.get(i));
                }

            }
        }

        Score++;
        fpsScoreText.setText("Current Score: "+Score);
    }
    /**
     * Sets up the keyboard bindings
     */
    private void Keys() {
        inputManager.registerKeyBinding("START", KeyInput.KEY_RETURN);
        inputManager.registerKeyBinding("Left", KeyInput.KEY_LEFT);
        inputManager.registerKeyBinding("Right", KeyInput.KEY_RIGHT);
        //used with method onBinding in BindingListener interface
        //in order to add function to keys
        inputManager.addTriggerListener(this);
    }

    public void onBinding(String binding, float value) {
        if (binding.equals("START") && !START){
            START = true;
            guiNode.detachChild(pressStart);
            System.out.println("START");
        }else if (START == true && binding.equals("Left")){
            player.move(0, 0, -speed / 2);
            camAngle -= value;
        }else if (START == true && binding.equals("Right")){
            player.move(0, 0, speed / 2);
            camAngle += value;
        }
    }
    /**
     * Determines the colors of the player, floor, obstacle and background
     */
    private void colorLogic() {
    	if (timer.getTimeInSeconds() >= coreTime){
            
        	colorInt++;
            coreTime = timer.getTimeInSeconds() + 20;
        

	        switch (colorInt){
	            case 1:
	                obstacleColors.clear();
	                boxSolid(false);
	                obstacleColors.add(ColorRGBA.Green);
	                renderer.setBackgroundColor(ColorRGBA.Black);
	                player.getChild("player").getMaterial().setColor("m_Color", ColorRGBA.White);
					player.getChild("floor").getMaterial().setColor("m_Color", ColorRGBA.Black);
	                break;
	            case 2:
	                obstacleColors.set(0, ColorRGBA.Black);
	                boxSolid(true);
	                renderer.setBackgroundColor(ColorRGBA.White);
	                player.getChild("player").getMaterial().setColor("m_Color", ColorRGBA.Gray);
					player.getChild("floor").getMaterial().setColor("m_Color", ColorRGBA.LightGray);
	                break;
	            case 3:
	                obstacleColors.set(0, ColorRGBA.Pink);
	                break;
	            case 4:
	                obstacleColors.set(0, ColorRGBA.Cyan);
	                obstacleColors.add(ColorRGBA.Magneta);
	                renderer.setBackgroundColor(ColorRGBA.Gray);
					player.getChild("floor").getMaterial().setColor("m_Color", ColorRGBA.Gray);
	                player.getChild("player").getMaterial().setColor("m_Color", ColorRGBA.White);
	                break;
	            case 5:
	                obstacleColors.remove(0);
	                renderer.setBackgroundColor(ColorRGBA.Pink);
	                boxSolid(false);
	                player.getChild("player").getMaterial().setColor("m_Color", ColorRGBA.White);
	                break;
	            case 6:
	                obstacleColors.set(0, ColorRGBA.White);
	                boxSolid(true);
	                renderer.setBackgroundColor(ColorRGBA.Black);
	                player.getChild("player").getMaterial().setColor("m_Color", ColorRGBA.Gray);
					player.getChild("floor").getMaterial().setColor("m_Color", ColorRGBA.LightGray);
	                break;
	            case 7:
	                obstacleColors.set(0, ColorRGBA.Green);
	                renderer.setBackgroundColor(ColorRGBA.Gray);
	                player.getChild("player").getMaterial().setColor("m_Color", ColorRGBA.Black);
					player.getChild("floor").getMaterial().setColor("m_Color", ColorRGBA.Orange);
	                break;
	            case 8:
	                obstacleColors.set(0, ColorRGBA.Red);
					player.getChild("floor").getMaterial().setColor("m_Color", ColorRGBA.Pink);
	                break;
	            case 9:
	                obstacleColors.set(0, ColorRGBA.Orange);
	                obstacleColors.add(ColorRGBA.Red);
	                obstacleColors.add(ColorRGBA.Yellow);
	                renderer.setBackgroundColor(ColorRGBA.White);
	                player.getChild("player").getMaterial().setColor("m_Color", ColorRGBA.Red);
	                player.getChild("floor").getMaterial().setColor("m_Color", ColorRGBA.Gray);
	                colorInt=0;
	                break;
	            default:
	                break;
	        }
        }
    }
    /**
     * Sets up a BitmapText to be displayed
     * @param txt the Bitmap Text
     * @param text the 
     * @param font the font of the text
     * @param x    
     * @param y
     * @param z
     */
    private void loadText(BitmapText txt, String text, BitmapFont font, float x, float y, float z) {
        txt.setSize(font.getCharSet().getRenderedSize());
        txt.setLocalTranslation(txt.getLineWidth() * x, txt.getLineHeight() * y, z);
        txt.setText(text);
        guiNode.attachChild(txt);
    }

    /**
     * Changes the boolean variable boxSolid 
     * @param solid the boolean to determine if the boxes will be solid or wireFrame
     */
    private void boxSolid(boolean solid) {
        if (solid == false){
            boxSolid = "wire_color.j3md";
        }else{
            boxSolid = "solid_color.j3md";
        }
    }

} 