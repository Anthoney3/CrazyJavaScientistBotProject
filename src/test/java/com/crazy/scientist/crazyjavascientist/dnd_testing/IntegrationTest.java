package com.crazy.scientist.crazyjavascientist.dnd_testing;

import com.crazy.scientist.crazyjavascientist.CrazyJavaScientistApplication;
import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_entities.DNDAttendanceEntity;
import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_entities.DNDPlayersEntity;
import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_repos.DNDAttendanceRepo;
import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_repos.DNDPlayersRepo;
import com.crazy.scientist.crazyjavascientist.exceptions.DNDException;
import com.crazy.scientist.crazyjavascientist.schedulers.DNDScheduledTasks;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
@SpringBootTest(classes = CrazyJavaScientistApplication.class)
@ActiveProfiles("testing")
@Slf4j
public class IntegrationTest {

  @Autowired
  private  DNDScheduledTasks dnd_scheduled_tasks;
  @Autowired
  private  DNDAttendanceRepo dndAttendanceRepo;
  @Autowired
  private  DNDPlayersRepo dndPlayersRepo;
  @Autowired
  private ShardManager shardManager;

  private static ShardManager shardManagerShutdown;

  private static final OffsetDateTime test_start_time = OffsetDateTime.now();

  private final String[] player_names = {
    "Anthony",
    "Nick",
    "Ty",
    "Thai",
    "Pat",
    "Shane",
    "Jared",
    "Gary",
  };
  private final String[] player_nicknames = {
    "Yelgeiros",
    "Doug",
    "Jimbo",
    "Artemis",
    "Sudoku",
    "Uthal",
    "Digby",
    "Metra",
  };
  private final long[] player_discord_ids = {
    448620591944171521L,
    416342612484554752L,
    258377595652014080L,
    154404187256389632L,
    671548924426715152L,
    330537143485333504L,
    274916008643526666L,
    204074647245815808L,
    196742001306107915L,
  };
  private final String[] emojis = { "\uD83D\uDE2D", "\uD83C\uDF5E" };

  public IntegrationTest() throws SQLException {}

  private static boolean initialize_shard_manager = false;
  private static boolean run_clean_up_method = false;
  private static String clean_up_choice = "";
  private static String guild_name_to_use = "";
  private static String channel_name_to_use = "";
  private static String title_context_to_use = "";

  private final String column_one_format = "```%-10.10s";
  private final String column_two_format = "%s```";
  private final String formatInfo = column_one_format + " " + column_two_format;

  @BeforeEach
  public void init() throws LoginException, IOException {
    for (int i = 0; i < player_names.length; i++) {
      dndPlayersRepo.save(
        new DNDPlayersEntity(
          i + 1,
          player_names[i],
          player_nicknames[i],
          String.valueOf(player_discord_ids[i])
        )
      );
      dndAttendanceRepo.save(
        new DNDAttendanceEntity(
          player_discord_ids[i],
          player_names[i],
          "N",
          "N",
          "Y"
        )
      );
    }
    if (!initialize_shard_manager) {
      try {
        shardManagerShutdown = shardManager;
        initialize_shard_manager = true;
      } catch (Exception e) {
        throw new RuntimeException(e.getCause());
      }
    }
  }

  @AfterAll
  public static void final_clean_up() {shardManagerShutdown.shutdown();
  }

  @AfterEach
  public void clean_up_any_messages_sent() {
    if (run_clean_up_method) {
      clean_up_sent_messages_in_discord(
        guild_name_to_use,
        channel_name_to_use,
        clean_up_choice
      );
      run_clean_up_method = false;
    }
  }

