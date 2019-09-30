package com.legendsofvaleros.util.model;

import com.codingforcookies.robert.item.ItemBuilder;
import com.google.common.collect.ImmutableList;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.InterfaceTypeAdapter;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Models {
    private interface RPC {
        Promise<List<Model>> findModels();
    }

    private static RPC rpc;

    private static final Map<String, Model> models = new HashMap<>();

    public static void onLoad() {
        rpc = APIController.create(RPC.class);

        InterfaceTypeAdapter.register(IModel.class,
                (model) -> model.getId(),
                (id) -> Promise.make(models.get(id)));
    }

    public static void onPostLoad() {
        loadAll().get();
    }

    public static Promise<List<Model>> loadAll() {
        models.clear();

        return rpc.findModels().onSuccess(val -> {
            models.clear();

            val.orElse(ImmutableList.of()).stream().forEach(model -> {
                models.put(model.getId(), model);
                models.put(model.getSlug(), model);
            });

            Model.EMPTY_SLOT = stack("empty-slot").create();

            Utilities.getInstance().getLogger().info("Loaded " + models.size() + " models.");
        });
    }

    public static Model get(String id) {
        if (!models.containsKey(id)) {
            return Model.NONE;
        }
        return models.get(id);
    }

    public static void put(String id, String name, Material material) {
        models.put(id, new Model(id, name, material, 0));
    }

    public static ItemBuilder stack(String id) {
        return get(id).toStack();
    }

    public static ItemStack merge(String id, ItemStack stack) {
        if (stack == null) {
            stack = Model.EMPTY_SLOT;
        }

        ItemBuilder ib = stack(id);
        if (stack.getItemMeta() != null) {
            ItemMeta im = stack.getItemMeta();
            ib.setName(im.getDisplayName());

            if (im.getLore() != null) {
                ib.addLore(im.getLore().toArray(new String[0]));
            }
        }
        return ib.create();
    }
}