package com.crazy.scientist.crazyjavascientist.dnd;

import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.DNDAttendanceEntity;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.DNDAttendanceRepo;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.DNDPlayersRepo;
import com.crazy.scientist.crazyjavascientist.utils.DBTablePrinter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.crazy.scientist.crazyjavascientist.config.DiscordBotConfigJDAStyle.player_responses;

@Slf4j
@Component
@NoArgsConstructor
@Getter
@ToString
public class DNDTesting extends ListenerAdapter {

    @Autowired
    private DNDPlayersRepo dndPlayersRepo;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DNDAttendanceRepo attendanceRepo;

    @Value("${spring.datasource.url}")
    private String db_url;

    @Value("${spring.datasource.username}")
    private String db_username;

    @Value("${spring.datasource.password}")
    private String db_password;


    public void testingEmbedsWithActionRows(boolean isAllowedToUseCommand, @Nonnull SlashCommandInteraction event) {


        if (event.getName().equalsIgnoreCase("dnd-test") && isAllowedToUseCommand) {

            Button excused_button = Button.danger("excused_button", Emoji.fromUnicode("\uD83D\uDE2D"));
            Button attending_button = Button.success("attending_button", Emoji.fromUnicode("\uD83C\uDF5E"));
            Button remove_button = Button.secondary("remove_button", Emoji.fromUnicode("\uD83D\uDDD1"));
            Button alpharius_button = Button.success("alpharius_button",Emoji.fromMarkdown("<:alpharius:1045825620300529818>"));

            MessageEmbed builder = new EmbedBuilder().setTitle("DND Session Attendance").setTimestamp(ZonedDateTime.now()).build();

            Message message = new MessageBuilder().setActionRows(ActionRow.of(attending_button,alpharius_button,excused_button, remove_button))
//                    .setContent("@here")
                    .setEmbeds(builder).build();

            event.reply(message).queue();

        } else {
            event.replyFormat("You are not allowed to use slash commands%n Please reach out to  %s  and he can allow you to use commands ", event.getJDA().getUserById(416342612484554752L).getName()).queue();
        }

    }


    public void test_epheral_message_with_coding_block(boolean isAllowedToUseCommand, @Nonnull SlashCommandInteraction event){

        if(event.getName().equalsIgnoreCase("test-code-block-message")){

            try{


                Connection connection = DriverManager.getConnection(db_url, db_username, db_password);

                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle("DND Weekly History")
                        .setThumbnail("https://yawningportal.org/wp-content/uploads/2019/09/dnddescentkeyartjpg-1.jpeg")
                        .setTimestamp(OffsetDateTime.now());

                builder.appendDescription("```");
                builder.appendDescription(DBTablePrinter.printTable(connection,"SELECT * FROM DND_ATTENDANCE_HISTORY","DND_ATTENDANCE_HISTORY"));
                builder.appendDescription("```");
                MessageEmbed embed = builder.build();

                event.replyEmbeds(embed).setEphemeral(true).queue();


            } catch (Exception e) {
                event.getJDA().getUserById(416342612484554752L).openPrivateChannel().queue(channel -> {
                    channel.sendMessage(e.getCause().getMessage()).queue();
                });
            }
        }
    }