  @Test
  public void test_database_status_update_no_one_attending()
          throws SQLException, InterruptedException, ExecutionException, DNDException {
    title_context_to_use = "DND Attendance Status Update";
    guild_name_to_use = "The Java Way";
    channel_name_to_use = "private-bot-testing-channel";
    clean_up_choice = "status_update";
    run_clean_up_method = true;
    dnd_scheduled_tasks.showUpdateForWhoWillBeAttending();

    Awaitility
      .await()
      .pollDelay(Duration.ofSeconds(3))
      .until(() ->
        check_to_see_if_message_sent_exists(
          guild_name_to_use,
          channel_name_to_use,
          title_context_to_use
        )
      );

    assertNotNull(
      find_message_sent_to_testing_channel(
        guild_name_to_use,
        channel_name_to_use,
        title_context_to_use
      )
    );
    assertTrue(
      find_message_sent_to_testing_channel(
        guild_name_to_use,
        channel_name_to_use,
        title_context_to_use
      )
        .getEmbeds()
        .get(0)
        .getDescription()
        .contains("NO_SHOW_OR_NO_RESPONSE")
    );
  }

  @Test
  public void test_embed_message_formatting_and_editing() {
    title_context_to_use = "DND Session Attendance";
    guild_name_to_use = "Bot Testing Server";
    channel_name_to_use = "crazy-java-scientist";
    clean_up_choice = "session_attendance";
    run_clean_up_method = true;

    EmbedBuilder builder = new EmbedBuilder()
      .setTitle(title_context_to_use)
      .setTimestamp(Instant.now());

    String[] messages_found;

    for (int i = 0; i < player_names.length; i++) {
      builder.appendDescription(
        String.format(
          formatInfo,
          player_names[i],
          emojis[new Random().nextInt(emojis.length)]
        )
      );
    }

    MessageEmbed built_message = builder.build();

    shardManager
      .getGuildsByName(guild_name_to_use, true)
      .get(0)
      .getTextChannelsByName(channel_name_to_use, true)
      .get(0)
      .sendMessageEmbeds(built_message)
      .queue();

    Awaitility
      .await()
      .pollDelay(Duration.ofSeconds(3))
      .until(() ->
        check_to_see_if_message_sent_exists(
          guild_name_to_use,
          channel_name_to_use,
          title_context_to_use
        )
      );

    messages_found =
      shardManager
        .getGuildsByName(guild_name_to_use, true)
        .get(0)
        .getTextChannelsByName(channel_name_to_use, true)
        .get(0)
        .getIterableHistory()
        .stream()
        .filter(message ->
          !message.getEmbeds().get(0).isEmpty() && message.getAuthor().isBot()
        )
        .filter(message ->
          message.getEmbeds().get(0).getTitle().contains(title_context_to_use)
        )
        .findFirst()
        .get()
        .getEmbeds()
        .get(0)
        .getDescription()
        .split("```");

    messages_found =
      Arrays
        .stream(messages_found)
        .filter(message -> !message.isEmpty())
        .toList()
        .toArray(new String[0]);

    assertEquals(9, messages_found.length);
  }

  @Test
  public void test_database_status_update_all_attending()
          throws SQLException, InterruptedException, ExecutionException, DNDException {
    title_context_to_use = "DND Attendance Status Update";
    guild_name_to_use = "The Java Way";
    channel_name_to_use = "private-bot-testing-channel";
    clean_up_choice = "status_update";
    run_clean_up_method = true;
    dndAttendanceRepo.updateAllValuesToAttendingForTesting();

    dnd_scheduled_tasks.showUpdateForWhoWillBeAttending();

    Awaitility
      .await()
      .pollDelay(Duration.ofSeconds(3))
      .until(() ->
        check_to_see_if_message_sent_exists(
          guild_name_to_use,
          channel_name_to_use,
          title_context_to_use
        )
      );

    assertNotNull(
      find_message_sent_to_testing_channel(
        guild_name_to_use,
        channel_name_to_use,
        title_context_to_use
      )
    );
    assertTrue(
      find_message_sent_to_testing_channel(
        guild_name_to_use,
        channel_name_to_use,
        title_context_to_use
      )
        .getEmbeds()
        .get(0)
        .getDescription()
        .contains("All Players Expected To Join!")
    );
  }

