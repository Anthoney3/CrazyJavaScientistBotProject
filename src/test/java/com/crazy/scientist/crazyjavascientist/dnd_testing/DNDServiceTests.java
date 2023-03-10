package com.crazy.scientist.crazyjavascientist.dnd_testing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class DNDServiceTests {

  @Test
  public void test_remove_button_works() {
    String test_string =
      "```Anthony \uD83D\uDE2D``````Nick  \uD83D\uDE2D``````Jared  \uD83D\uDE2D``````Gary  \uD83D\uDE2D``````Zach  \uD83D\uDE2D``````Ty  \uD83D\uDE2D```";

    List<String> split_string = new ArrayList<>(
      List.of(test_string.split("```"))
    );

    split_string.removeIf(string ->
      string.contains("Jared") || string.isEmpty() || string.contains("Anthony")
    );

    StringBuilder output_string = new StringBuilder();
    split_string.forEach(string ->
      output_string.append(String.format("```%s```", string))
    );

    assertEquals(
      "```Nick  \uD83D\uDE2D``````Gary  \uD83D\uDE2D``````Zach  \uD83D\uDE2D``````Ty  \uD83D\uDE2D```",
      output_string.toString()
    );
  }

  @Test
  public void test_spacing_for_attendance_response_embed() {
    String[] names = {
      "Anthony",
      "Zach",
      "Gary",
      "Jared",
      "Ty",
      "Thai",
      "Pat",
      "Nick",
      "Shane",
    };
    String emoji = "\uD83D\uDE2D";

    String column_one_format = "%-8.8s";
    String column_two_format = "%-12.12s";
    String formatInfo = column_one_format + " " + column_two_format;

    for (int i = 0; i < names.length; i++) {
      System.out.format(formatInfo, names[i], emoji);
      System.out.println();
    }
  }
}
