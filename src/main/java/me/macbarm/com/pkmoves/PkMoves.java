package me.macbarm.com.pkmoves;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import me.macbarm.com.pkmoves.Listener.listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class PkMoves extends JavaPlugin {

    public static PkMoves plugin;




    @Override
    public void onEnable() {
        plugin = this;
        CoreAbility.registerPluginAbilities(this, "me.macbarm.com");
        plugin.getServer().getPluginManager().registerEvents(new listener(), plugin);


    }

    @Override
    public void onDisable() {

    }
}
