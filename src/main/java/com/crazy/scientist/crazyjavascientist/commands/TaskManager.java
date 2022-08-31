package com.crazy.scientist.crazyjavascientist.commands;

import com.crazy.scientist.crazyjavascientist.models.UserTaskTable;
import com.crazy.scientist.crazyjavascientist.models.UserTaskTableI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class TaskManager {


    @Autowired
    UserTaskTableI userTaskTable;


    public void createNewUserList(@Nonnull SlashCommandInteraction event){

        UserTaskTable taskTable = new UserTaskTable();


        if(event.getOptions().isEmpty()){
            taskTable.setTaskTitle(event.getOption("title").getAsString());
            taskTable.setUsername(event.getUser().getName());
        }else{

            taskTable.setUsername(event.getUser().getName());
            taskTable.setTaskTitle(event.getOption("title").getAsString());
            taskTable.setTaskDescription(event.getOption("description").getAsString());
            taskTable.setTaskStatus(event.getOption("status").getAsString());
            taskTable.setTaskComments(event.getOption("comments").getAsString());
        }




        userTaskTable.saveUserTaskTable(taskTable);

/*

        MessageEmbed responseMessage = new EmbedBuilder()
                .setTitle(userTaskTable.get)
*/

        event.reply("List Created Successfully").queue();








    }

}
