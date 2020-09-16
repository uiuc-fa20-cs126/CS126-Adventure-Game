package pfister.game;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.javaws.exceptions.InvalidArgumentException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import one.util.streamex.StreamEx;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedPseudograph;

public class GameMap {
  private final Graph<Room, RoomExit> roomGraph;

  /**
   * Loads the json file from the path, then constructs the GameMap
   * @param jsonPath the path to the JSON file
   * @throws IOException If the the json file cannot be read or does not exist at the passed
   *     location
   * @throws InvalidArgumentException If the json file cannot be validly parsed as a game map
   */
  public GameMap(String jsonPath) throws IOException, InvalidArgumentException {
    this(readJsonMapFile(jsonPath));
  }
  /**
   * Constructs the game map from a json file.
   * Creates a graph where the vertices are the rooms, and
   * the edges are the potential exits from those rooms.
   * Either from a "go" command (DirectionExit),or a "smack" command (SmackExit)
   * @param jsonRooms a JsonArray containing a list of rooms
   * @throws InvalidArgumentException If the json file cannot be validly parsed as a game map
   */
  public GameMap(JsonArray jsonRooms) throws InvalidArgumentException {
    roomGraph = new DirectedPseudograph<>(RoomExit.class);
    Map<Room, List<RoomExit>> roomToExitsMap = createRoomToExitsMap(jsonRooms);

    // Add all rooms to graph before iteration, so we can add edges between them
    roomToExitsMap.forEach((r, i) -> roomGraph.addVertex(r));
    // Load rooms and exits into graph
    for (Entry<Room, List<RoomExit>> exits : roomToExitsMap.entrySet()) {
      for (RoomExit roomExit : exits.getValue()) {
        // If the nextRoom value is an empty string, then this exit leads back to the room itself
        if (roomExit.getNextRoom().isEmpty()) {
          roomGraph.addEdge(exits.getKey(), exits.getKey(), roomExit);
          continue;
        }
        // Otherwise look for the next room in the vertex set, and create an edge between the two
        // rooms
        Optional<Room> nextRoom =
            StreamEx.of(roomGraph.vertexSet())
                .findFirst(v -> v.getRoomName().equals(roomExit.getNextRoom()));
        if (!nextRoom.isPresent()) {
          throw new InvalidArgumentException(
              new String[] {
                "Error parsing JSON map. Room '"
                    + roomExit.getNextRoom()
                    + "' is not found in the game map."
              });
        }
        roomGraph.addEdge(exits.getKey(), nextRoom.get(), roomExit);
      }
    }
  }

  /**
   * Loads a JSON file from a path, validates that it can be read, and parses it into a JSON array
   *
   * @param jsonPath a string containing the location of the json map file
   * @return a JsonArray containing the map rooms
   * @throws IOException If the path specified is not found, or could not be opened for reading
   * @throws InvalidArgumentException If the json file is not a valid json file, or does not have a
   *     top level array object
   */
  private static JsonArray readJsonMapFile(String jsonPath) throws IOException, InvalidArgumentException {
    FileReader reader = new FileReader(jsonPath);
    JsonArray rooms;
    try {
      rooms = (JsonArray) JsonParser.parseReader(reader);
    } catch (ClassCastException | JsonIOException | JsonSyntaxException e) {
      throw new InvalidArgumentException(
          new String[] {
            "Passed file cannot be parsed as a JSON file or the top level object is not an array."
          });
    }
    return rooms;
  }

