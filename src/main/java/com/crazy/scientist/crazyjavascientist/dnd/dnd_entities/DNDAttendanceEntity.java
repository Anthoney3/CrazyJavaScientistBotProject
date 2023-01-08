package com.crazy.scientist.crazyjavascientist.dnd.dnd_entities;

import lombok.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;

@Entity
@Table(name = "DND_ATTENDANCE_INFO")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DNDAttendanceEntity {

    @Id
    @Column(name = "DISCORD_ID")
    private long discord_id;

    @Column(name = "PLAYERS_NAME")
    private String players_name;

    @Column(name="ATTENDING")
    private String attending;

    @Column(name = "EXCUSED")
    private String excused;

    @Column(name="NO_SHOW_OR_NO_RESPONSE")
    private String no_show;



}
