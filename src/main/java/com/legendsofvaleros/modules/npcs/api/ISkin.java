package com.legendsofvaleros.modules.npcs.api;

public interface ISkin {
    String getId();

    String getSlug();

    Texture getTexture();

    class Texture {
        String uuid;
        public String getUUID() { return this.uuid; }

        String username;
        public String getUsername() { return this.username; }

        String signature;
        public String getSignature() { return this.signature; }

        String data;
        public String getData() { return this.data; }
    }
}
