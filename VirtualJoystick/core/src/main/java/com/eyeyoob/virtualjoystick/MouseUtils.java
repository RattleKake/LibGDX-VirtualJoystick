package com.eyeyoob.virtualjoystick;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class MouseUtils {
    public static Vector2 getScreenCoordinateToWorldCoordinate(Camera camera, Vector2 coordinates){
        Vector3 newCoord = camera.unproject(new Vector3(coordinates.x, coordinates.y, 0));
        return new Vector2(newCoord.x, newCoord.y);
    }
}
