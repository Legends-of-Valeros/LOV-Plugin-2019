package com.legendsofvaleros.api;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * This allows the use of an interface to get a reference to an object, but still allow the object itself to be decoded.
 * i.e. The Gear type will be decoded normally, but decoding an IGear field will reference an existing Gear object.
 */
public class InterfaceTypeAdapter<T> extends TypeAdapter<T> {
    public static <T> void register(Class<T> clazz, Encoder<T> encoder, Decoder<T> decoder) {
        APIController.getInstance().getGsonBuilder().registerTypeAdapterFactory(of(clazz, encoder, decoder));
    }

    public static <T> TypeAdapterFactory of(Class<T> clazz, Encoder<T> encoder, Decoder<T> decoder) {
        return new TypeAdapterFactory() {
            @Override
            public TypeAdapter create(Gson gson, TypeToken typeToken) {
                final Class<?> requestedType = typeToken.getRawType();
                if (!clazz.isAssignableFrom(requestedType)) {
                    return null;
                }

                return new InterfaceTypeAdapter(this, requestedType, encoder, decoder);
            }
        };
    }

    public interface Encoder<T> {
        String toString(T val);
    }

    public interface Decoder<T> {
        T toValue(String id);
    }

    private final Class<T> clazz;
    private final Encoder<T> encoder;
    private final Decoder<T> decoder;

    private final TypeAdapterFactory skipPast;

    private InterfaceTypeAdapter(TypeAdapterFactory skipPast, Class<T> clazz, Encoder<T> encoder, Decoder<T> decoder) {
        this.clazz = clazz;
        this.encoder = encoder;
        this.decoder = decoder;

        this.skipPast = skipPast;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        // We should never be saving any full object registered through this type. Therefore, we only save the reference string.
        out.value(value != null ? encoder.toString(value) : null);
    }

    @Override
    public T read(JsonReader in) throws IOException {
        // If we reference the interface, then the type should be a string, and we return the stored object.
        // Note: it must be loaded already, else this returns null.
        if(in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        // If it's not a string, then it's not a reference type. Grab an adapter that's not this one (usually the
        // default reflective adapter) and decode like normal.
        if(in.peek() != JsonToken.STRING) {
            return APIController.getInstance().getGson().getDelegateAdapter(skipPast, TypeToken.get(clazz)).read(in);
        }

        String id = in.nextString();
        T val = decoder.toValue(id);

        if(val == null) {
            APIController.getInstance().getLogger().warning("'" + id + "' does not map to any " + clazz.getSimpleName() + "!");
        }

        return val;
    }
}
