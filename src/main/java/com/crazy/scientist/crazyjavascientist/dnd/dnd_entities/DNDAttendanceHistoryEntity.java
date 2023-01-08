package com.crazy.scientist.crazyjavascientist.dnd.dnd_entities;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "DND_ATTENDANCE_HISTORY")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DNDAttendanceHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "PLAYERS_ATTENDED")
    private int players_attended;

    @Column(name = "PLAYERS_EXCUSED")
    private int players_excused;

    @Column(name = "PLAYERS_NO_SHOW_OR_NO_RESPONSE")
    private int players_no_show;

    @Column(name="PLAYERS_NAMES_ATTENDED")
    private String players_names_attended;

    @Column(name = "PLAYERS_NAMES_EXCUSED")
    private String players_names_excused;

    @Column(name="PLAYERS_NAMES_NO_SHOW_OR_NO_RESPONSE")
    private String players_names_no_show;

    @Column(name="WEEK_OF_ATTENDANCE")
    private String week_of_attendance;

}
