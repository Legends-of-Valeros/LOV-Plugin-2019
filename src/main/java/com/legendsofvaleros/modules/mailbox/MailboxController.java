package com.legendsofvaleros.modules.mailbox;

import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.EventHandler;

/**
 * Created by Crystall on 11/24/2018
 */
@DependsOn(Characters.class)
// TODO: Create subclass for listeners?
@ModuleInfo(name = "Mailbox", info = "")
public class MailboxController extends MailboxAPI {
    private static MailboxController instance;

    public static MailboxController getInstance() {
        return instance;
    }


    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        instance = null;
    }


    /**
     * Returns the mailbox object of a player character
     * @param characterId
     * @return
     */
    public Mailbox getMailbox(CharacterId characterId) {
        return mailboxes.get(characterId);
    }

    @EventHandler
    public void onCharacterStartLoading(PlayerCharacterStartLoadingEvent event) {
        PhaseLock lock = event.getLock("Mailbox");

        loadMailbox(event.getPlayerCharacter().getUniqueCharacterId())
                .onFailure((err) -> MessageUtil.sendSevereException(MailboxController.getInstance(), event.getPlayer(), err))
                .onSuccess((s) -> {
                    lock.release();
                    getMailbox(event.getPlayerCharacter().getUniqueCharacterId()).notifyOwner();
                }, MailboxController.getInstance().getScheduler()::async);
    }

    @EventHandler
    public void onCharacterLogout(PlayerCharacterLogoutEvent event) {
        PhaseLock lock = event.getLock("Mailbox");

        saveMailbox(event.getPlayerCharacter().getUniqueCharacterId())
                .onFailure((err) -> MessageUtil.sendSevereException(MailboxController.getInstance(), event.getPlayer(), err))
                .onSuccess((s) -> {
                    mailboxes.remove(event.getPlayerCharacter().getUniqueCharacterId());
                    lock.release();
                }, MailboxController.getInstance().getScheduler()::async);
    }

    /**
     * Opens a book with the content of the mail
     * @param mail
     */
    public void openMail(Mail mail) {
        //TODO open a book with the content of the mail
    }

    @EventHandler
    public void onCharacterRemoved(PlayerCharacterRemoveEvent event) {
        deleteMailbox(event.getPlayerCharacter().getUniqueCharacterId());
    }

}
