package com.eyeyoob.virtualjoystick;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import static com.eyeyoob.virtualjoystick.MouseUtils.getScreenCoordinateToWorldCoordinate;

class Joystick {
    // Initialize variables
    Vector2 parentPosition;
    float radius;

    Vector2 thumbstickPosition;
    float thumbstickSize;

    float horizontalInput = 0;
    float verticalInput = 0;

    boolean isPressed = false;


    public Joystick(float x, float y, float radius) {
        // Set values
        this.parentPosition = new Vector2(x, y);
        this.radius = radius;

        this.thumbstickPosition = new Vector2(this.parentPosition.x, this.parentPosition.y);
        this.thumbstickSize = 20;
    }

    // Runs every frame
    public void update(Vector2 touchPosition) {
        boolean isInJoystick = touchPosition.dst(parentPosition) <= radius;

        // Detect if the joystick is pressed or not
        if (Gdx.input.justTouched() && isInJoystick) {isPressed = true;}
        else if (!Gdx.input.isTouched()) {isPressed = false;}

        // Change the position of the thumbsticks
        if (isPressed) {
            // Follow the touchPosition
            if (isInJoystick) {
                thumbstickPosition = touchPosition;
            }
            // If the touchPosition is outside the joystick, it should still follow but don't leave it's radius
            else {
                float angle = angleToPoint(parentPosition, touchPosition);
                thumbstickPosition.x = (float) (parentPosition.x + Math.cos(angle) * radius);
                thumbstickPosition.y = (float) (parentPosition.y + Math.sin(angle) * radius);
            }
        }
        // Reset thumbstick position
        else {thumbstickPosition = parentPosition;}

        // Update horizontalInput and verticalInput
        horizontalInput = (thumbstickPosition.x - parentPosition.x) / radius;
        verticalInput = (thumbstickPosition.y - parentPosition.y) / radius;

        System.out.println(getThumbstickAngle());
    }

    // Draws every frame
    public void draw(ShapeRenderer shapeRenderer) {
        // Draw joystick background
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.circle(parentPosition.x, parentPosition.y, radius);

        // Draw thumbstick
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(thumbstickPosition.x, thumbstickPosition.y, thumbstickSize);
    }

    // Gets the horizontalInput value
    public float getHorizontalInput() {return horizontalInput;}

    // Gets the verticalInput value
    public float getVerticalInput() {return verticalInput;}

    // Set the position for the joystick
    public void setPosition(float x, float y) {
        this.parentPosition.x = x;
        this.parentPosition.y = y;
    }

    // Gets the angle of the thumbstick
    public float getThumbstickAngle() {
        float localThumbstickX = thumbstickPosition.x - parentPosition.x;
        float localThumbstickY = thumbstickPosition.y - parentPosition.y;
        return new Vector2(localThumbstickX, localThumbstickY).angleDeg();
    }

    // Return the radius of the thumbstick
    public float getRadius() {return radius;}

    // Return the angle of a vector
    private float angle(Vector2 vector) {return MathUtils.atan2(vector.y, vector.x);}

    // Return the point of the angle
    private float angleToPoint(Vector2 vectorA, Vector2 vectorB) {
        return angle(new Vector2(vectorB.x - vectorA.x, vectorB.y - vectorA.y));
    }

}


class Player {
    // Initialize variables
    public VirtualJoystick game;

    public float x, y;

    public float moveSpeed = 4;

    Texture texture;
    Sprite sprite;

    public Player(VirtualJoystick game, float x, float y) {
        // Set values
        this.game = game;

        this.x = x;
        this.y = y;

        this.texture = new Texture("player.png");
        this.sprite = new Sprite(this.texture);

        // Set sprite properties
        sprite.setPosition(x, y);
        sprite.setOrigin((float) texture.getWidth() / 2, (float) texture.getHeight() / 2);
        sprite.setCenter((float) texture.getWidth() / 2, (float) texture.getHeight() / 2);
        sprite.setRotation(0);
    }

    // Runs every frame
    public void update() {
        // Moving around
        x += game.joystick.getHorizontalInput() * moveSpeed;
        y += game.joystick.getVerticalInput() * moveSpeed;

        // Update sprite
        sprite.setPosition(x, y);
        sprite.setRotation(game.joystick.getThumbstickAngle());
    }

    // Draws every frame
    public void draw(SpriteBatch spriteBatch) {
        sprite.draw(spriteBatch);
    }
}



public class VirtualJoystick extends ApplicationAdapter {
    // Initialize variables
    ShapeRenderer shapeRenderer;
    SpriteBatch spriteBatch;

    Camera camera;
    Viewport viewport;

    Joystick joystick;
    Player player;

    @Override
    public void create() {
        // Set values
        this.shapeRenderer = new ShapeRenderer();
        this.spriteBatch = new SpriteBatch();

        this.camera = new OrthographicCamera();
        this.viewport = new ExtendViewport(640, 480, camera);

        this.joystick = new Joystick(0, 0, 50);
        this.player = new Player(this, 0, 100);
    }

    @Override
    public void render() {
        // Set the clear color to grey
        ScreenUtils.clear(Color.GRAY);

        // Update player
        player.update();

        // Update joystick
        Vector2 mouseCoord = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        Vector2 mouseToWorld = getScreenCoordinateToWorldCoordinate(camera, mouseCoord);
        joystick.update(mouseToWorld);

        // Keep joystick to the side
        float joystickX = (-viewport.getWorldWidth() / 2) + joystick.getRadius();
        float joystickY = (-viewport.getWorldHeight() / 2) + joystick.getRadius();
        joystick.setPosition(joystickX, joystickY);

        // Draw player
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        player.draw(spriteBatch);
        spriteBatch.end();

        // Draw joystick
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        joystick.draw(shapeRenderer);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        // Dispose these before closing the game
        shapeRenderer.dispose();
        spriteBatch.dispose();
    }

    @Override
    public void resize(int width, int height) {
        // Resize the viewport when the window is resized
        viewport.update(width, height);
        viewport.apply();
    }
}
