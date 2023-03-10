package com.crazy.scientist.crazyjavascientist.dnd;

import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.PlayerResponse;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.DNDAttendanceRepo;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.DNDPlayersRepo;
import com.crazy.scientist.crazyjavascientist.dnd.enums.UnicodeResponses;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.ZonedDateTime;
import java.util.HashMap;

import static com.crazy.scientist.crazyjavascientist.constants.StaticUtils.shardManager;

@Slf4j
@Component
@NoArgsConstructor
@Getter
@Setter
@ToString
public class DNDService extends ListenerAdapter {

    @Autowired
    private DNDPlayersRepo dndPlayersRepo;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DNDAttendanceRepo attendanceRepo;
    private HashMap<Long, PlayerResponse> discord_response = new HashMap<>();


    public void testingEmbedsWithActionRows(boolean isAllowedToUseCommand, @Nonnull SlashCommandInteraction event) {
        if (event.getName().equalsIgnoreCase("dnd-test") && isAllowedToUseCommand) {
            Button excused_button = Button.danger("excused_button", Emoji.fromUnicode("\uD83D\uDE2D"));
            Button attending_button = Button.success("attending_button", Emoji.fromUnicode("\uD83C\uDF5E"));
            Button remove_button = Button.secondary("remove_button", Emoji.fromUnicode("\uD83D\uDDD1"));
            Button alpharius_button = Button.success("alpharius_button", Emoji.fromFormatted("<:alpharius:1045825620300529818>"));

            MessageEmbed builder = new EmbedBuilder().setTitle("DND Session Attendance").setTimestamp(ZonedDateTime.now()).build();

            event.reply(new MessageCreateBuilder().setActionRow((ItemComponent)ActionRow.of(attending_button, alpharius_button, excused_button, remove_button)).setEmbeds(builder).build()).queue();
        } else {
            event.replyFormat("You are not allowed to use slash commands%n Please reach out to  %s  and he can allow you to use commands ", event.getJDA().getUserById(416342612484554752L).getName()).queue();
        }
    }

    public void dndAttendanceButtonInteractionEvent(@NotNull ButtonInteractionEvent event) {

        try {
            EmbedBuilder builder = new EmbedBuilder(event.getInteraction().getMessage().getEmbeds().get(0));

            PlayerResponse current_player_response = discord_response.get(event.getUser().getIdLong());

            String column_one_format = "```%-10.10s";
            String column_two_format = "%s```";
            String formatInfo = column_one_format + " " + column_two_format;

            boolean hasResponded = !discord_response.get(event.getUser().getIdLong()).getResponse_emoji_unicode().equals(UnicodeResponses.NO_SHOW_NO_RESPONSE);

            if (hasResponded && !event.getButton().getId().equalsIgnoreCase("remove_button")) {
                event.reply("Your Response has already been recorded, If you want to change it use the remove button first").setEphemeral(true).queue();
            } else if (!hasResponded && event.getButton().getId().equalsIgnoreCase("remove_button")) {
                event.reply("You need to add a response before you can delete it!").setEphemeral(true).queue();
            } else if (hasResponded && event.getButton().getId().equalsIgnoreCase("remove_button")) {
                discord_response.replace(event.getUser().getIdLong(), current_player_response, new PlayerResponse(current_player_response.getPlayer_name(), UnicodeResponses.NO_SHOW_NO_RESPONSE));

                StringBuilder build_message = new StringBuilder();

                discord_response.entrySet().stream().filter(item -> !item.getValue().getResponse_emoji_unicode().equals(UnicodeResponses.NO_SHOW_NO_RESPONSE)).forEach(item -> build_message.append(String.format(formatInfo, item.getValue().getPlayer_name(), item.getValue().getResponse_emoji_unicode().getResponse())));
                builder.setDescription(build_message);

                log.info("{}'s Response has been Updated to No Show as {} removed their response", current_player_response.getPlayer_name(), current_player_response.getPlayer_name());

                event.getInteraction().getMessage().editMessageEmbeds(builder.build()).complete();

                event.reply("Your Response Has been Removed").setEphemeral(true).submit();
            } else {
                if (event.getButton().getId().equalsIgnoreCase("excused_button")) {
                    discord_response.replace(event.getUser().getIdLong(), current_player_response, new PlayerResponse(current_player_response.getPlayer_name(), UnicodeResponses.EXCUSED));

                    //Edits Embed Message with Updated Record
                    builder.appendDescription(String.format(formatInfo, discord_response.get(event.getUser().getIdLong()).getPlayer_name(), discord_response.get(event.getUser().getIdLong()).getResponse_emoji_unicode().getResponse()));

                    MessageEmbed messageEmbed = builder.build();
                    event.getInteraction().getMessage().editMessageEmbeds(messageEmbed).complete();

                    log.info("{}'s status updated to Excused", current_player_response.getPlayer_name());

                    event.reply("Your Response has been Added!").setEphemeral(true).submit();
                } else if (event.getButton().getId().equalsIgnoreCase("attending_button") || (event.getButton().getId().equalsIgnoreCase("alpharius_button") && ((event.getUser().getIdLong() == 204074647245815808L) || (event.getUser().getIdLong() == 416342612484554752L)))) {
                    discord_response.replace(event.getUser().getIdLong(), current_player_response, new PlayerResponse(current_player_response.getPlayer_name(), UnicodeResponses.ATTENDING));

                    //Edits Embed Message with Updated Record
                    builder.appendDescription(String.format(formatInfo, discord_response.get(event.getUser().getIdLong()).getPlayer_name(), discord_response.get(event.getUser().getIdLong()).getResponse_emoji_unicode().getResponse()));

                    MessageEmbed messageEmbed = builder.build();
                    event.getInteraction().getMessage().editMessageEmbeds(messageEmbed).complete();

                    log.info("{}'s status updated to Attending", current_player_response.getPlayer_name());

                    event.reply("Your Response has been Added!").setEphemeral(true).submit();
                } else {
                    event.reply("This button only works for a special individual! Please use either the bread Emoji or Crying Emoji for your response").setEphemeral(true).queue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            shardManager.getTextChannelsByName("private-bot-testing-channel", true).get(0).sendMessage("Something went wrong, ☕ Java Masochist ☕ will look into it").queue();
        }
    }


}
