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

    @Column(name="PLAYERNAME")
    private String player_name;

    @Column(name = "ATTENDED")
    private String attended;

    @Column(name="NO_SHOW")
    private String no_show;

    @Column(name="EXCUSED")
    private String excused;

    @Column(name="WEEK_OF")
    private String week_of;

}