  /**
   * Reads a JsonArray and attempts to parse into a Map of rooms and their exits
   *
   * @param jsonMap a JsonArray holding a list of rooms
   * @return a map between rooms and their exits
   * @throws InvalidArgumentException If the json cannot be parsed properly
   */
  private Map<Room, List<RoomExit>> createRoomToExitsMap(JsonArray jsonMap)
      throws InvalidArgumentException {
    Gson gson = new Gson();
    // Holds a mapping between each room and its potential ways of exiting the room, these will be
    // edges in the graph after the vertices (rooms) are added
    Map<Room, List<RoomExit>> roomToExitsMap = new HashMap<>();
    boolean startingRoomIsDefinedInJSON = false;
    // Read JSON into roomExits map
    for (JsonElement jsonRoom : jsonMap) {
      if (!jsonRoom.isJsonObject()) {
        throw new InvalidArgumentException(
            new String[] {
              "Element in rooms array cannot be parsed as a valid room: " + jsonRoom.toString()
            });
      }
      JsonObject roomObject = jsonRoom.getAsJsonObject();
      Room room;
      JsonArray jsonDirExits;
      JsonArray jsonSmackExits;
      try {
        room = gson.fromJson(jsonRoom, Room.class);
        jsonDirExits = roomObject.getAsJsonArray("directionExits");
        jsonSmackExits = roomObject.getAsJsonArray("smackExits");
      } catch (ClassCastException | JsonSyntaxException | NullPointerException e) {
        throw new InvalidArgumentException(
            new String[] {
              "Element in rooms array cannot be parsed as a valid room: " + jsonRoom.toString()
            });
      }
      if (room.getRoomName() == null || room.getItems() == null || room.getDescription() == null) {
        throw new InvalidArgumentException(
            new String[] {
              "Room object must have defined fields of : roomName, items, description. Room: "
                  + jsonRoom.toString()
            });
      }
      // Check for room name uniqueness
      if (StreamEx.of(roomToExitsMap.keySet())
          .findAny(v -> v.getRoomName().equals(room.getRoomName()))
          .isPresent()) {
        throw new InvalidArgumentException(
            new String[] {"Duplicate room name in JSON file of '" + room.getRoomName() + "'"});
      }

      List<RoomExit> exitsFromRoom = new ArrayList<>();
      List<DirectionExit> dirExits;
      try {
        dirExits = Arrays.asList(gson.fromJson(jsonDirExits, DirectionExit[].class));
        if (StreamEx.of(dirExits).findAny(d -> d.getDirection() == null).isPresent()) {
          throw new JsonSyntaxException("");
        }
      } catch (JsonSyntaxException e) {
        throw new InvalidArgumentException(
            new String[] {
              "Direction exits in room '" + room.getRoomName() + "' cannot be parsed properly."
            });
      }
      // Check for no duplicate direction exits
      if (StreamEx.of(dirExits).distinct(DirectionExit::getDirection).count() < dirExits.size()) {
        throw new InvalidArgumentException(
            new String[] {
              "Directions within 'directionExits' are not unique for room '"
                  + room.getRoomName()
                  + "'"
            });
      }

      List<SmackExit> smackExits;
      try {
        smackExits = Arrays.asList(gson.fromJson(jsonSmackExits, SmackExit[].class));
        if (StreamEx.of(smackExits).findAny(d -> d.getItemUsed() == null).isPresent()) {
          throw new JsonSyntaxException("");
        }
      } catch (JsonSyntaxException e) {
        throw new InvalidArgumentException(
            new String[] {
              "Smack exits in room '" + room.getRoomName() + "' cannot be parsed properly."
            });
      }
      // Check for no duplicate smack exits
      if (StreamEx.of(smackExits).distinct(SmackExit::getItemUsed).count() < smackExits.size()) {
        throw new InvalidArgumentException(
            new String[] {
              "Items within 'smackExits' are not unique for room '" + room.getRoomName() + "'"
            });
      }

      exitsFromRoom.addAll(dirExits);
      exitsFromRoom.addAll(smackExits);
      roomToExitsMap.put(room, exitsFromRoom);

      if (room.getRoomName().equals("StartingRoom")) {
        startingRoomIsDefinedInJSON = true;
      }
    }

    if (!startingRoomIsDefinedInJSON) {
      throw new InvalidArgumentException(
          new String[] {
            "The json map does not have a room titled 'StartingRoom', cannot create game map."
          });
    }
    return roomToExitsMap;
  }

  /**
   * Gets all potential ways a user can leave a room, either through the "go" command or the "smack"
   * command
   *
   * @param r the room to check
   * @return A set containing all the RoomExits for this room
   */
  public Set<RoomExit> getExitsForRoom(Room r) {
    if (!roomGraph.vertexSet().contains(r)) return Collections.emptySet();
    return roomGraph.outgoingEdgesOf(r);
  }

  /**
   * Gets the starting room of the map, where the player is placed when the game starts
   *
   * @return The starting room
   */
  public Room getStartingRoom() {
    return StreamEx.of(roomGraph.vertexSet())
        .findFirst(r -> r.getRoomName().equals("StartingRoom"))
        .get();
  }

  /**
   * Returns the target room for the RoomExit passed
   *
   * @param e a RoomExit leading to some specific room
   * @return An optional room value, empty if the target room could not be found in the graph
   */
  public Optional<Room> getNextRoom(RoomExit e) {
    try {
      return Optional.of(roomGraph.getEdgeTarget(e));
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
  }

  /**
   * Gets all the potential ways a user can leave the room by typing "go <direction>"
   *
   * @param r the room to check
   * @return A set of DirectionExits the user can use
   */
  public Set<DirectionExit> getDirectionExitsForRoom(Room r) {
    return StreamEx.of(getExitsForRoom(r)).select(DirectionExit.class).collect(Collectors.toSet());
  }
  /**
   * Gets all the potential ways a user can leave the room by typing "smack"
   *
   * @param r the room to check
   * @return A set of SmackExits the user can use
   */
  public Set<SmackExit> getSmackExitsForRoom(Room r) {
    return StreamEx.of(getExitsForRoom(r)).select(SmackExit.class).collect(Collectors.toSet());
  }

  /**
   * Gets the default smack exit for a room, this is defined in the json file as a smackExit with
   * "default" for the item field
   *
   * @param r the room to check
   * @return An optional SmackExit, empty if the room does not define a default smack interaction
   */
  public Optional<SmackExit> getDefaultSmackExitForRoom(Room r) {
    return getSmackExitsForRoom(r).stream()
        .filter(v -> v.getItemUsed().equalsIgnoreCase("default"))
        .findFirst();
  }
}
