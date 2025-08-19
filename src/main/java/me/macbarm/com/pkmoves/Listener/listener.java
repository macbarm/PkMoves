package me.macbarm.com.pkmoves.Listener;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;

import com.projectkorra.projectkorra.ability.LavaAbility;
import me.macbarm.com.pkmoves.air.AirKick;
import me.macbarm.com.pkmoves.air.AirSlide;
import me.macbarm.com.pkmoves.earth.LavaCrash;
import me.macbarm.com.pkmoves.fire.BlazingUppercut;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
//import me.macbarm.com.pkmoves.chi.DripLeafDash;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class listener implements Listener {

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event){
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);



        if (bPlayer.canBend(CoreAbility.getAbility("AirKick"))){
            new AirKick(player);
        }

        if (bPlayer.canBend(CoreAbility.getAbility("AirSlide"))){
            new AirSlide(player);
        }

    }

    @EventHandler
    public void onClick(PlayerInteractEvent event){
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);


        AirKick airKick = CoreAbility.getAbility(player, AirKick.class);
        if (airKick != null){
            airKick.click();
        }

        AirSlide airSlide = CoreAbility.getAbility(player, AirSlide.class);
        if (airSlide != null){
            airSlide.click();
        }

        if (bPlayer.getBoundAbility().getName().equals("BlazingUppercut")){
            new BlazingUppercut(player);
        }



    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event){
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        Block source = event.getClickedBlock();
        BlockFace blockFace = event.getBlockFace();


        if (bPlayer.canBend(CoreAbility.getAbility("LavaCrash"))){
            new LavaCrash(player, source, blockFace);
        }

    }


}
