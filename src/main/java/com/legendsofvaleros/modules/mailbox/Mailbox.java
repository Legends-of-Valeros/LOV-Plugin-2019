package com.legendsofvaleros.modules.mailbox;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by Crystall on 11/24/2018
 * Represents a Mailbox object that contains all mails of a loaded character
 */
public class Mailbox {

    public CharacterId characterId;
    public ArrayList<Mail> mails;

    public Mailbox(CharacterId characterId) {
        this(characterId, new ArrayList<>());
    }

    public Mailbox(CharacterId characterId, ArrayList<Mail> mails) {
        this.characterId = characterId;
        this.mails = mails;
    }

    public boolean addMail(Mail mail) {
        if (!this.mails.contains(mail)) {
            this.mails.add(mail);
            return true;
        }
        return false;
    }

    /**
     * Returns the amount of unread mails in the mailbox
     * @return
     */
    public int getUnreadMails() {
        int count = 0;
        for (Mail mail : mails) {
            if (!mail.isRead()) count++;
        }
        return count;
    }

    /**
     * Notifies the owner of the mailbox
     */
    public void notifyOwner() {
        if (!Characters.isPlayerCharacterLoaded(characterId)) {
            return;
        }

        Player p = Characters.getPlayerCharacter(characterId).getPlayer();
        MessageUtil.sendInfo(p, "You have " + ChatColor.BOLD + getUnreadMails() + ChatColor.RESET + " unread mails!");
    }

    public ArrayList<Mail> getMails() {
        return mails;
    }

    public void setMails(ArrayList<Mail> mails) {
        this.mails = mails;
    }

    public CharacterId getCharacterId() {
        return characterId;
    }

    public void setCharacterId(CharacterId characterId) {
        this.characterId = characterId;
    }
}
