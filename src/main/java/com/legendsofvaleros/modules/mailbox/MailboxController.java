package com.legendsofvaleros.modules.mailbox;

import com.codingforcookies.doris.orm.ORMTable;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Crystall on 11/24/2018
 */
@DependsOn(Characters.class)
public class MailboxController extends ModuleListener {
    private static MailboxController instance;

    public static MailboxController getInstance() {
        return instance;
    }

    private static ORMTable<Mail> mailBoxMails;

    private static final Map<CharacterId, Mailbox> mailboxes = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;

        String dbPoolId = LegendsOfValeros.getInstance().getConfig().getString("dbpools-database");

        mailBoxMails = ORMTable.bind(dbPoolId, Mail.class);
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

    /**
     * Adds a mail to the mailbox of player character
     * @param characterId
     * @param mail
     * @return
     */
    public ListenableFuture<Void> addMail(CharacterId characterId, Mail mail) {
        SettableFuture<Void> ret = SettableFuture.create();

        Mailbox mailbox = mailboxes.get(characterId);

        mailBoxMails.save(mail, true)
                .addListener(() -> {
                    mailbox.getMails().add(mail);
                    ret.set(null);
                }, MailboxController.getInstance().getScheduler()::sync);

        return ret;
    }

    /**
     * Load the Mailbox object with all emails into the cache / RAM
     * @param characterId
     * @return
     */
    private ListenableFuture<Void> loadMailBox(CharacterId characterId) {
        SettableFuture<Void> ret = SettableFuture.create();

        Mailbox mailbox = new Mailbox(characterId);

        mailBoxMails.query()
                .get(characterId)
                .forEach((mail) -> addMail(characterId, mail))
                .onFinished(() -> {
                    mailboxes.put(characterId, mailbox);
                    ret.set(null);
                })
                .execute(true);

        return ret;
    }

    private static ListenableFuture<Void> onLogout(final CharacterId characterId) {
        SettableFuture<Void> ret = SettableFuture.create();

        Mailbox mailbox = mailboxes.remove(characterId);
        if (mailbox == null) {
            ret.set(null);
        } else {
            mailBoxMails.saveAll(mailbox.getMails(), true)
                    .addListener(() -> ret.set(null), MailboxController.getInstance().getScheduler()::async);

        }

        return ret;
    }

    @EventHandler
    public void onCharacterStartLoading(PlayerCharacterStartLoadingEvent event) {
        PhaseLock lock = event.getLock("Mailbox");

        loadMailBox(event.getPlayerCharacter().getUniqueCharacterId())
                .addListener(() -> {
                    lock.release();
                    getMailbox(event.getPlayerCharacter().getUniqueCharacterId()).notifyOwner();
                }, MailboxController.getInstance().getScheduler()::async);
    }

    @EventHandler
    public void onCharacterLogout(PlayerCharacterLogoutEvent event) {
        PhaseLock lock = event.getLock("Mailbox");
        onLogout(event.getPlayerCharacter().getUniqueCharacterId())
                .addListener(() -> {
                    mailboxes.remove(event.getPlayerCharacter().getUniqueCharacterId());
                    lock.release();
                }, MailboxController.getInstance().getScheduler()::async);
    }

    @EventHandler
    public void onCharacterRemoved(PlayerCharacterRemoveEvent event) {
        mailBoxMails.query().remove(event.getPlayerCharacter().getUniqueCharacterId().toString()).execute(true);

    }

}
