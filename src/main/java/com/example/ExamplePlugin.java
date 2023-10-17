package com.example;

import com.google.inject.Provides;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.account.SessionManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneScapeProfile;
import net.runelite.client.config.RuneScapeProfileType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
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
  private ExampleConfig config;

  @Inject
  private ConfigManager configManager;

  private OkHttpClient httpClient;

  @Override
  protected void startUp() throws Exception {
    log.info("Example started!");
    httpClient = new OkHttpClient();
  }

  @Override
  protected void shutDown() throws Exception {
    log.info("Example stopped!");
  }

  @Subscribe
  public void onGameStateChanged(GameStateChanged gameStateChanged) {
    if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
      client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
    }
  }

  @Subscribe
  public void onNpcLootReceived(final NpcLootReceived npcLootReceived) {
    RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), "{\"page\": 42}");
    Request request = new Request.Builder()
        .url("www.google.com")
        .post(body)
        .build();

    httpClient.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        log.debug("Error submitting webhook", e);
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        log.info(response.body().string());
        response.close();
      }
    });

    List<RuneScapeProfile> rsProfiles = configManager.getRSProfiles();
    for (RuneScapeProfile profile : rsProfiles) {
      log.info(profile.toString());
    }
    log.info(String.valueOf(client.getAccountHash()));
    RuneScapeProfileType profileType = RuneScapeProfileType.getCurrent(client);
    log.info(profileType.toString());
    log.info(npcLootReceived.toString());
  }

  @Provides
  ExampleConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(ExampleConfig.class);
  }
}