    @Transactional
    @Modifying
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        try {

            EmbedBuilder builder = new EmbedBuilder(event.getInteraction().getMessage().getEmbeds().get(0));



            String users_real_name = player_responses.get(event.getUser().getIdLong()).getPlayers_name();

            String column_one_format = "```%-10.10s";
            String column_two_format = "%s```";
            String formatInfo = column_one_format + " " + column_two_format;

            boolean hasResponded = player_responses.get(event.getUser().getIdLong()).getNo_show().equalsIgnoreCase("n");

            if (hasResponded && !event.getButton().getId().equalsIgnoreCase("remove_button")) {
                event.reply("Your Response has already been recorded, If you want to change it use the remove button first").setEphemeral(true).queue();

            } else if (!hasResponded && event.getButton().getId().equalsIgnoreCase("remove_button")) {

                event.reply("You need to add a response before you can delete it!").setEphemeral(true).queue();

            } else if (hasResponded && event.getButton().getId().equalsIgnoreCase("remove_button")) {

                List<String> split_messages = new ArrayList<>(Stream.of(builder.build().getDescription().split("```")).filter(string -> !string.isEmpty()).toList());

                split_messages.removeIf(string -> string.contains(users_real_name));

                StringBuilder build_message = new StringBuilder();
                split_messages.forEach(message -> build_message.append(String.format("```%s```", message)));

                builder.setDescription(build_message);

                player_responses.replace(event.getUser().getIdLong(),player_responses.get(event.getUser().getIdLong()),new DNDAttendanceEntity(event.getUser().getIdLong(),player_responses.get(event.getUser().getIdLong()).getPlayers_name(),"N","N","Y"));

                log.info("{}'s Response has been Updated to No Show as {} removed their response", users_real_name, users_real_name);

                event.getInteraction().getMessage().editMessageEmbeds(builder.build()).complete();

                event.reply("Your Response Has been Removed").setEphemeral(true).submit();

            } else {

                if (event.getButton().getId().equalsIgnoreCase("alpharius_button")) {
                    if ((event.getUser().getIdLong() == 204074647245815808L) || (event.getUser().getIdLong() == 416342612484554752L)) {
                        //Edits Embed Message with Updated Record
                        builder.appendDescription(String.format(formatInfo, users_real_name, "\uD83C\uDF5E"));

                        MessageEmbed messageEmbed = builder.build();
                        event.getInteraction().getMessage().editMessageEmbeds(messageEmbed).complete();

                        player_responses.replace(event.getUser().getIdLong(),player_responses.get(event.getUser().getIdLong()),new DNDAttendanceEntity(event.getUser().getIdLong(),player_responses.get(event.getUser().getIdLong()).getPlayers_name(),"Y","N","N"));

                        log.info("{}'s status updated to Attending", users_real_name);

                        event.reply("Your Response has been Added!").setEphemeral(true).submit();

                    }else{
                        event.reply("This button only works for a special individual! Please use either the bread Emoji or Crying Emoji for your response").setEphemeral(true).queue();
                    }
                }
                if (event.getButton().getId().equalsIgnoreCase("excused_button")) {


                    //Edits Embed Message with Updated Record
                    builder.appendDescription(String.format(formatInfo, users_real_name, "\uD83D\uDE2D"));

                    MessageEmbed messageEmbed = builder.build();
                    event.getInteraction().getMessage().editMessageEmbeds(messageEmbed).complete();

                    player_responses.replace(event.getUser().getIdLong(),player_responses.get(event.getUser().getIdLong()),new DNDAttendanceEntity(event.getUser().getIdLong(),player_responses.get(event.getUser().getIdLong()).getPlayers_name(),"N","Y","N"));

                    log.info("{}'s status updated to Excused", users_real_name);

                    event.reply("Your Response has been Added!").setEphemeral(true).submit();

                }
                if (event.getButton().getId().equalsIgnoreCase("attending_button")) {

                    builder.appendDescription(String.format(formatInfo, users_real_name, "\uD83C\uDF5E"));

                    MessageEmbed messageEmbed = builder.build();
                    event.getInteraction().getMessage().editMessageEmbeds(messageEmbed).complete();

                    player_responses.replace(event.getUser().getIdLong(),player_responses.get(event.getUser().getIdLong()),new DNDAttendanceEntity(event.getUser().getIdLong(),player_responses.get(event.getUser().getIdLong()).getPlayers_name(),"Y","N","N"));
                    log.info("{}'s status updated to Attending", users_real_name);

                    event.reply("Your Response has been Added!").setEphemeral(true).submit();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            event.reply("Something went wrong, ☕ Java Masochist ☕ will look into it").queue();
        }
    }



}
