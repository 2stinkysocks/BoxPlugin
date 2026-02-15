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

            loc.getWorld().spawnParticle(Particle.DUST, loc.clone().add(point), 1, 0.03, 0.03, 0.03, dustOption);
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
            current.getWorld().spawnParticle(particle, current, 1, 0.03, 0.03, 0.03, speed);
        }
    }

    public static void renderDustLine(Location origin, Vector direction, Particle.DustOptions dustOption) {
        Location originClone = origin.clone();
        int points = (int) direction.length() * 6;

        Vector directionShort = direction.clone().normalize().divide(new Vector(6,6,6)); //.normalize to make it 1 block long, 8 points per block

        for(int i = 0; i < points; i++) {
            originClone.getWorld().spawnParticle(Particle.DUST, originClone, 1, 0, 0, 0, dustOption);
            originClone.add(directionShort);
        }
    }

    public static void renderParticleLine(Location origin, Vector direction, Particle particle, double speed) {
        Location originClone = origin.clone();
        int points = (int) direction.length() * 8;

        Vector directionShort = direction.normalize().divide(new Vector(8,8,8));

        for(int i = 0; i < points; i++) {
            originClone.getWorld().spawnParticle(particle, originClone, 1, 0, 0, 0, speed);
            originClone.add(directionShort);
        }
    }

    public static void renderParticleTube(Location origin, Vector direction, int circlePoints, double radius, Particle particle, double speed) {
        Vector dir = direction.clone().normalize();
        int points = Math.max(1, (int) (direction.length() * 8));
        Vector step = dir.clone().multiply(1.0 / 8.0);

        // Find a perpendicular basis
        Vector up = Math.abs(dir.getY()) < 0.99 ? new Vector(0, 1, 0) : new Vector(1, 0, 0);

        Vector right = dir.clone().crossProduct(up).normalize();
        Vector forward = right.clone().crossProduct(dir).normalize();

        Location point = origin.clone();

        for (int i = 0; i < points; i++) {
            for (int j = 0; j < circlePoints; j++) {
                double angle = 2 * Math.PI * j / circlePoints;

                Vector offset = right.clone().multiply(Math.cos(angle) * radius).add(forward.clone().multiply(Math.sin(angle) * radius));

                point.getWorld().spawnParticle(particle, point.clone().add(offset), 1, 0, 0, 0, speed);
            }
            point.add(step);
        }
    }

    public static void renderDustTube(Location origin, Vector direction, int circlePoints, double radius, Particle.DustOptions dustOption) {
        Vector dir = direction.clone().normalize();
        int points = Math.max(1, (int) (direction.length() * 7));
        Vector step = dir.clone().multiply(1.0 / 7.0);

        // Find a perpendicular basis
        Vector up = Math.abs(dir.getY()) < 0.99 ? new Vector(0, 1, 0) : new Vector(1, 0, 0);

        Vector right = dir.clone().crossProduct(up).normalize();
        Vector forward = right.clone().crossProduct(dir).normalize();

        Location point = origin.clone();

        for (int i = 0; i < points; i++) {
            for (int j = 0; j < circlePoints; j++) {
                double angle = 2 * Math.PI * j / circlePoints;

                Vector offset = right.clone().multiply(Math.cos(angle) * radius).add(forward.clone().multiply(Math.sin(angle) * radius));

                point.getWorld().spawnParticle(Particle.DUST, point.clone().add(offset), 1, 0, 0, 0, dustOption);
            }
            point.add(step);
        }
    }

    public static void renderParticleCYL(Location origin, Vector direction, int circlePoints, double radius, int particlesPerPoint, Particle particle, double speed) {
        Vector dir = direction.clone().normalize();
        Location originClone = origin.clone();
        int points = (int) direction.length() * 7;
        Vector directionShort = dir.divide(new Vector(7,7,7)); //.normalize to make it 1 block long, 8 points per block
        double interval = 2*Math.PI/circlePoints;
        for(int j = 0; j < points; j++){
            for(int i = 0; i < circlePoints; i++) {
                double t = i*interval;
                double x = radius * Math.cos(t);
                double z = radius * Math.sin(t);
                Location particleLoc = originClone.clone().add(x, 0, z);
                origin.getWorld().spawnParticle(particle, particleLoc, particlesPerPoint, 0.07, 0.05, 0.07,  speed);
            }
            originClone.add(directionShort);
        }
    }

    public static void renderDustCYL(Location origin, Vector direction, int circlePoints, double radius, int particlesPerPoint, Particle.DustOptions dustOption) {
        Vector dir = direction.clone().normalize();
        Location originClone = origin.clone();
        int points = (int) direction.length() * 8;
        Vector directionShort = dir.divide(new Vector(8,8,8)); //.normalize to make it 1 block long, 8 points per block
        double interval = 2*Math.PI/circlePoints;
        for(int j = 0; j < points; j++){
            for(int i = 0; i < circlePoints; i++) {
                double t = i*interval;
                double x = radius * Math.cos(t);
                double z = radius * Math.sin(t);
                Location particleLoc = originClone.clone().add(x, 0, z);
                origin.getWorld().spawnParticle(Particle.DUST, particleLoc, particlesPerPoint, 0.05, 0, 0.05,  dustOption);
            }
            originClone.add(directionShort);
        }
    }

    //this does not work, the clanker made it

    public static void renderParticleHelix(Location origin, Vector direction, double radius, double particlesPerRotation, Particle particle, double speed) {
        double lineLen = direction.length();
        Location originClone = origin.clone();

        Vector dir = direction.clone().normalize();

        // Calculate number of points along the line
        double totoalRevs = lineLen / 2.0; //2.0 means 2 blocks per rev
        int totalPoints = (int)Math.round(particlesPerRotation * totoalRevs);
        double step = lineLen / totalPoints;

        // Perpendicular basis for the helix
        Vector up = Math.abs(dir.getY()) < 0.99 ? new Vector(0,1,0) : new Vector(1,0,0);
        Vector right = dir.clone().crossProduct(up).normalize();
        Vector forward = right.clone().crossProduct(dir).normalize();

        for (int i = 0; i < totalPoints; i++) {
            double angle = Math.PI * 2 * (double)i / particlesPerRotation;
            double xOffset = radius * Math.cos(angle);
            double zOffset = radius * Math.sin(angle);
            Vector offset = right.clone().multiply(xOffset).add(forward.clone().multiply(zOffset));

            Location particleLoc = originClone.clone().add(dir.clone().multiply(step * (double)i)).add(offset);
            origin.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, speed);
        }
    }

}