  @Test
  public void test_database_status_update_has_excused_players()
          throws SQLException, InterruptedException, ExecutionException, DNDException {
    title_context_to_use = "DND Attendance Status Update";
    guild_name_to_use = "The Java Way";
    channel_name_to_use = "private-bot-testing-channel";
    clean_up_choice = "status_update";
    run_clean_up_method = true;

    HashMap<Long, DNDAttendanceEntity> player_responses = new HashMap<>(
      dndAttendanceRepo
        .findAll()
        .stream()
        .collect(
          Collectors.toMap(
            DNDAttendanceEntity::getDiscord_id,
            Function.identity()
          )
        )
    );
    player_responses.forEach((k, v) ->
      player_responses.replace(
        k,
        v,
        new DNDAttendanceEntity(k, v.getPlayers_name(), "N", "Y", "N")
      )
    );
    dndAttendanceRepo.deleteAll();
    dndAttendanceRepo.saveAll(player_responses.values());
    dnd_scheduled_tasks.showUpdateForWhoWillBeAttending();

    Awaitility
      .await()
      .pollDelay(Duration.ofSeconds(3))
      .until(() ->
        check_to_see_if_message_sent_exists(
          guild_name_to_use,
          channel_name_to_use,
          title_context_to_use
        )
      );

    assertNotNull(
      find_message_sent_to_testing_channel(
        guild_name_to_use,
        channel_name_to_use,
        title_context_to_use
      )
    );
    assertTrue(
      find_message_sent_to_testing_channel(
        guild_name_to_use,
        channel_name_to_use,
        title_context_to_use
      )
        .getEmbeds()
        .get(0)
        .getDescription()
        .contains("EXCUSED")
    );
  }

  @Test
  public void test_embed_removed_one_item_and_sent_successfully_updated()
    throws ExecutionException, InterruptedException {
    title_context_to_use = "DND Session Attendance";
    guild_name_to_use = "Bot Testing Server";
    channel_name_to_use = "crazy-java-scientist";
    clean_up_choice = "session_attendance";
    run_clean_up_method = true;

    String user_to_remove = "Anthony";

    EmbedBuilder builder = new EmbedBuilder()
      .setTitle(title_context_to_use)
      .setTimestamp(OffsetDateTime.now());

    HashMap<Long, DNDAttendanceEntity> player_responses = new HashMap<>(
      dndAttendanceRepo
        .findAll()
        .stream()
        .collect(
          Collectors.toMap(
            DNDAttendanceEntity::getDiscord_id,
            Function.identity()
          )
        )
    );

    player_responses.forEach((k, v) ->
      builder.appendDescription(
        String.format(
          formatInfo,
          v.getPlayers_name(),
          emojis[new Random().nextInt(emojis.length)]
        )
      )
    );

    MessageEmbed messageEmbed = builder.build();

    shardManager
      .getGuildsByName(guild_name_to_use, true)
      .get(0)
      .getTextChannelsByName(channel_name_to_use, true)
      .get(0)
      .sendMessageEmbeds(messageEmbed)
      .queue();

    Awaitility
      .await()
      .pollDelay(Duration.ofSeconds(3))
      .until(() ->
        check_to_see_if_message_sent_exists(
          guild_name_to_use,
          channel_name_to_use,
          title_context_to_use
        )
      );

    StringBuilder message_builder = new StringBuilder();

    MessageEmbed found_sent_message_embed = find_message_sent_to_testing_channel(
      guild_name_to_use,
      channel_name_to_use,
      title_context_to_use
    )
      .getEmbeds()
      .get(0);

    EmbedBuilder update_builder = new EmbedBuilder(
      find_message_sent_to_testing_channel(
        guild_name_to_use,
        channel_name_to_use,
        title_context_to_use
      )
        .getEmbeds()
        .get(0)
    );

    List<String> description_text_split = new ArrayList<>(
      Arrays
        .stream(found_sent_message_embed.getDescription().split("```"))
        .filter(item -> !item.isEmpty())
        .toList()
    );

    description_text_split.removeIf(item -> item.contains(user_to_remove));

    description_text_split.forEach(item ->
      message_builder.append(String.format("```%s```", item))
    );

    update_builder.setDescription(message_builder);

    MessageEmbed resent_message = update_builder.build();

    find_message_sent_to_testing_channel(
      guild_name_to_use,
      channel_name_to_use,
      title_context_to_use
    )
      .editMessageEmbeds(resent_message)
      .queue();

    Awaitility
      .await()
      .pollDelay(Duration.ofSeconds(2))
      .until(() ->
        check_to_see_if_message_sent_exists(
          guild_name_to_use,
          channel_name_to_use,
          title_context_to_use
        )
      );

    assertFalse(
      find_message_sent_to_testing_channel(
        guild_name_to_use,
        channel_name_to_use,
        title_context_to_use
      )
        .getEmbeds()
        .get(0)
        .getDescription()
        .contains(user_to_remove)
    );
  }

