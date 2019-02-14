package com.legendsofvaleros.util.item;

import com.codingforcookies.robert.item.ItemBuilder;
import com.google.common.collect.ImmutableList;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model {
	private interface RPC {
		Promise<List<Model>> findModels();
	}

	private static RPC rpc;

	public static final Model NONE = new Model(Material.BEDROCK, 0);
	public static ItemStack EMPTY_SLOT = null;

	private static final Map<String, Model> models = new HashMap<>();

	public static void onLoad() {
		rpc = APIController.create(RPC.class);
	}

	public static void onPostLoad() {
		try {
			loadAll().get();
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}

		EMPTY_SLOT = stack("empty-slot").create();
	}

	public static Promise<List<Model>> loadAll() {
		models.clear();

		return rpc.findModels().onSuccess(val -> {
			models.clear();

			val.orElse(ImmutableList.of()).stream().forEach(model -> models.put(model.id, model));

			Utilities.getInstance().getLogger().info("Loaded " + models.size() + " models.");
		});
	}

	public static Model get(String id) {
		if(!models.containsKey(id))
			return NONE;
		return models.get(id);
	}

	public static ItemBuilder stack(String id) {
		return get(id).toStack();
	}
	
	public static ItemStack merge(String id, ItemStack stack) {
		if(stack == null) stack = EMPTY_SLOT;
		
		ItemBuilder ib = stack(id);
		if(stack.getItemMeta() != null) {
			ItemMeta im = stack.getItemMeta();
			ib.setName(im.getDisplayName());
			if(im.getLore() != null)
				ib.addLore(im.getLore().toArray(new String[0]));
		}
		return ib.create();
	}

	private final String id;
	public String getId() { return id; }

	private final String group;
	public String getGroup() { return group; }

	private final String name;
	public String getName() { return name; }

	private final Material material;
	public Material getMaterial() { return material; }

	private final short metadata;
	public short getMetaData() { return metadata; }
	
	public Model(Material material, int metaData) {
		this(null, "Generated", material, metaData);
	}
	
	public Model(String id, String name, Material material, int metadata) {
		this.id = id;
		this.group = null;
		this.name = name;
		this.material = material;
		this.metadata = (short)metadata;
	}

	public ItemBuilder toStack() {
		return new ItemBuilder(material).setName(null).setDurability(metadata).unbreakable().addFlag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
	}

	@Override
	public String toString() {
		return "Model(id=" + id + ", group=" + group + ", name=" + name + ", material=" + material + ", metadata=" + metadata + ")";
	}
}