package mygame;

import com.jme3.anim.AnimComposer;
import com.jme3.anim.SkinningControl;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.effect.ParticleMesh;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.CenterQuad;
import com.jme3.scene.shape.Torus;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;

import mygame.effect.MyParticleEmitter;
import mygame.effect.shape.EmitterMeshFaceVFX;
import mygame.effect.shape.EmitterMeshVertexVFX;

/**
 *
 * @author capdevon
 */
public class Test_JmeVFX2 extends SimpleApplication implements ActionListener {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        Test_JmeVFX2 app = new Test_JmeVFX2();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);
        settings.setRenderer(AppSettings.LWJGL_OPENGL32);
        app.setSettings(settings);
        app.setPauseOnLostFocus(false);
        app.setShowSettings(false);
        app.start();
    }

    private MyParticleEmitter emit;
    private Node myModel;
    private AnimComposer animComposer;
    private BitmapText emitUI;
    private MotionEvent motionControl;
    private boolean playing;

    @Override
    public void simpleInitApp() {

        BitmapText hud = createTextUI(ColorRGBA.White, 20, 15);
        hud.setText("Play/Pause Motion: KEY_SPACE, InWorldSpace: KEY_I");

        emitUI = createTextUI(ColorRGBA.Blue, 20, 15 * 2);

        configCamera();
        setupLights();
        setupScene();
        setupCharacter();
        createMotionControl();
        setupKeys();
    }

    private void setupCharacter() {
        // Add model to the scene
        myModel = (Node) assetManager.loadModel("Models/Mixamo/ybot.j3o");
        rootNode.attachChild(myModel);

        animComposer = GameObject.findControl(myModel, AnimComposer.class);
        animComposer.setCurrentAction("Idle");

        SkinningControl skControl = GameObject.findControl(myModel, SkinningControl.class);
        skControl.setHardwareSkinningPreferred(false);

        // 1. Alpha_Surface
        // 2. Alpha_Joints
        Geometry body = (Geometry) myModel.getChild("Alpha_Surface");
        emit = createParticleEmitter(body);
        myModel.attachChild(emit);

//        Geometry torus = createTorus(1f);
//        myModel.attachChild(torus);
//        emit = createParticleEmitter(torus);
//        myModel.attachChild(emit);
    }

    private Geometry createTorus(float radius) {
        float s = radius / 80f; // s = 2/80 = 0.025
        Geometry geo = new Geometry("CircleXZ", new Torus(64, 4, s, radius));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);
        geo.setMaterial(mat);
        geo.rotate(FastMath.PI / 2, 0, 0);
        return geo;
    }

    private MyParticleEmitter createParticleEmitter(Geometry geo) {
        MyParticleEmitter emit = new MyParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 1000);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Smoke/Smoke.png"));
//        mat.setColor("GlowColor", ColorRGBA.Yellow);
        emit.setMaterial(mat);
        emit.setLowLife(1);
        emit.setHighLife(1);
        emit.setImagesX(15);
        emit.setStartSize(0.04f);
        emit.setEndSize(0.02f);
        emit.setStartColor(ColorRGBA.Orange);
        emit.setEndColor(ColorRGBA.Red);
        emit.setParticlesPerSec(900);
        emit.setGravity(0, 0f, 0);
        emit.getParticleInfluencer().setVelocityVariation(1);
        emit.getParticleInfluencer().setInitialVelocity(new Vector3f(0, .5f, 0));
        emit.setShape(new EmitterMeshVertexVFX(geo.getMesh()));
//        emit.setShape(new EmitterMeshFaceVFX(geo.getMesh()));
        return emit;
    }

    private void createMotionControl() {
        float radius = 5f;
        MotionPath path = new MotionPath();
        path.setCycle(true);
        path.addWayPoint(new Vector3f(radius, 0, 0));
        path.addWayPoint(new Vector3f(0, 0, radius));
        path.addWayPoint(new Vector3f(-radius, 0, 0));
        path.addWayPoint(new Vector3f(0, 0, -radius));
        path.setCurveTension(0.83f);
        //path.enableDebugShape(assetManager, rootNode);

        motionControl = new MotionEvent(myModel, path);
        motionControl.setLoopMode(LoopMode.Loop);
        //motionControl.setInitialDuration(10f);
        //motionControl.setSpeed(2f);
        motionControl.setDirectionType(MotionEvent.Direction.Path);
    }

    private void setupKeys() {
        addMapping("ToggleMotionEvent", new KeyTrigger(KeyInput.KEY_SPACE));
        addMapping("InWorldSpace", new KeyTrigger(KeyInput.KEY_I));
    }

    private void addMapping(String mappingName, Trigger... triggers) {
        inputManager.addMapping(mappingName, triggers);
        inputManager.addListener(this, mappingName);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("InWorldSpace") && isPressed) {
            boolean worldSpace = emit.isInWorldSpace();
            emit.setInWorldSpace(!worldSpace);

        } else if (name.equals("ToggleMotionEvent") && isPressed) {
            if (playing) {
                playing = false;
                motionControl.pause();
                animComposer.setCurrentAction("Idle");
            } else {
                playing = true;
                motionControl.play();
                animComposer.setCurrentAction("Running");
            }
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        emitUI.setText("InWorldSpace: " + emit.isInWorldSpace());
    }

    private void configCamera() {
        flyCam.setDragToRotate(true);
        flyCam.setMoveSpeed(10);

        cam.setLocation(new Vector3f(0, 6f, 9.2f));
        cam.lookAt(Vector3f.UNIT_Y, Vector3f.UNIT_Y);

        float aspect = (float) cam.getWidth() / cam.getHeight();
        cam.setFrustumPerspective(45, aspect, 0.1f, 1000f);
    }

    private void setupScene() {
        CenterQuad quad = new CenterQuad(12, 12);
        quad.scaleTextureCoordinates(new Vector2f(2, 2));
        Geometry floor = createMesh("Floor", quad);
        floor.rotate(-FastMath.HALF_PI, 0, 0);
        rootNode.attachChild(floor);
    }

    private Geometry createMesh(String name, Mesh mesh) {
        Geometry geo = new Geometry(name, mesh);
        Material mat = new Material(assetManager, Materials.LIGHTING);
        Texture tex = assetManager.loadTexture("Interface/Logo/Monkey.jpg");
        tex.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("DiffuseMap", tex);
        geo.setMaterial(mat);
        return geo;
    }

    private void setupLights() {
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);
        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White);
        //rootNode.addLight(ambient);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 4096, 3);
        dlsf.setLight(sun);
        dlsf.setShadowIntensity(0.4f);
        dlsf.setShadowZExtend(256);

        FXAAFilter fxaa = new FXAAFilter();
        BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(bloom);
        fpp.addFilter(dlsf);
        fpp.addFilter(fxaa);
        viewPort.addProcessor(fpp);
    }

    private BitmapText createTextUI(ColorRGBA color, float xPos, float yPos) {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Console.fnt");
        BitmapText bmp = new BitmapText(font);
        bmp.setSize(font.getCharSet().getRenderedSize());
        bmp.setLocalTranslation(xPos, settings.getHeight() - yPos, 0);
        bmp.setColor(color);
        guiNode.attachChild(bmp);
        return bmp;
    }

}
