package me.macbarm.com.pkmoves.fire;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.MovementHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BlazingUppercut extends FireAbility implements AddonAbility {

    private final double dashDistance = getConfig().getDouble("macbarm.fire." + getName() + ".DashDistance");
    private final double damage = getConfig().getDouble("macbarm.fire." + getName() + ".Damage");
    private final long cooldown = getConfig().getLong("macbarm.fire." + getName() + ".Cooldown");
    private final double launchHeight = getConfig().getDouble("macbarm.fire." + getName() + ".LaunchHeight");
    private final long burnDuration = getConfig().getLong("macbarm.fire." + getName() + ".BurnDuration");
    private final long fireTrailDuration = getConfig().getLong("macbarm.fire." + getName() + ".FireTrailDuration");

    public enum State {
        STATE_DASH,
        STATE_HIT,
        STATE_REMOVE
    }

    private State state;

    private Location location;
    private Vector direction;
    private double distanceTravelled = 0;

    private Entity target;
    private BendingPlayer targetB;


    public BlazingUppercut(Player player) {
        super(player);

        addConfig();
        BlazingUppercut blazingUppercut = CoreAbility.getAbility(player, BlazingUppercut.class);
        if (blazingUppercut != null) return;
        if (!bPlayer.canBend(this)) return;
        if (!player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) return;

        location = player.getLocation();
        state = State.STATE_DASH;



        start();
    }

    @Override
    public void progress() {

        if (player.isDead() || !player.isOnline()) {
            state = State.STATE_REMOVE;
        }

        switch (state){
            case STATE_DASH -> {
                dashing(player.getLocation());
                break;
            }
            case STATE_HIT -> {
                upperCut(target, player.getLocation().getY() + launchHeight);
                break;
            }
            case STATE_REMOVE -> {
                remove();
                break;
            }

        }

    }

    @Override
    public boolean isSneakAbility() {
        return false;
    }

    @Override
    public void remove(){
        player.setFireTicks(0);
        bPlayer.addCooldown(this);
        super.remove();
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public String getName() {
        return "BlazingUppercut";
    }

    @Override
    public Location getLocation() {
        return player.getLocation();
    }

    @Override
    public void load() {

    }

    @Override
    public void stop() {

    }

    @Override
    public String getAuthor() {
        return "Macbarm";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    public void addConfig() {
        ConfigManager.defaultConfig.get().addDefault("macbarm.fire.BlazingUppercut.DashDistance", 6.0);
        ConfigManager.defaultConfig.get().addDefault("macbarm.fire.BlazingUppercut.Damage", 5.0);
        ConfigManager.defaultConfig.get().addDefault("macbarm.fire.BlazingUppercut.Cooldown", 7000);
        ConfigManager.defaultConfig.get().addDefault("macbarm.fire.BlazingUppercut.LaunchHeight", 5);
        ConfigManager.defaultConfig.get().addDefault("macbarm.fire.BlazingUppercut.BurnDuration", 3000);
        ConfigManager.defaultConfig.get().addDefault("macbarm.fire.BlazingUppercut.FireTrailDuration", 3000);
        ConfigManager.defaultConfig.save();
    }

    private void dashing(Location location) {
        player.setFireTicks(0);

        Vector dir = player.getLocation().getDirection();
        dir.setY(0);

        if (dir.lengthSquared() < 0.01) {
            float yaw = player.getLocation().getYaw();
            double radians = Math.toRadians(yaw);
            dir.setX(-Math.sin(radians));
            dir.setZ(Math.cos(radians));
        }

        direction = dir.normalize();


        if (distanceTravelled >= dashDistance){
            state = State.STATE_REMOVE;
            return;
        }

        GeneralMethods.setVelocity(this.player,direction.clone().multiply(1));
        distanceTravelled += player.getVelocity().length();
        playFirebendingParticles(GeneralMethods.getMainHandLocation(player), 10,.2,.2,.2);
        playFirebendingSound(location);

        if (location.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
           createTempFire(location, fireTrailDuration);


        } else {
            player.teleport(location.subtract(0,1,0));
        }

        for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 1)) {
            if (entity.equals(player)) continue;
            target = entity;
            player.swingMainHand();
            GeneralMethods.setVelocity(this,player,new Vector(0,0,0));
            TempBlock tempBlock = new TempBlock(player.getLocation().getBlock(),Material.AIR.createBlockData(), 2000);
            player.getWorld().playSound(target.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,2f,.5f);
            state = State.STATE_HIT;

        }

    }

    public void upperCut(Entity entity, double endHeight) {
        if (entity == null || !entity.isValid() ) {
            state = State.STATE_REMOVE;
            return;
        }

        if (entity instanceof LivingEntity) {

        GeneralMethods.setVelocity(this, entity, new Vector(0, 0.9, 0));

        DamageHandler.damageEntity(entity, damage, this);
        entity.setFireTicks((int) (burnDuration / 50));

        if (entity instanceof Player){
            targetB = BendingPlayer.getBendingPlayer((OfflinePlayer) entity);
            for (String ability : targetB.getAbilities().values()) {
                targetB.addCooldown(ability, 1000);
            }
        }

        Location loc = entity.getLocation().clone();
        double radius = entity.getWidth() + 0.2;
        double height = entity.getHeight() + 1;

        for (int i = 0; i < 20; i++) {
            double progress = (double) i / 20;
            double angle = progress * 3 * 2 * Math.PI;
            double y = progress * height;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location particleLoc = loc.clone().add(x, y, z);
            playFirebendingParticles(particleLoc, 3, 0, 0, 0);
        }

        playFirebendingSound(loc);

        if (entity.getLocation().getY() >= endHeight) {
            state = State.STATE_REMOVE;
        }
    } else {
            state = State.STATE_REMOVE;
        }
    }

    @Override
    public String getDescription() {
        return "BlazingUppercut is a firebending move that allows the user to dash forward and uppercut a target, "
                + "launching them into the air while surrounding them with a spiral of fire. "
                + "Enemies hit will take damage and be set on fire.";
    }


    @Override
    public String getInstructions() {
        return "Left-click while looking at your target to dash forward. "
                + "Make sure you are standing on solid ground. "
                + "The ability will hit the first entity in your path and launch them upwards "
                + "with a spiral of fire around them";
    }


}
