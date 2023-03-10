package com.crazy.scientist.crazyjavascientist.dnd.dnd_entities;

import java.time.OffsetDateTime;
import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "CURRENT_WEEK_OF")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CurrentWeekOfEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "CURRENT_WEEK")
  private String current_week;

  public CurrentWeekOfEntity(String current_week) {
    this.current_week = current_week;
  }
}
