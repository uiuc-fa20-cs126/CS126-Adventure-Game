package pfister.game;

import com.sun.javaws.exceptions.InvalidArgumentException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import org.jgrapht.alg.util.Pair;

public class AdventureGame {
  private final GameMap gameMap;
  private final Set<String> inventory;
  private Room currentRoom;

  /**
   * Construct the AdventureGame object from a json file
   *
   * @param jsonPath the file location of the json file
   * @throws IOException If the json file cannot be read or is not present at that location
   * @throws InvalidArgumentException If the json file cannot be parsed as a valid game map
   */
  public AdventureGame(String jsonPath) throws IOException, InvalidArgumentException {
    gameMap = new GameMap(jsonPath);
    currentRoom = gameMap.getStartingRoom();
    inventory = new HashSet<>();
  }

  public Room getCurrentRoom() {
    return currentRoom;
  }

  public Set<String> getInventory() {
    return Collections.unmodifiableSet(inventory);
  }


  /**
   * Sets up a room before the player moves into it Currently removes any items that the player
   * already has in their inventory from the room
   *
   * @param r the room to set up
   * @return the room, after being set up
   */
  private Room setupRoom(Room r) {
    for (String item : inventory) {
      r.removeItem(item);
    }
    return r;
  }

  /**
   * Checks to see if the player has won by looking if they are in the "win" room
   *
   * @return a boolean indicating if the player has won the game
   */
  public boolean hasPlayerWon() {
    return this.currentRoom.getRoomName().equalsIgnoreCase("win");
  }

  /**
   * Examines the current room the player is in
   *
   * @return a string of information about the current room
   */
  public String examine() {
    String examineString = this.currentRoom.getDescription();
    if (hasPlayerWon()) {
      return examineString;
    }
    examineString += "\nInventory: " + String.join(",", this.inventory);
    examineString += "\nItems Visible: " + String.join(",", this.currentRoom.getItems());
    for (DirectionExit dirExit : gameMap.getDirectionExitsForRoom(currentRoom)) {
      examineString +=
          "\nTo the " + dirExit.getDirection().toString() + ": " + dirExit.getDescription();
    }
    return examineString;
  }

  /**
   * Attempt to take an item from the current room and place it in the user's inventory
   *
   * @param item the item to take from the current room
   * @return a string describing the interaction
   */
  public String take(String item) {
    if (item.isEmpty()) {
      return "Take what?";
    }
    if (!currentRoom.containsItem(item)) {
      return "There is no item '" + item + "' in this room.";
    }
    currentRoom.removeItem(item);
    inventory.add(item);
    return "You take the '" + item + "'.";
  }

  /**
   * Drops an item in a room
   *
   * @param item the item to drop
   * @return a string describing the interaction
   */
  public String drop(String item) {
    if (item.isEmpty()) {
      return "Drop what?";
    }
    if (!inventory.contains(item)) {
      return "There is no item '" + item + "' in your inventory.";
    }
    inventory.remove(item);
    currentRoom.addItem(item);
    return "You drop the '" + item + "'.";
  }

  /**
   * Attempt to move the player to another room using a DirectionExit, updates the currentRoom
   * variable upon success
   *
   * @param direction the direction to attempt to move the player
   * @return a string describing the interaction
   */
  public String go(Direction direction) {
    Set<DirectionExit> directionExits = gameMap.getDirectionExitsForRoom(currentRoom);
    Optional<DirectionExit> exit =
        StreamEx.of(directionExits).findFirst(e -> e.getDirection() == direction);

    if (!exit.isPresent()) {
      return "You cannot go " + direction + " from here.";
    }
    currentRoom = setupRoom(gameMap.getNextRoom(exit.get()).get());

    if (!exit.get().getOutcomeText().isEmpty()) {
      return exit.get().getOutcomeText() + "\n\n" + examine();
    }

    return examine();
  }

  /**
   * Attempt to move the player to another room using a SmackExit, updates the currentRoom variable
   * upon success Checks the user's inventory to determine which smack exit to take
   *
   * @return a string describing the interaction
   */
  public String smack() {
    Set<SmackExit> smackExits = gameMap.getSmackExitsForRoom(currentRoom);

    // Create a map between items in inventory to their SmackExit's, or Optional.empty() if they
    // don't have one
    Map<String, Optional<SmackExit>> itemToExitMap =
        StreamEx.of(inventory)
            .toMap(i -> StreamEx.of(smackExits).findFirst(e -> e.getItemUsed().equals(i)));

    // Filter out the items that do not have an associated SmackExit, then grab the first entry and
    // use that
    Optional<Pair<String, SmackExit>> itemAndExitUsed =
        EntryStream.of(itemToExitMap)
            .flatMapValues(StreamEx::of)
            .map(e -> Pair.of(e.getKey(), e.getValue()))
            .findFirst();

    // If we have no item smack exits defined for this room, use the default smack interaction if
    // defined
    if (!itemAndExitUsed.isPresent()) {
      Optional<SmackExit> exit = gameMap.getDefaultSmackExitForRoom(currentRoom);
      if (!exit.isPresent()) {
        return "You flail wildly and impressively. Nothing happens.";
      }
      itemAndExitUsed = Optional.of(Pair.of("", exit.get()));
    }
    String usedItem = itemAndExitUsed.get().getFirst();
    RoomExit exitUsed = itemAndExitUsed.get().getSecond();
    currentRoom = setupRoom(gameMap.getNextRoom(exitUsed).get());

    if (usedItem.isEmpty()) {
      return exitUsed.getDescription() + "\n\n" + examine();
    }

    inventory.remove(usedItem);
    return exitUsed.getDescription() + "\nThe " + usedItem + " breaks.\n\n" + examine();
  }
}
