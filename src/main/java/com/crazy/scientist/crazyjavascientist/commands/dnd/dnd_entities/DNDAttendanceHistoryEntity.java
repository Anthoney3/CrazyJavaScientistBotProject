package com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_entities;

import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "DND_ATTENDANCE_HISTORY")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

  @Column(name = "PLAYERS_NAMES_ATTENDED")
  private String players_names_attended;

  @Column(name = "PLAYERS_NAMES_EXCUSED")
  private String players_names_excused;

  @Column(name = "PLAYERS_NAMES_NO_SHOW_OR_NO_RESPONSE")
  private String players_names_no_show;

  @Column(name = "WEEK_OF_ATTENDANCE")
  private String week_of_attendance;

  public DNDAttendanceHistoryEntity(
    int players_attended,
    int players_excused,
    int players_no_show,
    String players_names_attended,
    String players_names_excused,
    String players_names_no_show,
    String week_of_attendance
  ) {
    this.players_attended = players_attended;
    this.players_excused = players_excused;
    this.players_no_show = players_no_show;
    this.players_names_attended = players_names_attended;
    this.players_names_excused = players_names_excused;
    this.players_names_no_show = players_names_no_show;
    this.week_of_attendance = week_of_attendance;
  }
}
