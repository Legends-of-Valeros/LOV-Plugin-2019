package com.legendsofvaleros.modules.chat.listener;

import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.chat.ChatChannel;
import com.legendsofvaleros.modules.chat.ChatController;
import com.legendsofvaleros.modules.chat.PlayerChat;
import com.legendsofvaleros.modules.playermenu.settings.PlayerSettings;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TextBuilder;
import net.citizensnpcs.npc.ai.speech.Chat;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.concurrent.ExecutionException;

public class CommandListener implements Listener {
    private ChatController chat = ChatController.getInstance();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerCommand(ServerCommandEvent e) {
        if (e.getCommand().startsWith("say")) {
            e.setCancelled(true);

            TextBuilder tb = new TextBuilder("");
            tb.append("Mysterious Voice").color(ChatColor.DARK_GRAY).bold(true);
            tb.append(": ").color(ChatColor.DARK_GRAY);
            tb.append(e.getCommand().split(" ", 2)[1]).color(ChatColor.GRAY);
            BaseComponent[] bc = tb.create();

            Bukkit.getConsoleSender().spigot().sendMessage(bc);

            for (Player p : Bukkit.getOnlinePlayers())
                p.spigot().sendMessage(bc);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) throws InterruptedException, ExecutionException {
        String[] query = e.getMessage().substring(1).split(" ", 2);
        if (query[0].length() != 1) return;

        Character channelId = query[0].toUpperCase().charAt(0);
        if (chat.getChannel(channelId) != null) {
            e.setCancelled(true);

            PlayerChat data = chat.getPlayer(e.getPlayer().getUniqueId());

            if (query.length > 1) {
                if (query.length == 2) {
                    if (query[1].equals("off") || query[1].equals("on")) {
                        if (query[1].equals("off")) {
                            if (!chat.isChannelOn(e.getPlayer(), channelId))
                                MessageUtil.sendUpdate(e.getPlayer(), "That channel is already off.");
                            else {
                                data.offChannels.add(channelId);
                                MessageUtil.sendUpdate(e.getPlayer(), "You will no longer receive messages from that channel.");
                            }
                        } else {
                            if (chat.isChannelOn(e.getPlayer(), channelId))
                                MessageUtil.sendUpdate(e.getPlayer(), "That channel is already on.");
                            else {
                                data.offChannels.remove(channelId);
                                MessageUtil.sendUpdate(e.getPlayer(), "You will now receive messages from that channel.");
                            }
                        }

                        ListenableFuture<PlayerSettings> future = PlayerSettings.get(e.getPlayer());
                        future.addListener(() -> {
                            try {
                                PlayerSettings settings = future.get();
                                if (data.offChannels.size() == 0)
                                    settings.remove("chat.off");
                                else
                                    settings.put("chat.off", String.join("", data.offChannels.toArray(new String[data.offChannels.size()])));
                            } catch (InterruptedException | ExecutionException e1) {
                                e1.printStackTrace();
                            }
                        }, ChatController.getInstance().getScheduler()::sync);

                        return;
                    }
                }

                if (chat.isChannelOn(e.getPlayer(), channelId)) {
                    Character oldChannel = (data.channel == null ? chat.getDefaultChannel().getPrefix() : data.channel);
                    data.channel = channelId;

                    e.getPlayer().chat(e.getMessage().substring(3));

                    data.channel = oldChannel;
                } else
                    MessageUtil.sendError(e.getPlayer(), "You may not speak in a channel you have disabled. Type '/" + channelId + " on' to re-enable it.");
            } else {
                ChatChannel ch = chat.getChannel(channelId);

                if (!ch.isCanSetDefault()) {
                    MessageUtil.sendError(e.getPlayer(), "You are not allowed to set that channel as default.");
                } else {
                    data.channel = channelId;
                    PlayerSettings.get(e.getPlayer()).get().put("chat.selected", String.valueOf(channelId));
                    MessageUtil.sendUpdate(e.getPlayer(), "You are now speaking in " + ch.getName() + " chat.");
                }
            }
        }
    }
}
