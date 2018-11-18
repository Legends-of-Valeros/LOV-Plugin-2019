package com.legendsofvaleros.modules.chat;

import java.util.ArrayList;
import java.util.List;

public class PlayerChat {
    public Character channel = Chat.getInstance().getChannelDefault();

    public String title;
    public String prefix;
    public String suffix;

    public List<Character> offChannels = new ArrayList<>();
}