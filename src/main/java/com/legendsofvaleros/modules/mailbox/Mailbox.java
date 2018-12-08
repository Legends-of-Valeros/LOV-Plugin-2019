package com.legendsofvaleros.modules.mailbox;

import com.codingforcookies.doris.orm.ORM;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.util.MessageUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by Crystall on 11/24/2018
 * Represents a Mailbox object that contains all mails of a loaded character
 */
public class Mailbox extends ORM {

    @Getter @Setter
    public CharacterId characterId;
    @Getter @Setter
    public ArrayList<Mail> mails;

    public Mailbox(CharacterId characterId) {
        this(characterId, new ArrayList<>());
    }

    public Mailbox(CharacterId characterId, ArrayList<Mail> mails) {
        this.characterId = characterId;
        this.mails = mails;
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
        Player p = Characters.getPlayerCharacter(characterId).getPlayer();
        if (Characters.isPlayerCharacterLoaded(p)) {
            // TODO make pretty like a princess
            MessageUtil.sendInfo(p, "You have" + getUnreadMails() + " unread mails!");
        }
    }
}
