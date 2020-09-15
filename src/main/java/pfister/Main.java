package pfister;

import com.sun.javaws.exceptions.InvalidArgumentException;
import java.io.IOException;
import java.util.Optional;
import pfister.game.AdventureGame;
import pfister.game.Direction;
import pfister.input.Command;
import pfister.input.InputManager;

public class Main {
    public static void main(String[] args) throws IOException, InvalidArgumentException {
        // TODO: Run an Adventure game on the console
        AdventureGame game = new AdventureGame("src/main/resources/map.json");
        boolean quit = false;
        System.out.println("Smack Adventure 3000!");
        System.out.println("Type 'help' to get a list of commands.");
        System.out.println("-----------------------------");
        System.out.println(game.examine());
        while (!quit) {
            Command currentCommand = InputManager.promptForInput();
            String output = "";
            switch (currentCommand.command) {
                case "examine":
                    output = game.examine();
                    break;
                case "quit": case "exit":
                    output = "Thanks for playing, goodbye!";
                    quit = true;
                    break;
                case "take":
                    output = game.take(currentCommand.argument);
                    break;
                case "help":
                    output = "Valid commands are: examine, take, smack, go, help, and quit/exit.";
                    break;
                case "go":
                    Optional<Direction> direction = Direction.parseDirection(currentCommand.argument);
                    if (!direction.isPresent()) {
                        output = "Valid directions are: North, South, East, West.";
                    }
                    else {
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
        }

    }
}