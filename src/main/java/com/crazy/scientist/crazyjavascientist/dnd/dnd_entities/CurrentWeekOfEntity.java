package com.crazy.scientist.crazyjavascientist.dnd.dnd_entities;


import lombok.*;

import javax.persistence.*;
import java.time.OffsetDateTime;

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

    @Column(name = "EMBED_INSERT_TIME")
    private OffsetDateTime embed_insertion_time;

    public CurrentWeekOfEntity(String current_week) {
        this.current_week = current_week;
    }
}
