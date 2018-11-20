package com.legendsofvaleros.util;

import com.google.common.base.Preconditions;
import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TextBuilder {
    private BaseComponent current;
    private final List<BaseComponent> parts = new ArrayList();

    public TextBuilder(TextBuilder original) {
        this.current = original.current.duplicate();
        Iterator var2 = original.parts.iterator();

        while(var2.hasNext()) {
            BaseComponent baseComponent = (BaseComponent)var2.next();
            this.parts.add(baseComponent.duplicate());
        }

    }

    public TextBuilder(String text) {
        this.current = new TextComponent(text);
    }

    public TextBuilder(BaseComponent component) {
        this.current = component.duplicate();
    }

    public TextBuilder prepend(TextBuilder builder) {
        for(int i = 0; i < builder.parts.size(); i++)
            this.parts.add(i, builder.parts.get(i));
        return this;
    }

    public TextBuilder prepend(BaseComponent component) {
        this.parts.add(0, component);
        return this;
    }

    public TextBuilder prepend(BaseComponent... components) {
        for(int i = 0; i < components.length; i++)
            this.parts.add(i, components[i]);
        return this;
    }

    public TextBuilder append(BaseComponent component) {
        return this.append(component, ComponentBuilder.FormatRetention.NONE);
    }

    public TextBuilder append(BaseComponent component, ComponentBuilder.FormatRetention retention) {
        this.parts.add(this.current);
        BaseComponent previous = this.current;
        this.current = component.duplicate();
        this.current.copyFormatting(previous, retention, false);
        return this;
    }

    public TextBuilder append(BaseComponent[] components) {
        return this.append(components, ComponentBuilder.FormatRetention.NONE);
    }

    public TextBuilder append(BaseComponent[] components, ComponentBuilder.FormatRetention retention) {
        Preconditions.checkArgument(components.length != 0, "No components to append");
        BaseComponent previous = this.current;
        BaseComponent[] var4 = components;
        int var5 = components.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            BaseComponent component = var4[var6];
            this.parts.add(this.current);
            this.current = component.duplicate();
            this.current.copyFormatting(previous, retention, false);
        }

        return this;
    }

    public TextBuilder append(String text) {
        return this.append(text, ComponentBuilder.FormatRetention.NONE);
    }

    public TextBuilder append(String text, ComponentBuilder.FormatRetention retention) {
        this.parts.add(this.current);
        BaseComponent old = this.current;
        this.current = new TextComponent(text);
        this.current.copyFormatting(old, retention, false);
        return this;
    }

    public TextBuilder append(Joiner joiner) {
        return joiner.join(this, ComponentBuilder.FormatRetention.NONE);
    }

    public TextBuilder append(TextBuilder.Joiner joiner, ComponentBuilder.FormatRetention retention) {
        return joiner.join(this, retention);
    }

    public TextBuilder color(ChatColor color) {
        this.current.setColor(color.asBungee());
        return this;
    }

    public TextBuilder bold(boolean bold) {
        this.current.setBold(bold);
        return this;
    }

    public TextBuilder italic(boolean italic) {
        this.current.setItalic(italic);
        return this;
    }

    public TextBuilder underlined(boolean underlined) {
        this.current.setUnderlined(underlined);
        return this;
    }

    public TextBuilder strikethrough(boolean strikethrough) {
        this.current.setStrikethrough(strikethrough);
        return this;
    }

    public TextBuilder obfuscated(boolean obfuscated) {
        this.current.setObfuscated(obfuscated);
        return this;
    }

    public TextBuilder insertion(String insertion) {
        this.current.setInsertion(insertion);
        return this;
    }

    public TextBuilder event(ClickEvent clickEvent) {
        this.current.setClickEvent(clickEvent);
        return this;
    }

    public TextBuilder event(HoverEvent hoverEvent) {
        this.current.setHoverEvent(hoverEvent);
        return this;
    }

    public TextBuilder hover(String... cmd) {
        event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextBuilder(String.join("\n", cmd)).create()));
        return this;
    }

    public TextBuilder command(String cmd) {
        event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
        return this;
    }

    public TextBuilder reset() {
        return this.retain(ComponentBuilder.FormatRetention.NONE);
    }

    public TextBuilder retain(ComponentBuilder.FormatRetention retention) {
        this.current.retain(retention);
        return this;
    }

    public BaseComponent[] create() {
        BaseComponent[] result = (BaseComponent[])this.parts.toArray(new BaseComponent[this.parts.size() + 1]);
        result[this.parts.size()] = this.current;
        return result;
    }

    public interface Joiner {
        TextBuilder join(TextBuilder var1, ComponentBuilder.FormatRetention var2);
    }
}
