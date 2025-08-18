package me.macbarm.com.pkmoves.air;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class AirSlide extends AirAbility implements AddonAbility {

    private final double slideSpeed = getConfig().getDouble("macbarm.air." + getName() + ".SlideSpeed");
    private final double leapSpeed = getConfig().getDouble("macbarm.air." + getName() + ".LeapSpeed");
    private final double leapRange = getConfig().getDouble("macbarm.air." + getName() + ".LeapRange");
    private final long cooldown = getConfig().getLong("macbarm.air." + getName() + ".Cooldown");

    private double distanceTravelled = 0;
    private double currPoint;
    private double currPoint2 = 180;

    private Location location;
    private Vector direction;

    public enum State {
        STATE_START,
        STATE_SLIDE,
        STATE_LEAPING
    }

    private State state;

    public AirSlide(Player player) {
        super(player);
        AirSlide airSlide = CoreAbility.getAbility(player, AirSlide.class);
        if (airSlide != null) return;
        addConfig();
        state = State.STATE_START;
        start();
    }

    @Override
    public void progress() {
        switch (state) {
            case STATE_START:
                checkWalls();
                break;
            case STATE_SLIDE:
                checkWalls();
                slide();
                break;
            case STATE_LEAPING:
                startLeap();
                break;
            default:
                remove();
                break;
        }
        if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid() && state != State.STATE_START) {
            remove();
        }
    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    public void addConfig() {
        String path = "macbarm.air." + getName() + ".";
        ConfigManager.defaultConfig.get().addDefault(path + "SlideSpeed", 0.3);
        ConfigManager.defaultConfig.get().addDefault(path + "LeapSpeed", 0.6);
        ConfigManager.defaultConfig.get().addDefault(path + "LeapRange", 30);
        ConfigManager.defaultConfig.get().addDefault(path + "Cooldown", 8000);
        ConfigManager.defaultConfig.save();
    }

    @Override
    public void remove() {
        bPlayer.addCooldown(this);
        super.remove();
    }

    @Override
    public String getName() {
        return "AirSlide";
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void load() {
        addConfig();
    }

    @Override
    public void stop() { }

    @Override
    public String getAuthor() {
        return "Macbarm";
    }

    @Override
    public String getVersion() {
        return "1.1";
    }

    private void checkWalls() {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        Location loc = player.getLocation().clone();
        boolean hasWall = false;

        for (BlockFace face : faces) {
            Block block = loc.getBlock().getRelative(face);

            if (block.getType().isSolid()) {
                hasWall = true;
                player.getWorld().spawnParticle(Particle.BLOCK_DUST, block.getLocation(), 10, 1, 1, 1, block.getBlockData());
                if (state == State.STATE_START) {
                    state = State.STATE_SLIDE;
                }
            }
        }

        if (state == State.STATE_SLIDE && !hasWall) {
            remove();
        }
    }

    private void slide() {
        playAirbendingParticles(player.getLocation(), 1, 0.4, 0.4, 0.4);
        playAirbendingSound(player.getLocation());
        if (!player.isSneaking()) {
            GeneralMethods.setVelocity(this, player, new Vector(0, -slideSpeed, 0));
        } else {
            GeneralMethods.setVelocity(this, player, new Vector(0, 0, 0));
        }
    }

    private void startLeap() {
        playParticleRing(60, .5f, 30, GeneralMethods.getOffHandLocation(player));
        playParticleRing2(60, .5f, 30, GeneralMethods.getMainHandLocation(player));

        location.add(direction.clone().multiply(leapSpeed));
        GeneralMethods.setVelocity(this, player, direction.clone().multiply(leapSpeed));

        distanceTravelled += direction.length();

        if (distanceTravelled >= leapRange || location.getBlock().getType().isSolid()) {
            remove();
        }

        playAirbendingSound(player.getLocation());
        playAirbendingParticles(location,1,.2,.2,.2);


        if (player.isSneaking()) remove();
    }

    private void playParticleRing(int points, float size, int speed, Location location) {
        playAirbendingSound(location);
        Location tempLoc = location.clone();
        currPoint += 360.0 / points;

        double angle = currPoint * Math.PI / 0.6;
        Vector vec = GeneralMethods.getOrthogonalVector(tempLoc.getDirection(), angle, 30);
        vec.normalize().multiply(size);

        Location loc = tempLoc.add(vec);
        ParticleEffect.EXPLOSION_NORMAL.display(loc, 2, 0.1, 0.1, 0.1, location.toVector().normalize().multiply(0.2));
    }

    private void playParticleRing2(int points, float size, int speed, Location location) {
        Location tempLoc = location.clone();
        currPoint2 += 360.0 / points;

        double angle = currPoint2 * Math.PI / 0.6;
        Vector vec = GeneralMethods.getOrthogonalVector(tempLoc.getDirection(), angle, 30);
        vec.normalize().multiply(size);

        Location loc = tempLoc.add(vec);
        Vector dir = location.toVector().subtract(loc.toVector()).normalize();
        ParticleEffect.EXPLOSION_NORMAL.display(loc, 2, 0, 0, 0, dir.multiply(speed));
    }

    public void click() {
        if (state == State.STATE_SLIDE) {
            location = player.getLocation();
            direction = player.getLocation().getDirection();
            state = State.STATE_LEAPING;
        }
    }

    @Override
    public boolean isEnabled(){
        return false;
    }
}
