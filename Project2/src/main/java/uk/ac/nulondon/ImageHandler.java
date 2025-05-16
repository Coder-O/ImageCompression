package uk.ac.nulondon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

/**
 * Handles an instance of ImageGraph, which stores an image.
 * Can edit the image and undo previous edits.
 * Also saves the image whenever an edit was made.
 */
public class ImageHandler {
    /**
     * A class to represent a single edit applied to imageGraph.
     * It contains all necessary information to undo itself.
     */
    private abstract class Command {

        // The seam which used to be in the graph
        protected ImageGraph.SeamNode editedSeam;

        // Variable that keeps track of whether the width of the image is changed as a result
        // of a deletion or an undo of a deletion
        boolean affectedWidth;

        public Command() {
        }

        /**
         * Undoes the edit this command enacted upon the imageGraph
         */
        public void undo() {
            imageGraph.insertSeam(editedSeam, affectedWidth);
        }

        public ImageGraph.SeamNode getEditedSeam() {
            return editedSeam;
        }
    }

    /**
     * A specific type of command that highlights a seam.
     */
    private abstract class HighlightCommand extends Command {
        // The seam that contains all the new, highlighted pixels
        // This is stored so that it can be easily removed later.
        ImageGraph.SeamNode highlightedSeam;

        /**
         * Highlights a seam and sets the affected width to false because
         * the width will not be altered.
         */
        public HighlightCommand() {
            super();
            affectedWidth = false;
        }
    }

    /**
     * A command to find the bluest seam in the image and highlight it blue.
     */
    private class HighlightBluestSeam extends HighlightCommand {
        /**
         * Finds the bluest seam in the image and highlights it blue.
         */
        public HighlightBluestSeam() {
            super();
            editedSeam = imageGraph.findSeam(new ImageGraph.BlueEnergy());
            highlightedSeam = imageGraph.highlightSeam(editedSeam, Color.BLUE);
        }
    }

    /**
     * A command to find the lowest energy seam in the image and highlight it red.
     */
    private class HighlightLowestEnergySeam extends HighlightCommand {
        /**
         * Finds the lowest energy seam in the image and highlights it red.
         */
        public HighlightLowestEnergySeam() {
            super();
            editedSeam = imageGraph.findSeam(new ImageGraph.BrightnessEnergy());
            highlightedSeam = imageGraph.highlightSeam(editedSeam, Color.RED);
        }
    }

    /**
     * A command to remove a given seam from the image.
     */
    private class DeleteSeam extends Command {
        /**
         * Removes the given seam from the image.
         * @param seam The seam to remove.
         */
        public DeleteSeam(ImageGraph.SeamNode seam) {
            affectedWidth = true;
            editedSeam = imageGraph.removeSeam(seam);
        }

        /**
         * For use with highlight deletion. Removes the given seam from the image.
         * Stores the original seam that the highlight replaced.
         * @param seam The seam to remove. Most likely a highlighted seam.
         * @param originalSeam The seam that was originally replaced, likely by a highlight command.
         */
        public DeleteSeam(ImageGraph.SeamNode seam, ImageGraph.SeamNode originalSeam) {
            affectedWidth = true;
            editedSeam = originalSeam;
            imageGraph.removeSeam(seam);
        }
    }

    // The image representation
    private final ImageGraph imageGraph;

    // Tracks the number of images created for file naming purposes
    private int tempImageCounter;

    // The edit history
    private Stack<Command> commandHistory;

    // Whether the image is currently highlighted
    private boolean isHighlighted;

    /**
     * Creates a new instance of ImageHandler, to handle the ImageGraph created from the given file.
     * @param filePath The path to the file, such as 'src/main/resources/beach.png'
     * @throws IOException If the original image cannot be read.
     */
    public ImageHandler(String filePath) throws IOException {

        File originalFile = new File(filePath);
        BufferedImage oldImg = ImageIO.read(originalFile);
        imageGraph = new ImageGraph(oldImg);

        tempImageCounter = 1;

        commandHistory = new Stack<>();
        isHighlighted = false;
    }

    /**
     * Returns the number of edits saved in the command history.
     * @return The number of edits saved in the command history.
     */
    public int getEditHistorySize() {
        return commandHistory.size();
    }

    /**
     * Returns the image representation.
     * @return The image representation.
     */
    public ImageGraph getImageGraph() {
        return imageGraph;
    }

    /**
     * Returns whether the image is currently highlighted or not.
     * @return Whether the image is currently highlighted or not.
     */
    public boolean isHighlighted() {
        return isHighlighted;
    }

    /**
     * Highlights the bluest seam in the image and adds the edit to the command history.
     */
    public void highlightBluestSeam() {
        commandHistory.add(new HighlightBluestSeam());
        isHighlighted = true;
        saveImage();
    }

    /**
     * Highlights the lowest energy seam in the image and adds the edit to the command history.
     */
    public void highlightLowestEnergySeam() {
        commandHistory.add(new HighlightLowestEnergySeam());
        isHighlighted = true;
        saveImage();
    }

    /**
     * Deletes the highlighted seem and adds a deletion command to the command history.
     * @throws IllegalStateException When the previous edit was not a highlight.
     *      In this case, the state of the program is not altered in any way.
     */
    public void deleteHighlightedSeam() throws IllegalStateException {
        if (isHighlighted) {
            // When the highlighted portion is deleted, the highlighting command in the command history is destroyed.
            // So, when undoing, the image will be reset back to before the highlight.
            HighlightCommand prevHighlight = (HighlightCommand) commandHistory.pop();
            commandHistory.add(new DeleteSeam(prevHighlight.highlightedSeam, prevHighlight.getEditedSeam()));
            isHighlighted = false;
            saveImage();
        } else {
            throw new IllegalStateException("The previous edit was not a highlight and was not deleted.");
        }
    }

    /**
     * Undoes the previous edit.
     * For the purpose of undoing, highlighting a seam and then deleting it are
     * counted as one edit, and are both undone together.
     */
    public void undo() {
        if (commandHistory.isEmpty()) {
            throw new IllegalStateException("There are no edits to undo!!!");
        }
        Command undoingCommand = commandHistory.pop();
        undoingCommand.undo();
        isHighlighted = false;
        saveImage();
    }

    /**
     * Saves the image stored in imageGraph
     */
    public void saveImage() {
        imageGraph.exportImage("tempIMG_%d.png".formatted(tempImageCounter));
        tempImageCounter++;
    }

    /**
     * Saves the final image of the program
     */
    public void finalOutput() {
        imageGraph.exportImage("newImg.png");
    }
}