  @Test
  public void test_embed_added_one_item_and_sent_successfully_updated()
    throws ExecutionException, InterruptedException {
    title_context_to_use = "DND Session Attendance";
    guild_name_to_use = "Bot Testing Server";
    channel_name_to_use = "crazy-java-scientist";
    clean_up_choice = "session_attendance";
    run_clean_up_method = true;

    String user_to_add = "Anthony";

    EmbedBuilder builder = new EmbedBuilder()
      .setTitle(title_context_to_use)
      .setTimestamp(OffsetDateTime.now());

    MessageEmbed messageEmbed = builder.build();

    shardManager
      .getGuildsByName(guild_name_to_use, true)
      .get(0)
      .getTextChannelsByName(channel_name_to_use, true)
      .get(0)
      .sendMessageEmbeds(messageEmbed)
      .queue();

    Awaitility
      .await()
      .pollDelay(Duration.ofSeconds(3))
      .until(() ->
        check_to_see_if_message_sent_exists(
          guild_name_to_use,
          channel_name_to_use,
          title_context_to_use
        )
      );

    EmbedBuilder update_builder = new EmbedBuilder(
      find_message_sent_to_testing_channel(
        guild_name_to_use,
        channel_name_to_use,
        title_context_to_use
      )
        .getEmbeds()
        .get(0)
    );

    update_builder.appendDescription(
      String.format(
        formatInfo,
        user_to_add,
        emojis[new Random().nextInt(emojis.length)]
      )
    );

    MessageEmbed resent_message = update_builder.build();

    find_message_sent_to_testing_channel(
      guild_name_to_use,
      channel_name_to_use,
      title_context_to_use
    )
      .editMessageEmbeds(resent_message)
      .queue();

    Awaitility
      .await()
      .pollDelay(Duration.ofSeconds(2))
      .until(() ->
        check_to_see_if_message_sent_exists(
          guild_name_to_use,
          channel_name_to_use,
          title_context_to_use
        )
      );

    assertTrue(
      find_message_sent_to_testing_channel(
        guild_name_to_use,
        channel_name_to_use,
        title_context_to_use
      )
        .getEmbeds()
        .get(0)
        .getDescription()
        .contains(user_to_add)
    );
  }

