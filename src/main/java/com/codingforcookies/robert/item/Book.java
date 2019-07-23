package com.codingforcookies.robert.item;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * WARNING: THIS CLASS IS PER-VERSION. CURRENTLY, IT MUST BE RECOMPILED WITH EACH UPDATE.
 */
public class Book {
    public static final int WIDTH = 112;
    public static final int LINES = 14;

    private final String title;
    private final String author;
    private final List<String> pages = new ArrayList<>();

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public Book addPage(BaseComponent[] message) {
        pages.add(ComponentSerializer.toString(message));
        return this;
    }

    public ItemStack build() {
        @SuppressWarnings("deprecation")
        ItemStack book = new ItemStack(Item.getById(Material.LEGACY_WRITTEN_BOOK.getId()));
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("author", author);
        tag.setString("title", title);
        NBTTagList pages = new NBTTagList();
        for (String page : this.pages) {
            pages.add(new NBTTagString(page));
        }
        tag.set("pages", pages);
        book.setTag(tag);
        return book;
    }

    public void open(Player p, boolean addStats) {
        open(p, build(), addStats);
    }

    private static void open(Player p, ItemStack book, boolean addStats) {
        org.bukkit.inventory.ItemStack hand = p.getInventory().getItemInMainHand();
        try {
            p.getInventory().setItemInMainHand(CraftItemStack.asBukkitCopy(book));
            ((CraftPlayer) p).getHandle().openBook(book, EnumHand.MAIN_HAND);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            p.getInventory().setItemInMainHand(hand);
        }
//		if(addStats){
//			player.b(StatisticList.USE_ITEM_COUNT[387]);
//		}
    }
}