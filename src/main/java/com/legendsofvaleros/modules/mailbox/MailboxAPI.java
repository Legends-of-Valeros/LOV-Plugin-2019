package com.legendsofvaleros.modules.mailbox;

import com.google.common.collect.ImmutableList;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.modules.characters.api.CharacterId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Crystall on 02/15/2019
 */
public class MailboxAPI extends ModuleListener {

    public interface RPC {
        Promise<List<Mail>> getMails(CharacterId characterId);

        Promise<Boolean> saveMail(CharacterId characterId, Mail mail);

        Promise<Boolean> saveMailbox(CharacterId characterId, ArrayList<Mail> mails);

        Promise<Boolean> deleteMailbox(CharacterId characterId);
    }

    private MailboxAPI.RPC rpc;
    public Map<CharacterId, Mailbox> mailboxes = new HashMap<>();


    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(MailboxAPI.RPC.class);

    }

    @Override
    public void onUnload() {
        super.onUnload();
    }

    /**
     * Adds a mail to the mailbox of player character
     * @param characterId
     * @param mail
     * @return
     */
    public Promise<Boolean> saveMail(CharacterId characterId, Mail mail) {

        Mailbox mailbox = mailboxes.get(characterId);

        if (mailbox != null) {
            Promise<Boolean> promise = rpc.saveMail(characterId, mail);
            promise.onSuccess(val -> {
                mailbox.mails.add(mail);
            }).onFailure(Throwable::printStackTrace);
            return promise;

        }
        return null;
    }

    /**
     * Load the Mailbox object with all emails into the cache / RAM
     * @param characterId
     * @return
     */
    public Promise<List<Mail>> loadMailbox(CharacterId characterId) {
        Promise<List<Mail>> promise = rpc.getMails(characterId);

        Mailbox mailbox = new Mailbox(characterId);
        promise.onSuccess(val -> {
            mailbox.mails.addAll(val.orElse(ImmutableList.of()));
        }).onFailure(Throwable::printStackTrace);
        mailboxes.put(characterId, mailbox);

        return promise;
    }

    /**
     * Saves all emails of the mailbox
     * @param characterId
     * @return
     */
    public Promise<Boolean> saveMailbox(CharacterId characterId) {
        Mailbox mailbox = mailboxes.get(characterId);

        if (mailbox == null) {
            return null;
        }

        return rpc.saveMailbox(characterId, mailbox.getMails());
    }

    /**
     * Deletes all mails according to a character
     * @param characterId
     * @return
     */
    public Promise<Boolean> deleteMailbox(CharacterId characterId) {
        Mailbox mailbox = mailboxes.get(characterId);

        if (mailbox == null) {
            return null;
        }

        return rpc.deleteMailbox(characterId);
    }

}