  @Test
  public void test_embed_added_multiple_items_and_sent_successfully_updated()
    throws ExecutionException, InterruptedException {
    title_context_to_use = "DND Session Attendance";
    guild_name_to_use = "Bot Testing Server";
    channel_name_to_use = "crazy-java-scientist";
    clean_up_choice = "session_attendance";
    run_clean_up_method = true;

    int number_of_names_added = new Random().nextInt(player_names.length);

    EmbedBuilder builder = new EmbedBuilder()
      .setTitle(title_context_to_use)
      .setTimestamp(OffsetDateTime.now());

    MessageEmbed messageEmbed = builder.build();

    shardManager
      .getGuildsByName(guild_name_to_use, true)
      .get(0)
      .getTextChannelsByName(channel_name_to_use, true)
      .get(0)
      .sendMessageEmbeds(messageEmbed)
      .queue();

    Awaitility
      .await()
      .pollDelay(Duration.ofSeconds(3))
      .until(() ->
        check_to_see_if_message_sent_exists(
          guild_name_to_use,
          channel_name_to_use,
          title_context_to_use
        )
      );

    EmbedBuilder update_builder = new EmbedBuilder(
      find_message_sent_to_testing_channel(
        guild_name_to_use,
        channel_name_to_use,
        title_context_to_use
      )
        .getEmbeds()
        .get(0)
    );

    for (int i = 0; i < number_of_names_added; i++) {
      update_builder.appendDescription(
        String.format(
          formatInfo,
          player_names[new Random().nextInt(player_names.length)],
          emojis[new Random().nextInt(emojis.length)]
        )
      );
    }
    MessageEmbed resent_message = update_builder.build();

    find_message_sent_to_testing_channel(
      guild_name_to_use,
      channel_name_to_use,
      title_context_to_use
    )
      .editMessageEmbeds(resent_message)
      .queue();

    Awaitility
      .await()
      .pollDelay(Duration.ofSeconds(2))
      .until(() ->
        check_to_see_if_message_sent_exists(
          guild_name_to_use,
          channel_name_to_use,
          title_context_to_use
        )
      );

    assertEquals(
      Arrays
        .stream(
          find_message_sent_to_testing_channel(
            guild_name_to_use,
            channel_name_to_use,
            title_context_to_use
          )
            .getEmbeds()
            .get(0)
            .getDescription()
            .split("```")
        )
        .filter(item -> !item.isEmpty())
        .toList()
        .size(),
      number_of_names_added
    );
  }

  @Test
  public void test_embed_removes_multiple_items_and_sent_successfully_updated()
    throws ExecutionException, InterruptedException {
    title_context_to_use = "DND Session Attendance";
    guild_name_to_use = "Bot Testing Server";
    channel_name_to_use = "crazy-java-scientist";
    clean_up_choice = "session_attendance";
    run_clean_up_method = true;

    EmbedBuilder builder = new EmbedBuilder()
      .setTitle(title_context_to_use)
      .setTimestamp(OffsetDateTime.now());

    for (int i = 0; i < player_names.length; i++) {
      builder.appendDescription(
        String.format(
          formatInfo,
          player_names[i],
          emojis[new Random().nextInt(emojis.length)]
        )
      );
    }

    MessageEmbed messageEmbed = builder.build();

    shardManager
      .getGuildsByName(guild_name_to_use, true)
      .get(0)
      .getTextChannelsByName(channel_name_to_use, true)
      .get(0)
      .sendMessageEmbeds(messageEmbed)
      .queue();

    Awaitility
      .await()
      .pollDelay(Duration.ofSeconds(3))
      .until(() ->
        check_to_see_if_message_sent_exists(
          guild_name_to_use,
          channel_name_to_use,
          title_context_to_use
        )
      );

    StringBuilder message_builder = new StringBuilder();

    EmbedBuilder update_builder = new EmbedBuilder(
      find_message_sent_to_testing_channel(
        guild_name_to_use,
        channel_name_to_use,
        title_context_to_use
      )
        .getEmbeds()
        .get(0)
    );

    List<String> description_split = new ArrayList<>(
      Stream
        .of(
          find_message_sent_to_testing_channel(
            guild_name_to_use,
            channel_name_to_use,
            title_context_to_use
          )
            .getEmbeds()
            .get(0)
            .getDescription()
            .split("```")
        )
        .filter(item -> !item.isEmpty())
        .toList()
    );

    description_split.removeIf(item ->
      item.contains("Anthony") ||
      item.contains("Zach") ||
      item.contains("Nick") ||
      item.contains("Jared")
    );

    description_split.forEach(item ->
      message_builder.append(String.format("```%s```", item))
    );
    update_builder.setDescription(message_builder);

    MessageEmbed resent_message = update_builder.build();

    find_message_sent_to_testing_channel(
      guild_name_to_use,
      channel_name_to_use,
      title_context_to_use
    )
      .editMessageEmbeds(resent_message)
      .queue();

    Awaitility
      .await()
      .pollDelay(Duration.ofSeconds(2))
      .until(() ->
        check_to_see_if_message_sent_exists(
          guild_name_to_use,
          channel_name_to_use,
          title_context_to_use
        )
      );

    assertEquals(
      4,
      Arrays
        .stream(
          find_message_sent_to_testing_channel(
            guild_name_to_use,
            channel_name_to_use,
            title_context_to_use
          )
            .getEmbeds()
            .get(0)
            .getDescription()
            .split("```")
        )
        .filter(item -> !item.isEmpty())
        .toList()
        .size()
    );
  }

