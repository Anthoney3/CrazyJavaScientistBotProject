package com.crazy.scientist.crazyjavascientist.commands;

import com.crazy.scientist.crazyjavascientist.enums.TaskManagerStatus;
import com.crazy.scientist.crazyjavascientist.models.UserTaskTable;
import com.crazy.scientist.crazyjavascientist.repos.UserTaskTableI;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Text;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TaskManager {


    @Autowired
    UserTaskTableI userTaskTable;


    public void createNewUserList(boolean hasPermission, @Nonnull SlashCommandInteraction event) {

        if (hasPermission) {

            UserTaskTable taskTable = new UserTaskTable();


            int numOfTaskLists = userTaskTable.getUserTaskListCount(event.getUser().getName());


            if (numOfTaskLists == 5) {

                event.reply("You have hit the limit of Lists allowed, The limit is 5. Please Remove a list before adding a new one").queue();
            } else {

                if (event.getOptions().isEmpty()) {

                    TextInput task_list_title = TextInput.create("task-list-title", "Task List Title", TextInputStyle.SHORT).build();

                    TextInput task_list_description = TextInput.create("task-list-description", "First Task's Description", TextInputStyle.PARAGRAPH).build();

//                TextInput task_list_status = TextInput.create("task-list-status","First Task's Status",TextInputStyle.UNKNOWN).build();


                    TextInput task_list_comments = TextInput.create("task-list-comment", "First Task's Comments", TextInputStyle.PARAGRAPH).build();

                    TextInput statusMenu = TextInput.create("status-menu", "First Task's Status", TextInputStyle.SHORT).build();


                    Modal taskListModal = Modal.create("create-task-list-modal", "Create A Task List!")
                            .addActionRows(ActionRow.of(task_list_title), ActionRow.of(task_list_description), ActionRow.of(statusMenu), ActionRow.of(task_list_comments))
                            .build();


                    event.replyModal(taskListModal).queue();

                /*taskTable.setTaskTitle(event.getOption("title").getAsString());
                taskTable.setUsername(event.getUser().getName());*/
                } else {

              /*  taskTable.setUsername(event.getUser().getName());
                taskTable.setTaskTitle(event.getOption("title").getAsString());
                taskTable.setTaskDescription(event.getOption("description").getAsString());
                taskTable.setTaskStatus(event.getOption("status").getAsString());
                taskTable.setTaskComments(event.getOption("comments").getAsString());*/

                    TextInput task_list_title = TextInput.create("task-list-title", "Task List Title", TextInputStyle.SHORT).build();

                    TextInput task_list_description = TextInput.create("task-list-description", "First Task's Description", TextInputStyle.PARAGRAPH).build();

//                TextInput task_list_status = TextInput.create("task-list-status","First Task's Status",TextInputStyle.UNKNOWN).build();


                    TextInput task_list_comments = TextInput.create("task-list-comment", "First Task's Comments", TextInputStyle.PARAGRAPH).build();

                    SelectMenu statusMenu = SelectMenu.create("status-menu").addOption("status", "Status", "sets the status for the task at hand").build();

                    Modal taskListModal = Modal.create("create-task-list-modal", "Create A Task List!")
                            .addActionRows(ActionRow.of(task_list_title), ActionRow.of(task_list_description), ActionRow.of(statusMenu), ActionRow.of(task_list_comments))
                            .build();
                    event.replyModal(taskListModal).queue();

                }

                userTaskTable.save(taskTable);
                event.reply("List Created Successfully").queue();
            }

        } else {
            event.replyFormat("You are not allowed to use slash commands%n Please reach out to  %s  and he can allow you to use commands ", event.getJDA().getUserById(416342612484554752L).getName()).queue();
        }


    }

    public void deleteUserTaskListByTitle(boolean hasPermission,@Nonnull SlashCommandInteraction event) {


        if(hasPermission) {
            if (userTaskTable.checkIfUserTaskListExists(event.getOption("title").getAsString(), event.getUser().getName()).isEmpty()) {

                event.replyFormat("No User Task Lists with the name %s found", event.getOption("title").getAsString()).queue();
            } else {

                userTaskTable.deleteTaskList(event.getOption("title").getAsString(), event.getUser().getName());

                event.replyFormat("User Task List \"%s\" has been deleted successfully!", event.getOption("title").getAsString()).queue();

            }
        }else{
            event.replyFormat("You are not allowed to use slash commands%n Please reach out to  %s  and he can allow you to use commands ", event.getJDA().getUserById(416342612484554752L).getName()).queue();
        }


    }

}
