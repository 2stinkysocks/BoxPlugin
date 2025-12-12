package me.twostinkysocks.boxplugin.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class RenderUtil {

    public static Vector rotateFunction(Vector v, Location loc) {
        double yawR = loc.getYaw()/180.0*Math.PI;
        double pitchR = loc.getPitch()/180*Math.PI;
        v = rotateAboutX(v, pitchR);
        v = rotateAboutY(v, -yawR);
        return v;
    }

    public static Vector rotateAboutX(Vector vect, double a) {
        double Y = Math.cos(a)*vect.getY() - Math.sin(a)*vect.getZ();
        double Z = Math.sin(a)*vect.getY() + Math.cos(a)*vect.getZ();
        return vect.setY(Y).setZ(Z);
    }

    public static Vector rotateAboutY(Vector vect, double b) {
        double x = Math.cos(b)*vect.getX() + Math.sin(b)*vect.getZ();
        double z = -Math.sin(b)*vect.getX() + Math.cos(b)*vect.getZ();
        return vect.setX(x).setZ(z);
    }

    public static Vector rotateAboutZ(Vector vect, double c) {
        double x = Math.cos(c)*vect.getX() - Math.sin(c)*vect.getY();
        double y = Math.sin(c)*vect.getX() + Math.cos(c)*vect.getY();
        return vect.setX(x).setY(y);
    }

}
