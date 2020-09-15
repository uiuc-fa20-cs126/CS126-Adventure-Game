package pfister.game;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
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
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.Multigraph;

public class GameMap {
  private Graph<Room, RoomExit> roomGraph;

  private JsonArray readJsonMapFile(String jsonPath) throws IOException, InvalidArgumentException {
    FileReader reader = new FileReader(jsonPath);
    JsonArray rooms;
    try {
      rooms = (JsonArray) JsonParser.parseReader(reader);
    } catch (ClassCastException | JsonIOException | JsonSyntaxException e) {
      throw new InvalidArgumentException(
          new String[] {"Passed file cannot be parsed as a JSON file or the top level object is not an array."});
    }
    return rooms;
  }
  public GameMap(String jsonPath) throws IOException, InvalidArgumentException {
    roomGraph = new DirectedPseudograph<>(RoomExit.class);
    Gson gson = new Gson();
    JsonArray jsonRooms = readJsonMapFile(jsonPath);
    // Holds a mapping between each room and its potential ways of exiting the room, these will be
    // edges in the graph after the vertices (rooms) are added
    Map<Room, List<RoomExit>> roomExits = new HashMap<>();

    for (JsonElement jsonRoom : jsonRooms) {
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
      } catch (JsonSyntaxException|NullPointerException e) {
        throw new InvalidArgumentException(
            new String[] {
                "Element in rooms array cannot be parsed as a valid room: " + jsonRoom.toString()
            });
      }

      List<RoomExit> exitsFromRoom = new ArrayList<>();
      List<DirectionExit> dirExits;
      try {
        dirExits = Arrays.asList(gson.fromJson(jsonDirExits, DirectionExit[].class));
      }
      catch (JsonSyntaxException e) {
        throw new InvalidArgumentException(new String[]{
            "Direction exits in room '" + room.getRoomName() + "' cannot be parsed properly."});
      }
      if (dirExits.stream().map(e -> e.direction).distinct().count() < dirExits.size()) {
        throw new InvalidArgumentException(new String[]{
            "Directions within 'directionExits' are not unique for room '" + room.getRoomName()
                + "'"});
      }
      List<SmackExit> smackExits;
      try {
        smackExits = Arrays.asList(gson.fromJson(jsonSmackExits, SmackExit[].class));
      } catch (JsonSyntaxException e) {
        throw new InvalidArgumentException(new String[]{
            "Smack exits in room '" + room.getRoomName() + "' cannot be parsed properly."});
      }
      if (smackExits.stream().map(e -> e.itemUsed).distinct().count() < smackExits.size()) {
        throw new InvalidArgumentException(new String[]{
            "Items within 'smackExits' are not unique for room '" + room.getRoomName()
                + "'"});
      }

      exitsFromRoom.addAll(dirExits);
      exitsFromRoom.addAll(smackExits);
      roomExits.put(room, exitsFromRoom);

      roomGraph.addVertex(room);
    }
    for (Entry<Room, List<RoomExit>> exits : roomExits.entrySet()) {
      for (RoomExit roomExit : exits.getValue()) {
        // If the nextRoom value is an empty string, then this exit leads back to the room itself
        if (roomExit.getNextRoom().isEmpty()) {
          roomGraph.addEdge(exits.getKey(), exits.getKey(), roomExit);
          continue;
        }
        // Otherwise look for the next room in the vertex set, and create an edge between the two
        // rooms
        Optional<Room> nextRoom =
            roomGraph.vertexSet().stream()
                .filter(v -> v.getRoomName().equals(roomExit.getNextRoom()))
                .findFirst();
        if (!nextRoom.isPresent()) {
          throw new InvalidArgumentException(
              new String[] {
                "Error parsing JSON map. Room '"
                    + roomExit.getNextRoom()
                    + "' is not found in the game map."
              });
        }
        System.out.println(roomGraph.addEdge(exits.getKey(), nextRoom.get(), roomExit));
      }
    }
  }

  public Set<RoomExit> getExitsForRoom(Room r) {
    if (!roomGraph.vertexSet().contains(r)) return Collections.emptySet();
    return roomGraph.outgoingEdgesOf(r);
  }

  public Room getStartingRoom() {
    return roomGraph.vertexSet().stream().filter(r -> r.getRoomName().equals("StartingRoom")).findFirst().get();
  }
  public Optional<Room> getNextRoom(RoomExit e) {
    try {
      return Optional.of(roomGraph.getEdgeTarget(e));
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
  }
  public Set<DirectionExit> getDirectionExitsForRoom(Room r) {
    return getExitsForRoom(r).stream()
        .filter(v -> v instanceof DirectionExit)
        .map(v -> (DirectionExit)v)
        .collect(Collectors.toSet());
  }
  public Set<SmackExit> getSmackExitsForRoom(Room r) {
    return getExitsForRoom(r).stream()
        .filter(v -> v instanceof SmackExit)
        .map(v -> (SmackExit)v)
        .collect(Collectors.toSet());
  }
  public Optional<SmackExit> getDefaultSmackExitForRoom(Room r) {
    return getSmackExitsForRoom(r).stream().filter(v -> v.itemUsed.equalsIgnoreCase("default")).findFirst();
  }


}
