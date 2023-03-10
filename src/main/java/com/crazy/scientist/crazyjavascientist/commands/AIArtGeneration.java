package com.crazy.scientist.crazyjavascientist.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@NoArgsConstructor
@Slf4j
public class AIArtGeneration {

    public void sendArtToShowcaseChannel(@NotNull SlashCommandInteractionEvent event){
            List<String> responseMessages = new ArrayList<>();

            String userName = event.getUser().getName();
            responseMessages.add(userName + " Added this beauty to Show off!");
            responseMessages.add(userName + " is such a show off!");
            responseMessages.add(userName + " You're gonna have to teach me how you did this one!");
            responseMessages.add(userName + " I'm so Jelly!");

            int messagePick = (int) (Math.random() * responseMessages.size());

            TextChannel textChannel1 = (TextChannel) Objects.requireNonNull(event.getGuild()).getGuildChannelById(1010606877236789319L);

            if (textChannel1 != null) {
                log.info("Message " + event.getOption("message-id").getAsString() + " was added to the " + textChannel1.getName());
                Message messageToBeMoved = event.getChannel().retrieveMessageById(Objects.requireNonNull(event.getOption("message-id")).getAsString()).complete();
                event.reply("Your message was sent to " + textChannel1.getName() + "!").queue();
                textChannel1.sendMessage(responseMessages.get(messagePick)).queue();
                textChannel1.sendMessageFormat("%s%n%s", messageToBeMoved.getReferencedMessage(), messageToBeMoved.getAttachments().get(0).getUrl()).queue();
            }
    }
}
