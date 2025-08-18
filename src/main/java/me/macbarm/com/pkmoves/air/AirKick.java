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
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class AirKick extends AirAbility implements AddonAbility {

    private final int jumpHeight = getConfig().getInt("macbarm.air." + getName() + ".JumpHeight");
    private final double range = getConfig().getDouble("macbarm.air." + getName() + ".Range");
    private final long cooldown = getConfig().getLong("macbarm.air." + getName() + ".Cooldown");
    private final double damage = getConfig().getDouble("macbarm.air." + getName() + ".Damage");

    private double startHeight;
    private double endHeight;
    private double distanceTravelled = 0;
    private double currPoint;
    private double currPoint2 = 180;

    private Location location;
    private Vector direction;

    public enum State {
        STATE_START,
        STATE_HOVER,
        STATE_SHOOTING
    }

    private State state;

    public AirKick(Player player) {
        super(player);
        AirKick airKick = CoreAbility.getAbility(player, AirKick.class);
        if (airKick != null) return;
        addConfig();
        state = State.STATE_START;
        startHeight = player.getLocation().getY();
        endHeight = startHeight + jumpHeight;
        start();
    }

    @Override
    public void progress() {
        switch (state) {
            case STATE_START:
                startJump();
                break;
            case STATE_HOVER:
                hover();
                break;
            case STATE_SHOOTING:
                createKick();
                break;
            default:
                remove();
                break;
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
        ConfigManager.defaultConfig.get().addDefault("macbarm.air.AirKick.Damage", 6);
        ConfigManager.defaultConfig.get().addDefault("macbarm.air.AirKick.Range", 40);
        ConfigManager.defaultConfig.get().addDefault("macbarm.air.AirKick.Cooldown", 8000);
        ConfigManager.defaultConfig.get().addDefault("macbarm.air.AirKick.JumpHeight", 5);
        ConfigManager.defaultConfig.save();
    }

    @Override
    public void remove() {
        bPlayer.addCooldown(this);
        super.remove();
    }

    @Override
    public String getName() {
        return "AirKick";
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void load() { addConfig(); }

    @Override
    public void stop() { }

    @Override
    public String getAuthor() {
        return "Macbarm";
    }

    @Override
    public String getVersion() {
        return "";
    }

    private void startJump() {
        playAirbendingParticles(player.getLocation(), 5, 0.4, 0.4, 0.4);
        playAirbendingSound(player.getLocation());
        player.setVelocity(new Vector(0, 1, 0));

        if (player.getLocation().getY() >= endHeight) {
            state = State.STATE_HOVER;
        }
    }

    private void hover() {
        playAirbendingParticles(player.getLocation(), 5, 0.4, 0.4, 0.4);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20, 5));

        if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
            remove();
        }
    }

    private void createKick() {
        playParticleRing(60, 1, 30, location);
        playParticleRing2(60, 1, 30, location);

        location.add(direction);
        distanceTravelled += direction.length();

        for (Entity target : GeneralMethods.getEntitiesAroundPoint(location, 1.2)) {
            if (!target.getUniqueId().equals(player.getUniqueId())) {
                target.setVelocity(direction.multiply(2));
                DamageHandler.damageEntity(target, damage, this);
                remove();
                return;
            }
        }

        if (distanceTravelled >= range || location.getBlock().getType().isSolid()) {
            remove();
        }
    }

    public void click() {
        if (state == State.STATE_HOVER) {
            location = player.getLocation();
            direction = player.getLocation().getDirection();
            state = State.STATE_SHOOTING;
            player.setVelocity(player.getLocation().getDirection().multiply(-1.5));
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
        }
    }

    private void playParticleRing(int points, float size, int speed, Location location) {
        playAirbendingSound(location);
        Location tempLoc = location.clone();
        currPoint += 360.0 / points;

        double angle = currPoint * Math.PI / 0.6;
        Vector vec = GeneralMethods.getOrthogonalVector(tempLoc.getDirection(), angle, 30);
        vec.normalize().multiply(size);

        Location loc = tempLoc.add(vec);
        playAirbendingParticles(loc,3,.1,.1,.1);
    }

    private void playParticleRing2(int points, float size, int speed, Location location) {
        Location tempLoc = location.clone();
        currPoint2 += 360.0 / points;

        double angle = currPoint2 * Math.PI / 0.6;
        Vector vec = GeneralMethods.getOrthogonalVector(tempLoc.getDirection(), angle, 30);
        vec.normalize().multiply(size);

        Location loc = tempLoc.add(vec);
        playAirbendingParticles(loc,3,.1,.1,.1);
    }
}