package com.legendsofvaleros.modules.mailbox;

import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.gear.item.GearItem;

/**
 * Created by Crystall on 11/24/2018
 * Represents a single mail within the mailsystem / gui
 */
@Table(name = "mail_box_mails")
public class Mail {
    @Column(primary = true, index = true, name = "character_id")
    private CharacterId characterId;

    @Column(name = "mail_content", length = 255)
    private String content;

    @Column(name = "mail_item", length = 255)
    // Has to be GearItem.Data, otherwise we lose persistent values
    // when turning it back into an item.
    private GearItem.Data item;

    @Column(name = "mail_is_read", length = 32)
    private boolean isRead;

    public Mail(CharacterId characterId, String content, boolean isRead) {
        this.characterId = characterId;
        this.content = content;
        this.isRead = isRead;
    }

    public Mail(CharacterId characterId, String content, GearItem.Instance item, boolean isRead) {
        this(characterId, content, item.getData(), isRead);
    }

    public Mail(CharacterId characterId, String content, GearItem.Data item, boolean isRead) {
        this.characterId = characterId;
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

    public GearItem.Data getItem() {
        return item;
    }

    public void setItem(GearItem.Data item) {
        this.item = item;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public CharacterId getCharacterId() {
        return characterId;
    }

    public void setCharacterId(CharacterId characterId) {
        this.characterId = characterId;
    }
}