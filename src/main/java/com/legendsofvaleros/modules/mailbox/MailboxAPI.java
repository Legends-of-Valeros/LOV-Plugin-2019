package com.legendsofvaleros.modules.mailbox;

import com.google.common.collect.ImmutableList;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.characters.api.CharacterId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Crystall on 02/15/2019
 */
public class MailboxAPI extends ListenerModule {

    public interface RPC {
        Promise<List<Mail>> getMails(CharacterId characterId);

        Promise<Object> saveMail(Mail mail);

        Promise<Object> saveMailbox(ArrayList<Mail> mails);

        Promise<Boolean> deleteMailbox(CharacterId characterId);
    }

    private MailboxAPI.RPC rpc;
    protected Map<CharacterId, Mailbox> mailboxes = new HashMap<>();


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
    Promise saveMail(CharacterId characterId, Mail mail) {
        if (!mailboxes.containsKey(characterId)) {
            return Promise.make(false);
        }

        return rpc.saveMail(mail)
                .onSuccess(val -> {
                    mailboxes.get(characterId).mails.add(mail);
                });
    }

    /**
     * Load the Mailbox object with all emails into the cache / RAM
     * @param characterId
     * @return
     */
    Promise<List<Mail>> loadMailbox(CharacterId characterId) {
        Promise<List<Mail>> promise = rpc.getMails(characterId);

        Mailbox mailbox = new Mailbox(characterId);
        promise.onSuccess(val -> {
            mailbox.mails.addAll(val.orElse(ImmutableList.of()));
        });
        mailboxes.put(characterId, mailbox);

        return promise;
    }

    /**
     * Saves all emails of the mailbox
     * @param characterId
     * @return
     */
    Promise<Object> saveMailbox(CharacterId characterId) {
        if (!mailboxes.containsKey(characterId)) {
            return Promise.make(false);
        }

        return rpc.saveMailbox(mailboxes.get(characterId).getMails());
    }

    /**
     * Deletes all mails according to a character
     * @param characterId
     * @return
     */
    Promise<Boolean> deleteMailbox(CharacterId characterId) {
        if (mailboxes.remove(characterId) == null) {
            return null;
        }

        return rpc.deleteMailbox(characterId);
    }

}
