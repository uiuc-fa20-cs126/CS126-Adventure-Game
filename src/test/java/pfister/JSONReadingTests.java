package pfister;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.sun.javaws.exceptions.InvalidArgumentException;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.IO;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import pfister.game.GameMap;

public class JSONReadingTests {
  JsonArray jsonMap;

  @Before
  public void setup() throws FileNotFoundException, IllegalStateException {
    jsonMap = JsonParser.parseReader(new FileReader("src/main/resources/map.json")).getAsJsonArray();
  }

  @Rule
  public ExpectedException ex = ExpectedException.none();

  @Test
  public void testMapFileCanBeLoaded() throws InvalidArgumentException {
    GameMap g = new GameMap(jsonMap);
  }

  @Test()
  public void testMapWithRoomExitToUndefinedRoom() throws IOException, InvalidArgumentException {
    ex.expect(InvalidArgumentException.class);
    ex.expectMessage("not found in the game map");
    GameMap g = new GameMap("src/main/resources/test/testMapWithRoomExitToUndefinedRoom.json");
  }

  @Test
  public void testGarbageJsonArray() throws IOException, InvalidArgumentException {
    ex.expect(InvalidArgumentException.class);
    ex.expectMessage("Element in rooms array cannot be parsed");
    GameMap g = new GameMap("src/main/resources/test/testGarbageJsonArray.json");
  }
  @Test
  public void testNoStartingRoom() throws IOException,InvalidArgumentException {
    ex.expect(InvalidArgumentException.class);
    ex.expectMessage("StartingRoom");
    GameMap g = new GameMap("src/main/resources/test/testNoStartingRoom.json");
  }
  @Test
  public void testNoRequiredRoomFields() throws IOException,InvalidArgumentException {
    ex.expect(InvalidArgumentException.class);
    ex.expectMessage("defined fields of");
    GameMap g = new GameMap("src/main/resources/test/testNoRequiredRoomFields.json");
  }
  @Test
  public void testDuplicateRoomNames() throws IOException,InvalidArgumentException {
    ex.expect(InvalidArgumentException.class);
    ex.expectMessage("Duplicate");
    GameMap g = new GameMap("src/main/resources/test/testDuplicateRoomNames.json");
  }
  @Test
  public void testInvalidDirections() throws IOException,InvalidArgumentException {
    ex.expect(InvalidArgumentException.class);
    ex.expectMessage("Direction exits in room");
    GameMap g = new GameMap("src/main/resources/test/testInvalidDirections.json");
  }
  @Test
  public void testNoSmackItem() throws IOException,InvalidArgumentException {
    ex.expect(InvalidArgumentException.class);
    ex.expectMessage("Smack exits in room");
    GameMap g = new GameMap("src/main/resources/test/testNoSmackItem.json");
  }
  @Test
  public void testNonUniqueSmackItems() throws IOException,InvalidArgumentException {
    ex.expect(InvalidArgumentException.class);
    ex.expectMessage("Items within");
    GameMap g = new GameMap("src/main/resources/test/testNonUniqueSmackItems.json");
  }


}
