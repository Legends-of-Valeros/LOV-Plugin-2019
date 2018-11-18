package com.legendsofvaleros.util;

import com.codingforcookies.doris.sql.TableManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.Bukkit;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerData {
	private static final String TABLE_NAME = "players";

	private static final String PLAYER_UUID = "player_id";
	private static final String PLAYER_NAME = "player_name";
	private static final String PLAYER_RESOURCE_PACK = "player_resource_pack";
	private static final String PLAYER_DISCORD_ID = "player_discord_id";

	private static TableManager manager;

	public static final Cache<UUID, PlayerData> cache = CacheBuilder.newBuilder()
															.concurrencyLevel(4)
															.expireAfterAccess(10, TimeUnit.MINUTES)
															.build();

	static void onEnable() {
		manager = new TableManager(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), TABLE_NAME);

		manager.primary(PLAYER_UUID, "VARCHAR(36)")
				.column(PLAYER_NAME, "VARCHAR(32)")
				.column(PLAYER_RESOURCE_PACK, "VARCHAR(16)")
				.column(PLAYER_DISCORD_ID, "VARCHAR(32)").create();
	}

	public static ListenableFuture<PlayerData> getByDiscordID(String id) {
		SettableFuture<PlayerData> ret = SettableFuture.create();
		
		for(Entry<UUID, PlayerData> entry : cache.asMap().entrySet()) {
			if(entry.getValue().discordId != null && entry.getValue().discordId.equals(id)) {
				ret.set(entry.getValue());
				break;
			}
		}
		
		if(!ret.isDone()) {
			manager.query()
						.select()
							.where(PLAYER_DISCORD_ID, id)
						.build()
					.callback((result) -> {
						if(!result.next()) {
							ret.set(null);
							return;
						}
						
						PlayerData data = new PlayerData(UUID.fromString(result.getString(PLAYER_UUID)));
						data.username = result.getString(PLAYER_NAME);
						data.resourcePack = result.getString(PLAYER_RESOURCE_PACK);
						data.discordId = result.getString(PLAYER_DISCORD_ID);
						
						cache.put(data.uuid, data);
						ret.set(data);
					})
				.execute(true);
		}
		
		return ret;
	}

	public static ListenableFuture<PlayerData> get(UUID uuid) {
		SettableFuture<PlayerData> ret = SettableFuture.create();
		
		PlayerData cached = cache.getIfPresent(uuid);
		if(cached != null)
			ret.set(cached);
		else
			manager.query()
						.select()
							.where(PLAYER_UUID, uuid.toString())
						.build()
					.callback((result) -> {
						PlayerData data = new PlayerData(uuid);
						if(result.next()) {
							data.username = result.getString(PLAYER_NAME);
							data.resourcePack = result.getString(PLAYER_RESOURCE_PACK);
							data.discordId = result.getString(PLAYER_DISCORD_ID);
						}
						
						String currentName = Bukkit.getOfflinePlayer(uuid).getName();
						if(currentName != null && !currentName.equals(data.username)) {
							data.username = currentName;
							
							manager.query()
										.insert()
											.values(PLAYER_UUID, uuid.toString(),
													PLAYER_NAME, currentName,
													PLAYER_RESOURCE_PACK, data.resourcePack,
													PLAYER_DISCORD_ID, data.discordId)
											.onDuplicateUpdate(PLAYER_NAME)
										.build()
									.execute(true);
						}
						
						cache.put(data.uuid, data);
						ret.set(data);
					})
				.execute(true);
		
		return ret;
	}
	public static void push(PlayerData data) {
		manager.query()
					.insert()
						.values(PLAYER_UUID, data.uuid.toString(),
								PLAYER_NAME, data.username,
								PLAYER_RESOURCE_PACK, data.resourcePack,
								PLAYER_DISCORD_ID, data.discordId)
						.onDuplicateUpdate(PLAYER_NAME, PLAYER_RESOURCE_PACK, PLAYER_DISCORD_ID)
					.build()
			.execute(true);
	}
	
	public final UUID uuid;
	public String username;
	public String resourcePack;
	public String discordId;

	PlayerData(UUID uuid) {
		this.uuid = uuid;
	}
}