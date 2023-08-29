package net.punchtree.melee.game;

import net.punchtree.minigames.game.PvpGame;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.UUID;

public interface MeleeLikeGame extends PvpGame {

    boolean hasPlayer(UUID entityId);

    default boolean hasPlayer(Player player) {
        return hasPlayer(player.getUniqueId());
    }

    boolean removePlayerFromGame(Player player);

    void handleKill(Player killer, Player killed, EntityDamageByEntityEvent edbee);

    void handleDeath(Player killed, EntityDamageEvent e);
}