  private boolean check_to_see_if_message_sent_exists(
    String guild_name,
    String channel_name,
    String embed_title
  ) throws ExecutionException, InterruptedException {
    return shardManager
      .getGuildsByName(guild_name, true)
      .get(0)
      .getTextChannelsByName(channel_name, true)
      .get(0)
      .getIterableHistory()
      .stream()
      .anyMatch(message ->
        message.getEmbeds().get(0).getTitle().contains(embed_title) &&
        message.getEmbeds().get(0).getTimestamp().isAfter(test_start_time) &&
        message.getAuthor().isBot()
      );
  }

  private Message find_message_sent_to_testing_channel(
    String guild_name,
    String channel_name,
    String embed_title
  ) throws ExecutionException, InterruptedException {
    return shardManager
      .getGuildsByName(guild_name, true)
      .get(0)
      .getTextChannelsByName(channel_name, true)
      .get(0)
      .getIterableHistory()
      .stream()
      .limit(50)
      .filter(message ->
        message.getEmbeds().get(0).getTitle().contains(embed_title) &&
        message.getEmbeds().get(0).getTimestamp().isAfter(test_start_time) &&
        message.getAuthor().isBot()
      )
      .findFirst()
      .get();
  }

  private void clean_up_sent_messages_in_discord(
    String guild_name,
    String channel_name,
    String clean_up_to_run
  ) {
    switch (clean_up_to_run) {
      case "status_update" -> {
        shardManager
          .getGuildsByName(guild_name, true)
          .get(0)
          .getTextChannelsByName(channel_name, true)
          .get(0)
          .getIterableHistory()
          .stream()
          .filter(message ->
            (
              message.getEmbeds().get(0).getDescription().contains("EXCUSED") ||
              message
                .getEmbeds()
                .get(0)
                .getDescription()
                .contains("NO_SHOW_OR_NO_RESPONSE") ||
              message.getEmbeds().get(0).getDescription().contains("ATTENDING")
            ) &&
            message.getEmbeds().get(0).getTimestamp().isAfter(test_start_time)
          )
          .findAny()
          .get()
          .delete()
          .queue();
      }
      case "session_attendance" -> {
        shardManagerShutdown
          .getGuildsByName(guild_name, true)
          .get(0)
          .getTextChannelsByName(channel_name, true)
          .get(0)
          .getIterableHistory()
          .stream()
          .filter(message ->
            (
              message
                .getEmbeds()
                .get(0)
                .getTitle()
                .contains("DND Session Attendance")
            ) &&
            message.getEmbeds().get(0).getTimestamp().isAfter(test_start_time)
          )
          .findAny()
          .get()
          .delete()
          .queue();
      }
      default -> throw new RuntimeException(
        "No Possible Clean Up choices were specified"
      );
    }
  }
}
