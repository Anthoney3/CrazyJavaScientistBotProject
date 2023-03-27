package com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_entities;

import javax.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;

@Entity
@Slf4j
@Table(name = "DND_PLAYERS_INFO")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DNDPlayersEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "PLAYERS_NAME")
  private String player_name;

  @Column(name = "PLAYERS_NICKNAME")
  private String player_nickname;

  @Column(name = "DISCORD_USER_ID")
  private String discord_user_id;
}
