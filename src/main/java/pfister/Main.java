package pfister;

import com.sun.javaws.exceptions.InvalidArgumentException;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;
import pfister.game.AdventureGame;
import pfister.game.Direction;
import pfister.input.Command;

public class Main {

  private static final Scanner scanner = new Scanner(System.in);

  /**
   * Prompts the user for input, then returns a Command object representing the command
   *
   * @return a command and its arguments
   */
  public static Command promptForInput() {
    System.out.print("> ");
    String readLine = "";
    while (readLine.isEmpty()) {
      readLine = scanner.nextLine();
    }
    String[] split = readLine.trim().split("\\s");
    String commandString = split[0];
    String argumentString = "";
    if (split.length > 1) {
      split[0] = "";
      argumentString = String.join(" ", split).trim();
    }
    return new Command(commandString.toLowerCase(), argumentString.toLowerCase());
  }

  public static void main(String[] args) throws IOException, InvalidArgumentException {
    AdventureGame game = new AdventureGame("src/main/resources/map.json");
    boolean quit = false;
    System.out.println("Smack Adventure 3000!");
    System.out.println("A game by Eric Pfister");
    System.out.println("Type 'help' to get a list of commands.");
    System.out.println("-----------------------------");
    System.out.println(game.examine());
    while (!quit) {
      Command currentCommand = promptForInput();
      String output;
      switch (currentCommand.command) {
        case "examine":
          output = game.examine();
          break;
        case "quit":
        case "exit":
          output = "Thanks for playing, goodbye!";
          quit = true;
          break;
        case "take":
          output = game.take(currentCommand.argument);
          break;
        case "drop":
          output = game.drop(currentCommand.argument);
          break;
        case "help":
          output = "Valid commands are: examine, take, drop, smack, go, help, and quit/exit.";
          break;
        case "go":
          Optional<Direction> direction = Direction.parseDirection(currentCommand.argument);
          if (!direction.isPresent()) {
            output = "Valid directions are: North, South, East, West.";
          } else {
            output = game.go(direction.get());
          }
          break;
        case "smack":
          output = game.smack();
          break;
        default:
          output = "I don't understand '" + currentCommand + "'.";
          break;
      }
      System.out.println();
      System.out.println(output);
      if (game.hasPlayerWon()) {
        System.out.println("\nCongratulations, you won!\nThanks for playing!");
        return;
      }
    }
  }
}
