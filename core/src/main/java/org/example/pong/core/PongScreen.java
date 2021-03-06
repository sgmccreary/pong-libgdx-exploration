package org.example.pong.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.example.pong.core.events.TouchEvent;

import javax.inject.Inject;

public class PongScreen implements Screen {

    private static final float BOX_STEP = 1 / 60f;
    private static final int BOX_VELOCITY_ITERATIONS = 6;
    private static final int BOX_POSITION_ITERATIONS = 2;

    public static final int VIRTUAL_WIDTH = 1024;
    public static final int VIRTUAL_HEIGHT = 768;
    private static final float ASPECT_RATIO = (float) VIRTUAL_WIDTH / (float) VIRTUAL_HEIGHT;

    private final Bus bus;

    private World world;

    private Camera camera;

    private Rectangle viewport;

    private Box2DDebugRenderer debugRenderer;

    @Inject
    public PongScreen(Bus bus) {
        super();
        this.bus = bus;
    }

    @Override
    public void show() {
        this.bus.register(this);
        this.world = new World(new Vector2(0, 0), true);
        this.viewport = new Rectangle(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        this.camera = this.createCamera();
        this.debugRenderer = new Box2DDebugRenderer();

        this.createBall(this.world, 0f, 0f);

        this.createWall(this.world, -this.viewport.width * 0.5f, this.viewport.height * 0.5f, 5f, this.viewport.height);
        this.createWall(this.world,  this.viewport.width * 0.5f, this.viewport.height * 0.5f, 5f, this.viewport.height);

        this.createWall(this.world, -this.viewport.width * 0.5f,  this.viewport.height * 0.5f, this.viewport.width, 5f);
        this.createWall(this.world, -this.viewport.width * 0.5f, -this.viewport.height * 0.5f, this.viewport.width, 5f);
    }

    @Override
    public void render(float delta) {
        this.camera.update();
        Gdx.gl.glViewport((int) viewport.x, (int) viewport.y, (int) viewport.width, (int) viewport.height);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        this.debugRenderer.render(this.world, this.camera.combined);
        this.world.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);
    }

    @Subscribe
    public void onTouch(TouchEvent touchEvent) {
        Gdx.app.log("TOUCH", "x = " + touchEvent.getScreenX());
    }

    @Override
    public void resize(int width, int height) {
        float aspectRatio = (float) width / (float) height;

        float scale = 1f;
        Vector2 crop = new Vector2(0f, 0f);
        if (aspectRatio > ASPECT_RATIO) {
            scale = (float) height / (float) VIRTUAL_HEIGHT;
            crop.x = (width - VIRTUAL_WIDTH * scale) / 2f;
        } else if (aspectRatio < ASPECT_RATIO) {
            scale = (float) width / (float) VIRTUAL_WIDTH;
            crop.y = (height - VIRTUAL_HEIGHT * scale) / 2f;
        } else {
            scale = (float) width / (float) VIRTUAL_WIDTH;
        }

        float w = (float) VIRTUAL_WIDTH * scale;
        float h = (float) VIRTUAL_HEIGHT * scale;
        this.viewport = new Rectangle(crop.x, crop.y, w, h);
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        this.bus.unregister(this);
    }

    private Camera createCamera() {
        Camera camera = new OrthographicCamera(1024, 768);
//        camera.position.set(camera.viewportWidth * 0.5f, camera.viewportHeight * 0.5f, 0f);
//        camera.update();
        return camera;
    }

    public void createBall(World world, float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.allowSleep = true;
        bodyDef.linearVelocity.set(100f, 50f);
        Body body = world.createBody(bodyDef);
        CircleShape dynamicCircle = new CircleShape();
        dynamicCircle.setRadius(5f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicCircle;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 1;
        body.createFixture(fixtureDef);
    }

    private void createWall(World world, float x, float y, float w, float h) {
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(new Vector2(x, y));
        Body groundBody = world.createBody(groundBodyDef);
        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(w, h);
        groundBody.createFixture(groundBox, 0.0f);
    }
}
