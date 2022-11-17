package com.crazy.scientist.crazyjavascientist.dnd;

import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.DNDAttendanceRepo;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.DNDPlayersRepo;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
@NoArgsConstructor
public class DNDTesting extends ListenerAdapter {

    @Autowired
    private DNDPlayersRepo dndPlayersRepo;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DNDAttendanceRepo attendanceRepo;


    public void testingEmbedsWithActionRows(boolean isAllowedToUseCommand, @Nonnull SlashCommandInteraction event) {


        if (event.getName().equalsIgnoreCase("dnd-test") && isAllowedToUseCommand) {

            Button excused_button = Button.danger("excused_button", Emoji.fromUnicode("\uD83D\uDE2D"));
            Button attending_button = Button.success("attending_button", Emoji.fromUnicode("\uD83C\uDF5E"));
            Button remove_button = Button.secondary("remove_button", Emoji.fromUnicode("\uD83D\uDDD1"));

            MessageEmbed builder = new EmbedBuilder().setTitle("DND Session Attendance").setTimestamp(ZonedDateTime.now()).build();

            Message message = new MessageBuilder().setActionRows(ActionRow.of(attending_button, excused_button, remove_button))
//                    .setContent("@here")
                    .setEmbeds(builder).build();

            event.reply(message).queue();

        } else {
            event.replyFormat("You are not allowed to use slash commands%n Please reach out to  %s  and he can allow you to use commands ", event.getJDA().getUserById(416342612484554752L).getName()).queue();
        }

    }



    @Transactional
    @Modifying
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {


        EmbedBuilder builder = new EmbedBuilder(event.getInteraction().getMessage().getEmbeds().get(0));


        Query getAttendingQuery = entityManager.createNativeQuery("SELECT ATTENDING FROM DND_ATTENDANCE_INFO WHERE PLAYERS_NAME= (SELECT PLAYERS_NAME FROM DND_PLAYERS_INFO WHERE DISCORD_USER_ID=?)");
        Query getExcusedQuery = entityManager.createNativeQuery("SELECT EXCUSED FROM DND_ATTENDANCE_INFO WHERE PLAYERS_NAME= (SELECT PLAYERS_NAME FROM DND_PLAYERS_INFO WHERE DISCORD_USER_ID=?)");
        Query getUsersRealName = entityManager.createNativeQuery("SELECT PLAYERS_NAME FROM DND_PLAYERS_INFO WHERE DISCORD_USER_ID=?");




        getAttendingQuery.setParameter(1, event.getUser().getId());
        getExcusedQuery.setParameter(1, event.getUser().getId());
        getUsersRealName.setParameter(1, event.getUser().getId());

        String users_real_name = getUsersRealName.getResultList().get(0).toString();

        String column_one_format = "```%-10.10s";
        String column_two_format = "%s```";
        String formatInfo = column_one_format + " " + column_two_format;

        boolean hasResponded = getAttendingQuery.getResultList().get(0).toString().equalsIgnoreCase("Y") || getExcusedQuery.getResultList().get(0).toString().equalsIgnoreCase("Y");


        if (hasResponded && !event.getButton().getId().equalsIgnoreCase("remove_button")) {
            event.reply("Your Response has already been recorded, If you want to change it use the remove button first").setEphemeral(true).queue();

        } else if (!hasResponded && event.getButton().getId().equalsIgnoreCase("remove_button")) {

            event.reply("You need to add a response before you can delete it!").setEphemeral(true).queue();

        } else if (hasResponded && event.getButton().getId().equalsIgnoreCase("remove_button")) {

            MessageEmbed incomingMessage = new EmbedBuilder(event.getInteraction().getMessage().getEmbeds().get(0)).build();

            List<String> split_messages = new ArrayList<>(Stream.of(incomingMessage.getDescription().split("```")).filter(string -> !string.isEmpty()).toList());

            split_messages.removeIf(string -> string.contains(dndPlayersRepo.getPlayerNameByDiscordUserID(event.getUser().getId())));

            StringBuilder build_message = new StringBuilder();
            split_messages.forEach(message -> build_message.append(String.format("```%s```", message)));

            EmbedBuilder edited_message = new EmbedBuilder(event.getInteraction().getMessage().getEmbeds().get(0));

            edited_message.setDescription(build_message);

            log.info(build_message.toString());

            MessageEmbed outgoingEmbed = edited_message.build();

            Query updateResponseQuery = entityManager.createNativeQuery("UPDATE DND_ATTENDANCE_INFO SET EXCUSED='N',ATTENDING='N',NO_SHOW_OR_NO_RESPONSE='Y' WHERE PLAYERS_NAME= (SELECT PLAYERS_NAME FROM DND_PLAYERS_INFO WHERE DISCORD_USER_ID=?)");
            updateResponseQuery.setParameter(1, event.getUser().getId());
            updateResponseQuery.executeUpdate();
            log.info("{}'s Response has been Updated to No Show as {} removed their response", users_real_name, users_real_name);

            event.getInteraction().getMessage().editMessageEmbeds(outgoingEmbed).complete();

            event.reply("Your Response Has been Removed").setEphemeral(true).submit();

        } else {

            if (event.getButton().getId().equalsIgnoreCase("excused_button")) {


                //Edits Embed Message with Updated Record
                builder.appendDescription(String.format(formatInfo, users_real_name, "\uD83D\uDE2D"));

                MessageEmbed messageEmbed = builder.build();
                event.getInteraction().getMessage().editMessageEmbeds(messageEmbed).complete();


                Query updateExcusedAttendanceQuery = entityManager.createNativeQuery("UPDATE DND_ATTENDANCE_INFO SET EXCUSED='Y',NO_SHOW_OR_NO_RESPONSE='N' WHERE PLAYERS_NAME= (SELECT PLAYERS_NAME FROM DND_PLAYERS_INFO WHERE DISCORD_USER_ID=?)");
                updateExcusedAttendanceQuery.setParameter(1, event.getUser().getId());
                updateExcusedAttendanceQuery.executeUpdate();

                log.info("{}'s status updated to Excused", users_real_name);

                event.reply("Your Response has been Added!").setEphemeral(true).submit();

            }
            if (event.getButton().getId().equalsIgnoreCase("attending_button")) {

                builder.appendDescription(String.format(formatInfo, users_real_name, "\uD83C\uDF5E"));

                MessageEmbed messageEmbed = builder.build();
                event.getInteraction().getMessage().editMessageEmbeds(messageEmbed).complete();
//                event.getInteraction().getMessage().addReaction("\uD83C\uDF5E").queue();

                Query updateAttendingAttendanceQuery = entityManager.createNativeQuery("UPDATE DND_ATTENDANCE_INFO SET ATTENDING='Y', NO_SHOW_OR_NO_RESPONSE='N' WHERE PLAYERS_NAME= (SELECT PLAYERS_NAME FROM DND_PLAYERS_INFO WHERE DISCORD_USER_ID=?)");
                updateAttendingAttendanceQuery.setParameter(1, event.getUser().getId());
                updateAttendingAttendanceQuery.executeUpdate();

                log.info("{}'s status updated to Attending", users_real_name);

                event.reply("Your Response has been Added!").setEphemeral(true).submit();
            }
        }
    }
}
