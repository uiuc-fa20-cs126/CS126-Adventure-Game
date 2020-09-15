package pfister.game;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/** Holds information about the room a current player is in */
public class Room {

  private String roomName;
  private String description;
  private Set<String> items;

  public Room(String roomName, String description, Set<String> items) {
    this.roomName = roomName;
    this.description = description;
    this.items = new HashSet<>(items);
  }

  public String getRoomName() {
    return roomName;
  }

  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Set<String> getItems() {
    return Collections.unmodifiableSet(items);
  }

  /**
   * Checks to see whether an item is within this room
   * @param item the item to check for
   * @return boolean if the item is in this room or not
   */
  public boolean containsItem(String item) {
    return this.items.contains(item);
  }

  /**
   * Adds an item to this room
   *
   * @param item the item to add to the room
   * @return boolean whether or not the item was successfully added
   */
  public boolean addItem(String item) {
    return items.add(item);
  }
  /**
   * Removes an item in this room
   *
   * @param item the item to remove from the room
   * @return boolean false if the item was not in the room to begin with, otherwise true
   */
  public boolean removeItem(String item) {
    return items.remove(item);
  }
}
