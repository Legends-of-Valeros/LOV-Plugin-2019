package com.legendsofvaleros.modules.dueling;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.IntegratesWith;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.dueling.core.Duel;
import com.legendsofvaleros.modules.dueling.listener.DuelListener;
import com.legendsofvaleros.modules.dueling.listener.PlayerMenuListener;
import com.legendsofvaleros.features.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.pvp.PvPController;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@DependsOn(PlayerMenu.class)
@IntegratesWith(module = PvPController.class)
@ModuleInfo(name = "Dueling", info = "")
public class DuelingController extends Module {
    private static DuelingController instance;

    public static DuelingController getInstance() {
        return instance;
    }

    public Table<Player, Player, Duel> duels = HashBasedTable.create();

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        registerEvents(new DuelListener());
        registerEvents(new PlayerMenuListener());
    }

    @Override
    public void onUnload() {
        for (Cell<Player, Player, Duel> c : duels.cellSet()) {
            c.getValue().cancel();
        }

        super.onUnload();
    }

    /**
     * Creates a duel with the two given players
     * @param p1
     * @param p2
     */
    public void createDuel(Player p1, Player p2) {
        duels.put(p1, p2, new Duel(p1, p2));

        Title title = new Title("", "Ready.... Fight!", 10, 40, 10);
        title.setTimingsToTicks();
        title.setSubtitleColor(ChatColor.GOLD);

        TitleUtil.queueTitle(title, p1);
        TitleUtil.queueTitle(title, p2);
    }

    /**
     * Returns a duel both given players are in
     * @param p1
     * @param p2
     * @return
     */
    public Duel getDuel(Player p1, Player p2) {
        Duel duel = null;

        if (duels.contains(p1, p2)) {
            duel = duels.get(p1, p2);
        } else if (duels.contains(p2, p1)) {
            duel = duels.get(p2, p1);
        }

        return duel;
    }

    /**
     * Returns the duel a player is in
     * @param p
     * @return
     */
    public Duel getDuel(Player p) {
        Duel duel = null;

        if (! duels.row(p).isEmpty()) {
            duel = duels.row(p).values().iterator().next();
        } else if (duels.column(p).size() != 0) {
            duel = duels.column(p).values().iterator().next();
        }

        return duel;
    }
}