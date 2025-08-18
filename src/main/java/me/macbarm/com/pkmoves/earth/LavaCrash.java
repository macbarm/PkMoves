package me.macbarm.com.pkmoves.earth;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.LavaAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempFallingBlock;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class LavaCrash extends LavaAbility implements AddonAbility, ComboAbility {

    private enum Phase { CRASH, MAGMA, LAVA, AIR, RESTORE }

    private Phase phase = Phase.CRASH;
    private long phaseStartTime;
    private final long cooldown;
    private final long magmaDelay;
    private final long lavaDelay;
    private final long airDelay;
    private final long restoreDelay;

    private final long magmaPhaseTime = 2000;
    private final long lavaPhaseTime = 2000;
    private final long airPhaseTime = 2000;

    private int blocksPerTick;
    private final int maxBlocks;
    private Location origin;
    private final List<Block> blocks = new ArrayList<>();
    private final List<Block> sortedBlocks = new ArrayList<>();
    private final List<TempBlock> tempBlocks = new ArrayList<>();
    private int spreadTick = 0;
    private boolean finishedSpreading = false;
    private int effectIndex = 0;
    private final Map<Block, Long> buildUpTimes = new HashMap<>();

    public LavaCrash(Player player, Block block, BlockFace face) {
        super(player);
        addConfig();
        cooldown = getConfig().getLong("macbarm.earth." + getName() + ".Cooldown");
        magmaDelay = getConfig().getLong("macbarm.earth." + getName() + ".MagmaDelay");
        lavaDelay = getConfig().getLong("macbarm.earth." + getName() + ".LavaDelay");
        airDelay = getConfig().getLong("macbarm.earth." + getName() + ".AirDelay");
        restoreDelay = getConfig().getLong("macbarm.earth." + getName() + ".RestoreDelay");
        maxBlocks = getConfig().getInt("macbarm.earth." + getName() + ".MaxBlocks");
        blocksPerTick = 1;

        if (block == null || face == BlockFace.UP) return;
        if (!block.getRelative(BlockFace.UP).getType().isSolid()) return;
        if (!isEarthbendable(block)) return;
        if (bPlayer.isOnCooldown(this)) return;
        LavaCrash lavaCrash = CoreAbility.getAbility(player, LavaCrash.class);
        if (lavaCrash != null) return;

        origin = block.getLocation();
        blocks.add(block);
        start();
    }

    @Override
    public void progress() {
        if (player.isDead() || !player.isOnline()) {
            cleanupTempBlocks();
            remove();
            return;
        }

        switch (phase) {
            case CRASH:
                if (!finishedSpreading) finishedSpreading = checkBlocks();
                if (blocks.size() >= maxBlocks || finishedSpreading) {
                    sortedBlocks.addAll(blocks);
                    sortedBlocks.sort(Comparator.comparingInt(Block::getY));
                    phase = Phase.MAGMA;
                    phaseStartTime = System.currentTimeMillis();
                    effectIndex = 0;
                    setPhaseSpeed(sortedBlocks.size(), magmaPhaseTime);
                }
                break;

            case MAGMA:
                if (System.currentTimeMillis() - phaseStartTime >= magmaDelay) {
                    int count = 0;
                    while (effectIndex < sortedBlocks.size() && count < blocksPerTick) {
                        Block b = sortedBlocks.get(effectIndex);
                        TempBlock tb = new TempBlock(b, Material.MAGMA_BLOCK.createBlockData(), lavaDelay + airDelay + restoreDelay);
                        tempBlocks.add(tb);
                        b.getWorld().spawnParticle(Particle.FLAME, b.getLocation().add(0.5, 1, 0.5), 10, 0.3, 0.5, 0.3, 0.01);
                        b.getWorld().spawnParticle(Particle.REDSTONE, b.getLocation().add(0.5, 0.5, 0.5), 3, 0.2, 0.2, 0.2, new Particle.DustOptions(Color.ORANGE, 1));
                        b.getWorld().playSound(b.getLocation(), Sound.BLOCK_BASALT_BREAK, 0.8f, 0.8f);
                        b.getWorld().playSound(b.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 0.5f, 1.2f);
                        effectIndex++;
                        count++;
                    }
                    if (effectIndex >= sortedBlocks.size()) {
                        phase = Phase.LAVA;
                        phaseStartTime = System.currentTimeMillis();
                        effectIndex = 0;
                        setPhaseSpeed(tempBlocks.size(), lavaPhaseTime);
                    }
                }
                break;

            case LAVA:
                if (System.currentTimeMillis() - phaseStartTime >= lavaDelay) {
                    int count = 0;
                    while (effectIndex < tempBlocks.size() && count < blocksPerTick) {
                        TempBlock tb = tempBlocks.get(effectIndex);
                        Block block = tb.getBlock();
                        block.getWorld().spawnParticle(Particle.DRIP_LAVA, block.getLocation().add(1.5, 0.8, 1.5), 2, 0.2, 0.5, 0.2, 0.02);
                        block.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, block.getLocation().add(0.5, 0.8, 0.5), 3, 0.1, 0.2, 0.1, 0.05);
                        if (!buildUpTimes.containsKey(block)) buildUpTimes.put(block, System.currentTimeMillis());
                        block.getWorld().spawnParticle(Particle.LAVA, block.getLocation().add(0.5, 0.5, 0.5), 4, 0.4, 0.4, 0.4, 0.05);
                        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_LAVA_POP, 0.9f, 0.9f);
                        block.getWorld().playSound(block.getLocation(), Sound.ITEM_BUCKET_EMPTY_LAVA, 0.3f, 0.5f);
                        effectIndex++;
                        count++;
                    }
                    if (effectIndex >= tempBlocks.size()) {
                        phase = Phase.AIR;
                        phaseStartTime = System.currentTimeMillis();
                        effectIndex = 0;
                        buildUpTimes.clear();
                        setPhaseSpeed(tempBlocks.size(), airPhaseTime);
                    }
                }
                break;

            case AIR:
                if (System.currentTimeMillis() - phaseStartTime >= airDelay) {
                    int count = 0;
                    int reverseIndex = tempBlocks.size() - 1 - effectIndex;

                    while (effectIndex < tempBlocks.size() && count < blocksPerTick) {
                        TempBlock tb = tempBlocks.get(reverseIndex);
                        Block block = tb.getBlock();

                        new TempFallingBlock(block.getLocation().add(0.5, 0, 0.5), Material.MAGMA_BLOCK.createBlockData(), new Vector(0, 0.1, 0), this);

                        tb.setType(Material.AIR);
                        block.getWorld().spawnParticle(Particle.CLOUD, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 0.02);
                        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 1);

                        effectIndex++;
                        reverseIndex--;
                        count++;
                    }

                    if (effectIndex >= tempBlocks.size()) {
                        phase = Phase.RESTORE;
                        phaseStartTime = System.currentTimeMillis();
                        effectIndex = 0;
                    }
                    bPlayer.addCooldown(this);
                }
                break;

            case RESTORE:
                if (System.currentTimeMillis() - phaseStartTime >= restoreDelay) {
                    cleanupTempBlocks();
                    bPlayer.addCooldown(this);
                    remove();
                }
                break;
        }
    }

    private void setPhaseSpeed(int totalBlocks, long targetMs) {
        int ticks = (int) Math.ceil(targetMs / 50.0);
        blocksPerTick = Math.max(1, (int) Math.ceil((double) totalBlocks / ticks));
    }

    private boolean checkBlocks() {
        spreadTick++;
        if (spreadTick == 3) {
            BlockFace[] faces = {BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
            if (blocks.size() < maxBlocks) {
                List<Block> newBlocks = new ArrayList<>();
                for (Block block : blocks) {
                    for (BlockFace face : faces) {
                        Block relative = block.getRelative(face);
                        if (!blocks.contains(relative) && !newBlocks.contains(relative) && isEarthbendable(relative)) {
                            newBlocks.add(relative);
                            relative.getWorld().spawnParticle(Particle.SMOKE_LARGE, relative.getLocation().add(0.5, 0.5, 0.5), 3, 0.3, 0.3, 0.3, 0.01);
                            relative.getWorld().playSound(relative.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, 0.2f, 3.2f);
                        }
                    }
                }
                blocks.addAll(newBlocks);
                spreadTick = 0;
                return newBlocks.isEmpty();
            } else {
                spreadTick = 0;
                return true;
            }
        }
        return false;
    }

    private void cleanupTempBlocks() {
        for (TempBlock tb : tempBlocks) tb.revertBlock();
        tempBlocks.clear();
    }

    @Override
    public boolean isSneakAbility() { return false; }
    @Override
    public boolean isHarmlessAbility() { return false; }
    @Override
    public long getCooldown() { return cooldown; }
    @Override
    public String getName() { return "LavaCrash"; }
    @Override
    public Location getLocation() { return origin; }
    @Override
    public void load() { addConfig(); }
    @Override
    public void stop() { cleanupTempBlocks(); }
    @Override
    public String getAuthor() { return "Macbarm"; }
    @Override
    public String getVersion() { return "1.0"; }
    @Override
    public boolean isHiddenAbility() { return true; }

    @Override
    public Object createNewComboInstance(Player player) {
        Block block = getTargetEarthBlock(player,5);
        BlockFace face = block.getFace(block);
        if (face == BlockFace.UP || face == BlockFace.SELF) face = BlockFace.NORTH;
        return new LavaCrash(player, block, face);
    }

    @Override
    public ArrayList<ComboManager.AbilityInformation> getCombination() {
        ArrayList<ComboManager.AbilityInformation> combo = new ArrayList<>();
        combo.add(new ComboManager.AbilityInformation("Collapse", ClickType.RIGHT_CLICK_BLOCK));
        combo.add(new ComboManager.AbilityInformation("Collapse", ClickType.RIGHT_CLICK_BLOCK));
        combo.add(new ComboManager.AbilityInformation("LavaFlow", ClickType.RIGHT_CLICK_BLOCK));
        return combo;
    }

    public void addConfig() {
        String path = "macbarm.earth." + getName() + ".";
        ConfigManager.defaultConfig.get().addDefault(path + "Cooldown", 5000);
        ConfigManager.defaultConfig.get().addDefault(path + "MagmaDelay", 1000);
        ConfigManager.defaultConfig.get().addDefault(path + "LavaDelay", 2000);
        ConfigManager.defaultConfig.get().addDefault(path + "AirDelay", 6000);
        ConfigManager.defaultConfig.get().addDefault(path + "RestoreDelay", 20000);
        ConfigManager.defaultConfig.get().addDefault(path + "MaxBlocks", 400);
        ConfigManager.defaultConfig.save();
    }

    @Override
    public String getDescription() {
        return "LavaCrash is a devastating lava-based Earthbending technique. "
                + "Smash the wall to ignite surrounding earth blocks into magma, "
                + "create flowing lava, and launch molten blocks into the air, "
                + "leaving a fiery spectacle in your wake.";
    }

    @Override
    public String getInstructions(){
        return "RightClick (Collapse) -> RightClick (Collapse) -> RightClick (LavaFlow)";
    }

}
