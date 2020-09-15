package pfister.game;

import com.sun.javaws.exceptions.InvalidArgumentException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class AdventureGame {
  private GameMap gameMap;
  private Room currentRoom;
  private Set<String> inventory;

  public AdventureGame(String jsonPath) throws IOException, InvalidArgumentException {
    gameMap = new GameMap(jsonPath);
    currentRoom = gameMap.getStartingRoom();
    inventory = new HashSet<>();
  }

  public String examine() {
    String examineString = this.currentRoom.getDescription();
    examineString += "\nItems: " + String.join(",",this.currentRoom.getItems());
    for (DirectionExit dirExit : gameMap.getDirectionExitsForRoom(currentRoom)) {
      examineString += "\nTo the " + dirExit.direction.toString() + ": " + dirExit.getDescription();
    }
    return examineString;
  }
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
  public String go(Direction direction) {
    Set<DirectionExit> directionExits = gameMap.getDirectionExitsForRoom(currentRoom);
    Optional<DirectionExit> exit = directionExits.stream().filter(v -> v.direction == direction).findFirst();
    if (!exit.isPresent()) {
      return "You cannot go " + direction + " from here.";
    }
    currentRoom = gameMap.getNextRoom(exit.get()).get();
    if (!exit.get().outcomeText.isEmpty()) {
      return exit.get().outcomeText + "\n\n" + examine();
    }
    else {
      return examine();
    }
  }
  public String smack() {
    Set<SmackExit> smackExits = gameMap.getSmackExitsForRoom(currentRoom);
    String usedItem = "";
    Optional<SmackExit> exit = Optional.empty();
    for (String item : inventory) {
      exit = smackExits.stream().filter(s -> s.itemUsed.equalsIgnoreCase(item)).findFirst();
      if (exit.isPresent()) {
        break;
      }
    }
    if (!exit.isPresent()) {
      exit = gameMap.getDefaultSmackExitForRoom(currentRoom);
      if (!exit.isPresent()) {
        return "You flail wildly and impressively. Nothing happens.";
      }
    }
    currentRoom = gameMap.getNextRoom(exit.get()).get();
    if (usedItem.isEmpty()) {
      return exit.get().getDescription() + "\n\n" + examine();
    }

    return exit.get().getDescription() + "\nThe " + usedItem + " breaks.\n\n" + examine();

  }
}
