package com.legendsofvaleros.modules.characters.stat;

import com.google.common.collect.ImmutableMap;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityCreateEvent;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Keeps track of regenerating stats' levels across logins.
 */
public class PersistentRegeneratingStats {

  private static final RegeneratingStat[] STATS = RegeneratingStat.values();

  private interface RPC {

    Promise<Map<RegeneratingStat, Double>> getPlayerStats(CharacterId characterId);

    Promise<Boolean> savePlayerStats(CharacterId characterId, Map<RegeneratingStat, Double> map);
  }

  private RPC rpc;

  private Map<CharacterId, Map<RegeneratingStat, Double>> dataMap = new HashMap<>();

  public PersistentRegeneratingStats() {
    this.rpc = APIController.create(RPC.class);

    Characters.getInstance().registerEvents(new PlayerCharacterListener());
  }

  private Promise<Map<RegeneratingStat, Double>> onLogin(CharacterId characterId) {
    return rpc.getPlayerStats(characterId).onSuccess(val -> {
      dataMap.put(characterId, val.orElse(ImmutableMap.of()));
    });
  }

  private Promise<Boolean> onLogout(CharacterId characterId, CombatEntity ce) {
    Map<RegeneratingStat, Double> stats = new HashMap<>();

      for (RegeneratingStat stat : STATS) {
          stats.put(stat, ce.getStats().getRegeneratingStat(stat));
      }

    return rpc.savePlayerStats(characterId, stats);
  }

  /**
   * Listens to player-character initialization and logouts.
   */
  private class PlayerCharacterListener implements Listener {

    @EventHandler
    public void onPlayerCharacterStartLoading(PlayerCharacterStartLoadingEvent event) {
      PhaseLock lock = event.getLock("Stats");

      onLogin(event.getPlayerCharacter().getUniqueCharacterId()).on(lock::release);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCombatEntityCreate(final CombatEntityCreateEvent event) {
      if (event.getCombatEntity().isPlayer()) {
        Player player = (Player) event.getLivingEntity();

          if (!Characters.isPlayerCharacterLoaded(player)) {
              return;
          }

        PlayerCharacter current = Characters.getPlayerCharacter(player);

        // applies stat numbers to combat entity
        Map<RegeneratingStat, Double> data = dataMap.remove(current.getUniqueCharacterId());
        if (data != null) {
          for (RegeneratingStat stat : STATS) {
            Double fromDb = data.get(stat);
            if (fromDb != null) {
              event.getCombatEntity().getStats().setRegeneratingStat(stat, fromDb);
            }
          }
        }
      }
    }

    @EventHandler
    public void onPlayerCharacterLogout(PlayerCharacterLogoutEvent event) {
      PhaseLock lock = event.getLock("Stats");

      onLogout(event.getPlayerCharacter().getUniqueCharacterId(),
          CombatEngine.getInstance().getCombatEntity(event.getPlayer()))
          .on(lock::release);
    }
  }
}
