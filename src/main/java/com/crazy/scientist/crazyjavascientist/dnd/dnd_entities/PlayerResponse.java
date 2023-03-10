package com.crazy.scientist.crazyjavascientist.dnd.dnd_entities;

import com.crazy.scientist.crazyjavascientist.dnd.enums.UnicodeResponses;
import lombok.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerResponse {

  private String player_name;
  private UnicodeResponses response_emoji_unicode;
}
