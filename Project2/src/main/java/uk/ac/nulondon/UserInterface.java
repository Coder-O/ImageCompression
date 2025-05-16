package uk.ac.nulondon;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * A user interface class for interacting with an image to perform various operations
 * such as highlighting the bluest seam, removing the bluest seam, highlighting the lowest energy seam,
 * removing the lowest energy seam, undoing removals, and saving modified images.
 * <p>
 * It provides a command-line interface for users to input commands and see the effects
 * of their operations on the image.
 * <p>
 * The class handles loading an image from a specified file path, displaying menu options to the user,
 * and processing user inputs to manipulate the image accordingly. It also manages saving the
 * state of the image after each operation to a specified directory.
 */
public class UserInterface {

    // Variable to keep track of whether the program is running. Is true until the user quits.
    private boolean keepRunning;

    // Scanner to read the user's input from the console
    private Scanner scanner;

    // Stores the imageGraph and handles an instance of imageGraph
    private ImageHandler imageHandler;

    /**
     * Initializes the scanner, prompts the user for their input of the file path
     * <p>
     * Validates their input and prompts the user again if the input is invalid
     */
    public UserInterface() {
        scanner = new Scanner(System.in);

        // Tracks whether a valid file path has been obtained
        boolean gettingFilePath = true;

        System.out.println("Welcome! Please enter a file path.");
        while (gettingFilePath) {
            String filePath = getUserInput();
            try {
                imageHandler = new ImageHandler(filePath);
                gettingFilePath = false;
            } catch (IOException e) {
                System.out.println("Invalid path. Please enter the exact path to the file.");
                System.out.println("For example, to use beach.png, enter src/main/resources/beach.png");
            }
        }

        keepRunning = true;
    }

    /**
     * Starts the user interaction session, allowing the user to perform various operations on the image.
     * It displays a menu of options, reads user input, and performs actions such as removing seams
     * or undoing removals based on the user's choices. The session continues until the user chooses to quit and
     * thereafter, the current image is saved.
     */
    public void start() {
        System.out.println("Welcome to Image Compression!");
        while (keepRunning) {
            printMenuOptions();
            String userInput = getUserInput();
            interpretUserInput(userInput);
        }
        scanner.close();
    }

    /**
     * Prints the menu options to the console, guiding the user on
     * how to interact with the application.
     */
    private void printMenuOptions() {
        System.out.println("Please choose an option");
        System.out.println("b - Highlight the bluest seam");
        System.out.println("e - Highlight the lowest energy seam");
        // As long as the width of the image is greater than 1 and the image is currently highlighted,
        // it is a valid option to delete a seam from the image
        if (imageHandler.getImageGraph().getWidth() > 1 && imageHandler.isHighlighted()) {
            System.out.println("d - Remove the seam from the image");
        }
        // Only displays the undo option if it is currently a valid choice (if there are edits to undo).
        if (imageHandler.getEditHistorySize() > 0) {
            System.out.println("u - Undo previous edit");
        }
        System.out.println("q - Quit");
    }

    /**
     * Gets the user input, checking if they entered a valid menu option, an empty string if not.
     * @return The user's input
     */
    private String getUserInput() {
        String userInput = "";

        // get the user's input
        try {
            userInput = scanner.nextLine().toLowerCase();
        } catch (InputMismatchException e) {
            // if user enters anything except a menu option
            // this is handled in interpretUserInput
        }

        return userInput;
    }

    /**
     * Interprets the user input and enacts a command if it was a valid selection.
     * Prints a relevant response to the user as well.
     * @param selection the value of the user's choice
     * @return True if the selection was a valid choice, false otherwise.
     */
    public boolean interpretUserInput(String selection) {
        selection = selection.toLowerCase();

        // if the image is highlighted and the user did not indicate they want to delete a seam,
        // undo or remove the highlight and let the user know
        if (imageHandler.isHighlighted() && !selection.equals("d")) {
            imageHandler.undo();
            System.out.println("Highlight removed.");
        }

        switch (selection) {
            case "b" -> {
                imageHandler.highlightBluestSeam();
                System.out.println("Ready to remove the bluest seam, as highlighted. Type 'd' to confirm, any other letter to cancel.");
                System.out.println();
            }
            case "e" -> {
                imageHandler.highlightLowestEnergySeam();
                System.out.println("Ready to remove the lowest energy seam, as highlighted. Type 'd' to confirm, any other letter to cancel.");
                System.out.println();
            }
            case "d" -> {
                // if there is only one seam left in the image, cannot delete it
                if (imageHandler.getImageGraph().getWidth() <= 1) {
                    System.out.println("We cannot remove the last seam in the image. Please either undo or quit.");
                    System.out.println();
                }
                // if the seam is not highlighted, the user cannot delete it
                else if (!imageHandler.isHighlighted()) {
                    System.out.println("There is no highlighted seam to remove. Please highlight a seam first.");
                    System.out.println();
                } else {
                    imageHandler.deleteHighlightedSeam();
                    System.out.println("Seam removed.");
                    System.out.println();
                }
            }
            case "u" -> {
                // If there are no edits left
                if (imageHandler.getEditHistorySize() <= 0) {
                    System.out.println("There are no edits to undo! Please try a different command.");
                    System.out.println();
                } else {
                    imageHandler.undo();
                    System.out.println("Last edit restored");
                    System.out.println();
                }
            }
            // if the user wants to quit
            case "q" -> {
                System.out.println("Quitting...");
                imageHandler.finalOutput();
                keepRunning = false;
            }
            // If the user entered none of the valid options
            default -> {
                System.out.println("That is not a valid option. " +
                        "Selections must be one of the singular characters listed.");
                System.out.println();
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        UserInterface UI = new UserInterface();
        UI.start();
    }
}

