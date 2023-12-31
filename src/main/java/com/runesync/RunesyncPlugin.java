package com.runesync;

import com.google.gson.Gson;
import com.google.inject.Provides;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.PlayerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.http.api.worlds.WorldType;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@PluginDescriptor(name = "RuneSync")
public class RunesyncPlugin extends Plugin {
  @Inject
  private RunesyncConfig config;

  @Inject
  private Client client;

  @Inject
  private OkHttpClient httpClient;

  @Inject
  private Gson gson;

  @Inject
  private ItemManager itemManager;

  private SetupUser currentUser;

  @Subscribe
  private void onPlayerChanged(PlayerChanged playerChanged) {
    if (playerChanged.getPlayer() != client.getLocalPlayer() || client.getAccountHash() == -1) {
      return;
    }

    String name = client.getLocalPlayer().getName();
    SetupUser user = new SetupUser(String.valueOf(client.getAccountHash()), name);

    if (user.equals(currentUser)) {
      log.debug("currentUser hasn't changed - skipping setup");
      return;
    }

    currentUser = user;

    if (!client.getWorldType().contains(WorldType.SEASONAL)) {
      log.info("RuneSync is for Leagues 4 only - log in to an appropriate world to get started!");
      return;
    }

    RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"),
        gson.toJson(currentUser));

    log.debug("calling setup-user for: " + currentUser.toString());

    Request request = new Request.Builder()
        .url("https://www.runesync.com/api/setup-user")
        .post(body)
        .build();

    httpClient.newCall(request).enqueue(new Callback() {

      @Override
      public void onFailure(Call call, IOException e) {
        log.debug("Error submitting request", e);
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        log.debug("SetupUser completed.");
        response.close();
      }
    });
  }

  @Subscribe
  public void onLootReceived(final LootReceived lootReceived) {
    if (!config.trackLoot()) {
      return;
    }

    if (client.getLocalPlayer() == null || client.getAccountHash() == -1) {
      return;
    }

    if (!client.getWorldType().contains(WorldType.SEASONAL)) {
      log.info("RuneSync is for Leagues 4 only - log in to an appropriate world to get started!");
      return;
    }

    log.debug("Received loot. Processing...");

    Instant timestamp = Instant.now();
    List<LootEntry> entries = new ArrayList<>();

    for (ItemStack stack : lootReceived.getItems()) {
      if (!CollectionLog.collectionLogItems.contains(stack.getId())) {
        continue;
      }

      ItemComposition itemComposition = itemManager.getItemComposition(stack.getId());
      WorldPoint point = WorldPoint.fromLocal(client, stack.getLocation());

      // We record separate loot entries for each unique item instead of tracking
      // item quantity.
      for (int i = 0; i < stack.getQuantity(); i++) {
        entries.add(new LootEntry(timestamp.toString(), String.valueOf(client.getAccountHash()),
            itemComposition.getName(), stack.getId(), lootReceived.getName(), lootReceived.getCombatLevel(),
            new EventLocation(point.getX(), point.getY(), point.getPlane(), point.getRegionID())));
      }
    }

    if (entries.size() == 0) {
      log.debug("Loot didn't contain any collection log items. Skipping API call.");
      return;
    }

    log.debug("Got unique loot - calling API.");

    RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"),
        gson.toJson(new RecordLoot(entries)));

    Request request = new Request.Builder()
        .url("https://www.runesync.com/api/record-loot")
        .post(body)
        .build();

    httpClient.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        log.debug("Error submitting request", e);
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful()) {
          log.debug("Error submitting loot record.");
        }

        response.close();
      }
    });
  }

  @Provides
  RunesyncConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(RunesyncConfig.class);
  }
}