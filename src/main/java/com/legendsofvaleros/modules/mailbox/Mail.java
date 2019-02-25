package com.legendsofvaleros.modules.mailbox;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.gear.core.Gear;

/**
 * Created by Crystall on 11/24/2018
 * Represents a single mail within the mailsystem / gui
 */
public class Mail {
    private CharacterId receiver;
    private String receiverName;

    private CharacterId from;
    private String fromName;

    private String title;
    private String content;

    // Has to be Gear.Data, otherwise we lose persistent values
    // when turning it back into an item.
    private Gear.Data item;

    private boolean isRead;

    public Mail(CharacterId receiver, CharacterId from, String title, String content, boolean isRead) {
        this.receiver = receiver;
        this.from = from;
        this.title = title;
        this.content = content;
        this.isRead = isRead;
    }

    public Mail(CharacterId receiver, CharacterId from, String content, Gear.Instance item, boolean isRead) {
        this(receiver, from, content, item.getData(), isRead);
    }

    public Mail(CharacterId receiver, CharacterId from, String content, Gear.Data item, boolean isRead) {
        this.receiver = receiver;
        this.from = from;
        this.content = content;
        this.item = item;
        this.isRead = isRead;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Gear.Data getItem() {
        return item;
    }

    public void setItem(Gear.Data item) {
        this.item = item;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public CharacterId getReceiver() {
        return receiver;
    }

    public void setReceiver(CharacterId receiver) {
        this.receiver = receiver;
    }

    public CharacterId getFrom() {
        return from;
    }

    public void setFrom(CharacterId from) {
        this.from = from;
    }
}