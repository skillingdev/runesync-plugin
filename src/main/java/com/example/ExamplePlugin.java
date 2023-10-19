package com.example;

import com.google.gson.Gson;
import com.google.inject.Provides;

import java.io.IOException;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.PlayerChanged;
import net.runelite.client.account.SessionManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.LootReceived;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@PluginDescriptor(name = "Example")
public class ExamplePlugin extends Plugin {
  @Inject
  SessionManager sessionManager;

  @Inject
  private Client client;

  @Inject
  private OkHttpClient httpClient;

  @Inject
  private Gson gson;

  private SetupUser currentUser;

  @Override
  protected void startUp() throws Exception {
    log.info("Example started!");
  }

  @Override
  protected void shutDown() throws Exception {
    log.info("Example stopped!");
  }

  @Subscribe
  private void onPlayerChanged(PlayerChanged playerChanged) {
    if (playerChanged.getPlayer() != client.getLocalPlayer()) {
      return;
    }

    String name = client.getLocalPlayer().getName();
    SetupUser user = new SetupUser(String.valueOf(client.getAccountHash()), name);

    if (user.equals(currentUser)) {
      log.debug("currentUser hasn't changed - skipping setup");
      return;
    }

    currentUser = user;

    RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"),
        gson.toJson(currentUser));

    log.info("calling SetupUser for: " + currentUser.toString());

    Request request = new Request.Builder()
        .url("https://runesync.vercel.app/api/SetupUser")
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
    log.info("HERE! HERE");
    log.info(lootReceived.toString());
  }

  @Provides
  ExampleConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(ExampleConfig.class);
  }
}
