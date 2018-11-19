package com.legendsofvaleros.util.item;

import com.codingforcookies.doris.orm.ORMTable;
import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import com.codingforcookies.robert.item.ItemBuilder;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

@Table(name = "item_models")
public class Model {
	public static final Model NONE = new Model(Material.BEDROCK, 0);
	
	public static ItemStack EMPTY_SLOT = null;

	private static ORMTable<Model> modelTable;
	public static void onEnable() {
		modelTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), Model.class);

		EMPTY_SLOT = stack("empty-slot").create();
	}

	public static final Cache<String, Model> cache = CacheBuilder.newBuilder()
													.concurrencyLevel(4)
													.expireAfterAccess(1, TimeUnit.HOURS)
													.build();
	public static ListenableFuture<Model> get(String id) {
		SettableFuture<Model> ret = SettableFuture.create();
		
		Model cached = cache.getIfPresent(id);
		if(cached != null) {
			 ret.set(cached);
		}else{
			modelTable.query()
						.get(id)
					.forEach((model) -> {
						cache.put(model.id, model);
						ret.set(model);
					}).onEmpty(() -> ret.set(NONE))
					.execute(true);
		}
		return ret;
	}

	public static ItemBuilder stack(String id) {
		try {
			return get(id).get(5, TimeUnit.SECONDS).toStack();
		} catch (Exception e) {
			MessageUtil.sendException(Utilities.getInstance(), null, new Exception("Failed to load item model! Offender: " + id), false);
		}
		return NONE.toStack();
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

	@Column(primary = true, name = "model_id", length = 64)
	private final String id;
	public String getId() { return id; }

	@Column(name = "model_group", length = 64)
	private final String group;
	public String getGroup() { return group; }

	@Column(name = "model_name", length = 64)
	private final String name;
	public String getName() { return name; }

	@Column(name = "model_material")
	private final Material material;
	public Material getMaterial() { return material; }

	@Column(name = "model_metadata")
	private final short metaData;
	public short getMetaData() { return metaData; }
	
	public Model(Material material, int metaData) {
		this(null, "Generated", material, metaData);
	}
	
	public Model(String id, String name, Material material, int metaData) {
		this.id = id;
		this.group = null;
		this.name = name;
		this.material = material;
		this.metaData = (short)metaData;
	}

	public ItemBuilder toStack() {
		return new ItemBuilder(material).setName(null).setDurability(metaData).unbreakable().addFlag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
	}

	@Override
	public String toString() {
		return "Model(id=" + id + ", group=" + group + ", name=" + name + ", material=" + material + ", meta_data=" + metaData + ")";
	}
}