package pfister;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.sun.javaws.exceptions.InvalidArgumentException;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import pfister.game.AdventureGame;
import pfister.game.Direction;

public class AdventureTest {
  AdventureGame game;

  @Before
  public void setUp() throws IOException, InvalidArgumentException {
    game = new AdventureGame("src/main/resources/map.json");
  }

  @Test
  public void testExaminePromptIsCorrect() {
    String examineString = game.examine();
    assertTrue(examineString.contains("\nInventory:"));
    assertTrue(examineString.contains("\nItems Visible:"));
  }

  @Test
  public void testPickingUpAndDroppingItem() {
    assertEquals(String.join(",", game.getInventory()), "");
    assertEquals(String.join(",", game.getCurrentRoom().getItems()), "baseball bat");
    game.take("baseball bat");

    assertEquals(String.join(",", game.getInventory()), "baseball bat");
    assertEquals(String.join(",", game.getCurrentRoom().getItems()), "");
    game.drop("baseball bat");

    assertEquals(String.join(",", game.getInventory()), "");
    assertEquals(String.join(",", game.getCurrentRoom().getItems()), "baseball bat");
  }

  @Test
  public void testGoingThroughDirectionExit() {
    assertEquals(game.getCurrentRoom().getRoomName(), "StartingRoom");
    game.go(Direction.East);
    assertEquals(game.getCurrentRoom().getRoomName(), "Hallway2Men");
    game.go(Direction.West);
    assertEquals(game.getCurrentRoom().getRoomName(), "StartingRoom");
  }

  @Test
  public void testGoingThroughSmackExit() {
    assertEquals(game.getCurrentRoom().getRoomName(), "StartingRoom");
    game.take("baseball bat");
    game.go(Direction.East);
    game.smack();
    assertEquals(game.getCurrentRoom().getRoomName(), "Hallway1Man");
    game.smack();
    assertEquals(game.getCurrentRoom().getRoomName(), "Cafeteria");
  }

  @Test
  public void testSmackBreaksItem() {
    game.take("baseball bat");
    assertEquals(String.join(",", game.getInventory()), "baseball bat");
    game.go(Direction.East);
    String smackString = game.smack();
    assertEquals(String.join(",", game.getInventory()), "");
    assertTrue(smackString.contains("The baseball bat breaks"));
  }
}
