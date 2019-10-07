package com.legendsofvaleros.api;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * This allows the use of an optional interface to get a reference to an object, but still allow the object itself to be decoded.
 * i.e. The Gear type will be decoded normally, but decoding an IGear field will reference an existing Gear object.
 */
public class InterfaceTypeAdapter {
    public interface Encoder<X> extends Function<X, String> { }
    public interface Getter<X> extends Function<String, Promise<X>> { }

    public static <X> void register(Class<X> clazz, Encoder<X> encoder, Getter<X> getter) {
        final TypeAdapter refTypeAdapter = new TypeAdapter<Ref<X>>() {
            @Override
            public void write(JsonWriter out, Ref<X> value) throws IOException {
                // We should never be saving any full object registered through this type. Therefore, we only save the reference string.
                out.value(value != null ? encoder.apply(value.get()) : null);
            }

            @Override
            public Ref<X> read(JsonReader in) throws IOException {
                if(in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return Ref.empty();
                }

                // So, if we just decode the value here, we run into the common issue of circular references.
                // This lets us delay loading until decoding has completed.
                return Ref.of(getter.apply(in.nextString()).get());
            }
        };

        APIController.getInstance().getGsonBuilder().registerTypeAdapterFactory(new TypeAdapterFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                Class<T> rawType = (Class<T>)type.getRawType();

                if (rawType != Ref.class) {
                    if(clazz.isAssignableFrom(rawType)) {
                        TypeAdapterFactory factory = this;

                        return new TypeAdapter() {
                            @Override
                            public void write(JsonWriter out, Object value) throws IOException {
                                // We should never be saving any full object registered through this type. Therefore, we only save the reference string.
                                out.value(value != null ? encoder.apply((X)value) : null);
                            }

                            @Override
                            public Object read(JsonReader in) throws IOException {
                                if(in.peek() == JsonToken.NULL) {
                                    in.nextNull();
                                    return null;
                                }

                                if(in.peek() == JsonToken.STRING) {
                                    return getter.apply(in.nextString()).get();
                                }

                                return APIController.getInstance().getGson().getDelegateAdapter(factory, type).read(in);
                            }
                        };
                    }

                    return null;
                }

                final ParameterizedType parameterizedType = (ParameterizedType)type.getType();
                final Type actualType = parameterizedType.getActualTypeArguments()[0];

                if(actualType != clazz)
                    return null;

                return refTypeAdapter;
            }
        });
    }
}