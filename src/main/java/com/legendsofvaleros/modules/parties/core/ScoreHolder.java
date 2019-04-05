package com.legendsofvaleros.modules.parties.core;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.util.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import static com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat.HEALTH;

/**
 * Created by Crystall on 04/05/2019
 */
public class ScoreHolder {

    String name;

    CharacterId uuid;
    boolean online = false;
    boolean current = false;

    int health = 0;
    int energyMana = 0;

    public ScoreHolder(CharacterId uuid) {
        this.uuid = uuid;

        name = Bukkit.getOfflinePlayer(uuid.getPlayerId()).getName();
        if (name == null) {
            name = "Unknown Player";

            PlayerData.get(uuid.getPlayerId()).onSuccess(val -> {
                if (!val.isPresent()) return;
                name = val.get().username;
            });
        }

        update();
    }

    public void update() {
        PlayerCharacter pc = Characters.getInstance().getCharacter(uuid);

        if (pc != null) {
            name = pc.getPlayer().getName();

            online = true;
            current = pc.isCurrent();

            CombatEntity ce = CombatEngine.getEntity(pc.getPlayer());
            if (ce != null) {
                this.health = (int) (ce.getStats().getRegeneratingStat(HEALTH) / ce.getStats().getStat(Stat.MAX_HEALTH) * 6);

                switch (pc.getPlayerClass().getSkillCostType()) {
                    case HEALTH:
                        break;
                    case ENERGY:
                        this.energyMana = (int) (ce.getStats().getRegeneratingStat(RegeneratingStat.ENERGY) / ce.getStats().getStat(Stat.MAX_ENERGY) * 6);
                        break;
                    case MANA:
                        this.energyMana = (int) (ce.getStats().getRegeneratingStat(RegeneratingStat.MANA) / ce.getStats().getStat(Stat.MAX_MANA) * 6);
                        break;
                }
            }
        } else {
            online = current = false;
        }
    }

    public String getDisplayString() {
        return getHealthString() + "    " + getEnergyString();
    }

    /**
     * Builds the health bar for display.
     */
    public String getHealthString() {
        StringBuilder sb = new StringBuilder();
        sb.append("♥♥♥♥♥♥");
        if (this.current)
            sb.insert(health, ChatColor.GRAY);
        sb.insert(0, this.current ? ChatColor.RED : (online ? ChatColor.DARK_GRAY : ChatColor.BLACK));
        return sb.toString();
    }

    /**
     * Builds the enerby/mana bar for display.
     */
    public String getEnergyString() {
        StringBuilder sb = new StringBuilder();
        sb.append("☼☼☼☼☼☼");
        if (this.current)
            sb.insert(6 - energyMana, ChatColor.AQUA);
        sb.insert(0, this.current ? ChatColor.GRAY : (online ? ChatColor.DARK_GRAY : ChatColor.BLACK));
        return sb.toString();
    }
}
