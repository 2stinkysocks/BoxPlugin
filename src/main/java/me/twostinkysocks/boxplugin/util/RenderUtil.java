package me.twostinkysocks.boxplugin.util;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
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

    public static void renderDustOrb(Location loc, int amount, double radius, Particle.DustOptions dustOption){
        double phi = (Math.sqrt(5) + 1) / 2;
        double goldenAngle = 2 * Math.PI * (phi - 1);

        for (int i = 0; i < amount; i++) {
            double z = 1 - (2.0 * i) / (amount - 1);
            double r = Math.sqrt(1 - z * z);
            double theta = i * goldenAngle;

            double x = r * Math.cos(theta);
            double y = r * Math.sin(theta);

            Vector point = new Vector(x, y, z).multiply(radius);

            loc.getWorld().spawnParticle(Particle.DUST, loc.clone().add(point), 1, 0, 0, 0, dustOption);
        }
    }

    public static void renderParticleOrb(Location loc, int amount, double radius, Particle particle, double speed){
        double phi = (Math.sqrt(5) + 1) / 2;
        double goldenAngle = 2 * Math.PI * (phi - 1);

        for (int i = 0; i < amount; i++) {
            double z = 1 - (2.0 * i) / (amount - 1);
            double r = Math.sqrt(1 - z * z);
            double theta = i * goldenAngle;

            double x = r * Math.cos(theta);
            double y = r * Math.sin(theta);

            Vector point = new Vector(x, y, z).multiply(radius);
            Location current = loc.clone().add(point);
            current.getWorld().spawnParticle(particle, current, amount, 0, 0, 0, speed);
        }
    }

}
