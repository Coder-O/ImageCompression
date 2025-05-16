package uk.ac.nulondon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Represents an image. Can find seams to compress the image without loosing much information.
 */
public class ImageGraph {
    /**
     * A representation of a single pixel.
     * Stores references to its neighbors (top, down, left, and right)
     */
    public static class Pixel {

        public Pixel rightNeighbor;
        public Pixel leftNeighbor;
        // The parent is the "topNeighbor"
        public Pixel parent;
        // The child is the "bottomNeighbor"
        public Pixel child;
        private int RGBValue;
        private double brightness;

        public Pixel() {
            this.RGBValue = 0;
            setBrightness();
        }

        public Pixel(int rgb) {
            this.RGBValue = rgb;
            setBrightness();
        }

        /**
         * Returns the color value of this Pixel
         * @return the color value of this Pixel.
         */
        public int getRGB() {
            return RGBValue;
        }

        /**
         * Sets the color value of this pixel.
         * @param rgb The integer representation of the color to be set.
         */
        public void setRGB(int rgb) {
            this.RGBValue = rgb;
            setBrightness();
        }

        /**
         * Returns the brightness of this pixel.
         * @return the brightness of this pixel.
         */
        public double getBrightness() {
            return brightness;
        }

        /**
         * Sets the brightness of this pixel based on its color
         */
        private void setBrightness() {
            Color color = new Color(RGBValue);
            brightness = ((double) (color.getRed() + color.getGreen() + color.getBlue())) / 3;
        }
    }

    /**
     * A node of a seam. A seam is represented by its first node, like a linked list.
     */
    public static class SeamNode {

        // The relationship between this node and the previous node in the graph.
        private PreviousSeamRelationship relationship;

        // The pixel stored in this node
        private Pixel pixel;

        // The previous node in the seam, which is above this node in the image. Similar to a linked list.
        private SeamNode previousNode;

        // The cost of this node and all of its previousNodes, as determined by an EnergyFormula.
        private final double cost;

        /**
         * Creates a new Seam Node
         * @param pixel The pixel to store in this node.
         * @param previousNode The previous node in the seam that this node should point to.
         * @param relationship The relationship between this node and the previous.
         * @param energyFormula The energyFormula that should be used to determine this node's cost.
         */
        public SeamNode(Pixel pixel, SeamNode previousNode, PreviousSeamRelationship relationship, EnergyFormula energyFormula) {
            this.pixel = pixel;
            this.previousNode = previousNode;
            this.relationship = relationship;
            this.cost = energyFormula.energyFormula(pixel) + previousNode.cost;
        }

        /**
         * Creates a new Seam Node
         * @param pixel The pixel to store in this node.
         * @param previousNode The previous node in the seam that this node should point to.
         * @param relationship The relationship between this node and the previous.
         * @param cost The cost of this node.
         */
        public SeamNode(Pixel pixel, SeamNode previousNode, PreviousSeamRelationship relationship, double cost) {
            this.pixel = pixel;
            this.previousNode = previousNode;
            this.relationship = relationship;
            this.cost = cost;
        }

        /**
         * Creates a new Seam Node. This constructor is used at the top row, when previousNode is null.
         * @param pixel The pixel to store in this node.
         * @param energyFormula The energyFormula that should be used to determine this node's cost.
         */
        public SeamNode(Pixel pixel, EnergyFormula energyFormula) {
            this.pixel = pixel;
            this.previousNode = null;
            // assign the previous seam relationship to STRAIGHT_UP
            // as it is the least complicated relationship
            this.relationship = PreviousSeamRelationship.STRAIGHT_UP;
            this.cost = energyFormula.energyFormula(pixel);
        }

        /**
         * Returns the pixel of this node.
         * @return The pixel of this node.
         */
        public Pixel getPixel() {
            return pixel;
        }

        /**
         * Returns the previous node of this node.
         * @return The previous node of this node.
         */
        public SeamNode getPreviousNode() {
            return previousNode;
        }

        /**
         * Sets the previous node of this node (the node of the row above it).
         * @param previousNode The node to be stored.
         */
        public void setPreviousNode(SeamNode previousNode) {
            this.previousNode = previousNode;
        }

        /**
         * Returns this node's relationship with the node above it (the previous node).
         * @return This node's relationship with the node above it (the previous node).
         */
        public PreviousSeamRelationship getRelationship() {
            return relationship;
        }

        /**
         * Gets the cost of this Seam, including all of its previous nodes.
         * @return The cost of this Seam.
         */
        public double getCost() {
            return cost;
        }

        /**
         * The relationship between a node and it's previous node, which lies in the row above it.
         */
        public enum PreviousSeamRelationship {

            /*
            | prev  |   X   |   X   |
            |   X   | this  |   X   |
            |   X   |   X   |   X   |
             */
            DIAGONAL_LEFT,

            /*
            |   X   |   X   | prev  |
            |   X   | this  |   X   |
            |   X   |   X   |   X   |
             */
            DIAGONAL_RIGHT,

            /*
            |   X   | prev  |   X   |
            |   X   | this  |   X   |
            |   X   |   X   |   X   |
             */
            STRAIGHT_UP,
        }
    }

    /**
     * An interface for energy formulas that find the 'energy' of a pixel in different ways.
     */
    private interface EnergyFormula {

        /**
         * Finds the energy of the given pixel.
         * @param currentPixel The pixel to find the energy of.
         * @return That pixel's 'energy'.
         */
        double energyFormula(Pixel currentPixel);
    }

    /**
     * An implementation of an Energy Formula that finds a given pixel's energy based on how blue it is.
     */
    public static class BlueEnergy implements EnergyFormula {

        /**
         * Gives the energy of the current pixel, based on how blue it is.
         * @param currentPixel The pixel to evaluate.
         * @return How blue that pixel is, as a double.
         *      The smaller the integer, the more blue the pixel is.
         */
        @Override
        public double energyFormula(Pixel currentPixel) {
            return 255 - new Color(currentPixel.getRGB()).getBlue();
        }
    }

    /**
     * An implementation of an Energy Formula that finds a given pixel's energy based on how the brightness of it and its neighbors.
     */
    public static class BrightnessEnergy implements EnergyFormula {

        /**
         * Gives the energy of the current pixel, based on the brightness of it and its neighbors.
         * @param currentPixel The pixel to evaluate.
         * @return How much "energy" that pixel has, as a double.
         */
        @Override
        public double energyFormula(Pixel currentPixel) {
            if (currentPixel == null) {
                return 0.0;
            }

            Pixel parent = currentPixel.parent;
            Pixel child = currentPixel.child;
            Pixel left = currentPixel.leftNeighbor;
            Pixel right = currentPixel.rightNeighbor;

            // Finds the diagonals using the parent and child.
            //  If the parent or child is null, then the currentPixel is at the top
            //  or bottom of the image, and the corresponding up or down diagonals are also null.
            Pixel upLeft = parent == null ? null : parent.leftNeighbor;
            Pixel upRight = parent == null ? null : parent.rightNeighbor;
            Pixel downLeft = child == null ? null : child.leftNeighbor;
            Pixel downRight = child == null ? null : child.rightNeighbor;

            /*
            Doubles A-I represent the brightness of each pixel, where E is the current node and
            the other pixels are E's neighbors as show below:

            A | B | C
            D | E | F
            G | H | I
            */
            double A = brightnessEnergyHelper(upLeft, currentPixel);
            double B = brightnessEnergyHelper(parent, currentPixel);
            double C = brightnessEnergyHelper(upRight, currentPixel);
            double D = brightnessEnergyHelper(left, currentPixel);
            double F = brightnessEnergyHelper(right, currentPixel);
            double G = brightnessEnergyHelper(downLeft, currentPixel);
            double H = brightnessEnergyHelper(child, currentPixel);
            double I = brightnessEnergyHelper(downRight, currentPixel);

            // The given equation
            double horizontalEnergy = A + 2 * D + G - (C + 2 * F + I);
            double verticalEnergy = A + 2 * B + C - (G + 2 * H + I);

            return Math.sqrt(
                    Math.pow(horizontalEnergy, 2) + Math.pow(verticalEnergy, 2)
            );
        }

        /**
         * Returns the brightness of a given neighbor.
         * If that neighbor is null, returns the currentPixel's brightness instead
         * @param neighbor The neighbor to find the brightness of.
         * @param currentPixel The current pixel to default back to if neighbor is null.
         * @return The brightness of neighbor if it is not null, otherwise the brightness of currentPixel.
         */
        private static double brightnessEnergyHelper(Pixel neighbor, Pixel currentPixel) {
            return neighbor == null ? currentPixel.getBrightness() : neighbor.getBrightness();
        }

    }

    // The height (in pixels) of the graph. This never changes.
    private final int height;

    // The width (in pixels) of the graph. This changes as seams are deleted and restored.
    private int width;

    // The topmost and leftmost pixel in the graph. Storing this is a method of storing the whole image.
    private Pixel head;

    /* The image graph is represented like so
                null                  null                  null      null
                 |                     |                     |        |
       null --- head       ––– head.rightNeighbor       --- ... --- a pixel ––– null
                 |                     |                     |        |
       null --- head.child --- head.child.rightNeighbor --- ... --- a pixel --- null
                 |                     |                     |        |
       null --- ...        ---        ...               --- ... ---  ...    --- null
                 |                     |                     |        |
       null --- a pixel    ---      a pixel             --- ... --- a pixel --- null
                 |                     |                     |        |
               null                  null                  null      null

    */

    /**
     * Constructs an ImageGraph representation of the given BufferedImage.
     * @param bufferedImage The BufferedImage to store.
     */
    public ImageGraph(BufferedImage bufferedImage) {
        head = new Pixel(bufferedImage.getRGB(0, 0));

        width = bufferedImage.getWidth();
        height = bufferedImage.getHeight();

        buildImage(bufferedImage);
    }

    /**
     * Helper function for the constructor. Builds the graph representation of the image.
     * @param bufferedImage The buffered image to store.
     */
    private void buildImage(BufferedImage bufferedImage) {
        // Initializing the iterating variables
        Pixel previousPixel = null;
        Pixel currentParent = null;
        Pixel currentPixel = head;
        Pixel firstPixelCurrentRow = currentPixel;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                // ----------------- Setting parent and parent's child -------------------- \\
                currentPixel.parent = currentParent;
                if (y != 0) {  // When current parent isn't null
                    currentParent.child = currentPixel;

                    // Iterating currentParent here to limit number of if statements.
                    currentParent = currentParent.rightNeighbor;
                }

                // Set the current pixel's child to null when at the last row
                if (y == (height - 1)) {
                    currentPixel.child = null;
                }

                // ---------------- Setting the left and right neighbors ------------------ \\
                currentPixel.leftNeighbor = previousPixel;

                if (x == (width - 1)) {
                    currentPixel.rightNeighbor = null;
                } else {
                    // Creating a new pixel to the right when not at last collumn
                    currentPixel.rightNeighbor = new Pixel(bufferedImage.getRGB(x + 1, y));
                }

                // Iterate to the next pixel in row
                // currentParent was already iterated
                previousPixel = currentPixel;
                currentPixel = currentPixel.rightNeighbor;
            }


            // Iterate to the next line, if there is a next line
            if (y != (height - 1)) {

                // The parent of the next row is the first pixel of this row.
                currentParent = firstPixelCurrentRow;

                // Creates the first pixel of the upcoming row, and stores it in firstPixelCurrentRow
                currentPixel = new Pixel(bufferedImage.getRGB(0, y + 1));
                firstPixelCurrentRow = currentPixel;

                // Resets previousPixel for the next row.
                previousPixel = null;
            }

        }
    }

    /**
     * Exports the saved picture as a png file
     * @param fileName The file name to save the image under
     */
    public void exportImage(String fileName) {
        // creating a new file
        File newFile = new File("src/main/" + fileName);

        // throw an exception if the width or height is not 0
        if (width == 0 || height == 0) {
            throw new IllegalStateException("Cannot save an empty image!!!");
        }

        // Creates a buffered image to save
        BufferedImage imageToSave = toBufferedImage();

        // Save to file and announce that it has been saved, or that it failed to save.
        try {
            ImageIO.write(imageToSave, "png", newFile);
            System.out.println(newFile.getName() + " saved successfully");
        } catch (IOException e) {
            System.out.println("Failed to export image");
            System.exit(0);
        }
    }

    /**
     * Creates a BufferedImage representation of this image.
     * @return said BufferedImage.
     */
    private BufferedImage toBufferedImage() {
        // Creates a new BufferedImage to save the image data to
        BufferedImage newBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Declaring/Initialising the iterators
        Pixel tempPixel = head;
        Pixel nextRowPixel;

        for (int y = 0; y < height; y++) {
            // Setting up the next row iteration.
            nextRowPixel = tempPixel.child;

            for (int x = 0; x < width; x++) {
                newBufferedImage.setRGB(x, y, tempPixel.getRGB());

                // Iterate to the next pixel in row
                tempPixel = tempPixel.rightNeighbor;
            }

            // Iterate to the next line
            tempPixel = nextRowPixel;
        }
        return newBufferedImage;
    }

    /**
     * Returns the width of the image.
     * @Return The width of the image, in pixels.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the head that contains the image.
     * @return The head that contains the image.
     */
    public Pixel getHead() {
        return head;
    }

    /**
     * Finds the lowest energy seam that divides the image vertically.
     * @param energyFormula The formula to determine the cost of a seam.
     * @return The seam with the least cost as defined by that formula.
     */
    public SeamNode findSeam(EnergyFormula energyFormula) {
        // For every pixel, find the cheapest cost of its upper neighbors and update the pointers accordingly

        // The seams from the previous row
        ArrayList<SeamNode> previousSeams = new ArrayList<>();

        // The seams for the current row
        ArrayList<SeamNode> currentSeams = new ArrayList<>();

        // Setting up the iteration
        Pixel currentPixel = head;
        Pixel nextRowPixel = head.child;

        // For every pixel in the first row, create a new SeamNode()
        for (int col = 0; col < width; col++) {
            previousSeams.add(new SeamNode(currentPixel, energyFormula));
            currentPixel = currentPixel.rightNeighbor;
        }

        // For all rows after the first
        for (int row = 1; row < height; row++) {

            // iteration
            currentPixel = nextRowPixel;
            nextRowPixel = currentPixel.child;

            for (int col = 0; col < width; col++) {

                // The currently best seam to reach the pixel. The one right above the current node by default.
                SeamNode bestSeam = previousSeams.get(col);
                double bestValue = bestSeam.getCost();
                SeamNode.PreviousSeamRelationship relationship = SeamNode.PreviousSeamRelationship.STRAIGHT_UP;


                // Check if the top left and top right have better seams
                if (col > 0 && previousSeams.get(col - 1).getCost() < bestValue) {
                    bestSeam = previousSeams.get(col - 1);
                    bestValue = bestSeam.getCost();
                    relationship = SeamNode.PreviousSeamRelationship.DIAGONAL_LEFT;
                }
                if (col < width - 1 && previousSeams.get(col + 1).getCost() < bestValue) {
                    bestSeam = previousSeams.get(col + 1);
                    bestValue = previousSeams.get(col + 1).getCost();
                    relationship = SeamNode.PreviousSeamRelationship.DIAGONAL_RIGHT;
                }

                // Adds the newly formed seam to the list of current seams
                currentSeams.add(new SeamNode(currentPixel, bestSeam, relationship, energyFormula));

                // Iterate
                currentPixel = currentPixel.rightNeighbor;
            }
            previousSeams = currentSeams;
            currentSeams = new ArrayList<>();

        }


        // The bestSeam so far is the first seam in the arraylist
        SeamNode bestSeam = previousSeams.getFirst();


        // For every seam in the seam node, compare the cost of the current seam to that of the best
        // seam to determine which seam has the lowest cost. If so, update the best seam
        // and return it at the end of the loop.
        for (SeamNode seam : previousSeams) {
            if (seam.cost < bestSeam.cost) {
                bestSeam = seam;
            }
        }

        return bestSeam;
    }

    /**
     * Highlights the given seam with a supplied color.
     * Does not actually change the given seam, but replaces it with a new seam of a single color of pixels.
     * @param seam The seam to be highlighted.
     * @param color The color to assign each node in the seam to.
     * @return The new, highlighted seam.
     */
    public SeamNode highlightSeam(SeamNode seam, Color color) {
        // Handling two seams: the currentSeam, which is the old seam being replaced
        //   and the currentNewSeam, which is the seam containing all the new pixels of the given color.

        // Stores the current node of the original seam
        SeamNode currentSeam = seam;

        // Stores the current node of the new, single-color seam.
        SeamNode currentNewSeam = new SeamNode(new Pixel(color.getRGB()), null, currentSeam.getRelationship(), currentSeam.getCost());

        // Stores the last created node of the new seam
        SeamNode previousNewSeamNode = null;

        // Stores the first node of the new seam, so we can reference this seam later
        // (like the root of a linked list; it is at the bottom of the image)
        SeamNode firstSeamNode = currentNewSeam;

        // This traverses the original seam from the bottom to the top
        while (currentSeam != null) {
            if (previousNewSeamNode != null) {
                // For all nodes except the topmost
                previousNewSeamNode.setPreviousNode(currentNewSeam);
            }

            // Variables that store the current pixel of the original seam and the new seam
            Pixel currentPixel = currentSeam.getPixel();
            Pixel currentNewPixel = currentNewSeam.getPixel();

            // Updating pointers so that the pixels in the graph point to the currentNewPixel, rather than the old pixel.
            // It is possible and necessary to update a neighbor's pointer only when the neighbor is not null,
            //    so we check if each neighbor is null before continuing
            if (currentPixel.rightNeighbor != null) {
                currentPixel.rightNeighbor.leftNeighbor = currentNewPixel;
            }
            if (currentPixel.leftNeighbor != null) {
                currentPixel.leftNeighbor.rightNeighbor = currentNewPixel;
            }
            if (currentPixel.parent != null) {
                currentPixel.parent.child = currentNewPixel;
            }
            if (currentPixel.child != null) {
                currentPixel.child.parent = currentNewPixel;
            }

            // Make sure currentNewPixel points to its neighbors
            currentNewPixel.rightNeighbor = currentPixel.rightNeighbor;
            currentNewPixel.leftNeighbor = currentPixel.leftNeighbor;
            currentNewPixel.parent = currentPixel.parent;
            currentNewPixel.child = currentPixel.child;

            // If the head is replaced, updates the head pointer accordingly.
            if (head.equals(currentPixel)) {
                head = currentNewPixel;
            }

            /// ----- Iterating and setting up the next run of the loop ----- \\\

            // previous new seamNode will be the old current seamNode
            previousNewSeamNode = currentNewSeam;
            currentSeam = currentSeam.getPreviousNode();
            if (currentSeam != null) {
                // If it is not the last iteration
                currentNewSeam = new SeamNode(new Pixel(color.getRGB()), null, currentSeam.getRelationship(), currentSeam.getCost());
            }
        }

        return firstSeamNode;
    }

    /**
     * Remove a seam from the graph by removing all pointers to its pixels within the graph.
     * @param seam The seam to remove.
     * @return The removed seam. This is just the parameter, and should be unedited.
     */
    public SeamNode removeSeam(SeamNode seam) {
        SeamNode currentSeam = seam;

        while (currentSeam != null) {
            // Iterate
            Pixel currentPixel = currentSeam.getPixel();

            // If we are removing the head, shift the head pointer accordingly
            if (head.equals(currentPixel)) {
                head = currentPixel.rightNeighbor;
            }

            // If they exist, set the left/right neighbor's pointers to not point to this pixel
            if (currentPixel.leftNeighbor != null) {
                currentPixel.leftNeighbor.rightNeighbor = currentPixel.rightNeighbor;
            }
            if (currentPixel.rightNeighbor != null) {
                currentPixel.rightNeighbor.leftNeighbor = currentPixel.leftNeighbor;
            }

            // Updating the parent relationships to remove the seam
            switch (currentSeam.getRelationship()) {
                case DIAGONAL_LEFT -> {
                    // Should only be diagonal if the diagonal exists. No need to check for null.
                    currentPixel.parent.child = currentPixel.leftNeighbor;
                    currentPixel.leftNeighbor.parent = currentPixel.parent;
                }
                case DIAGONAL_RIGHT -> {
                    // Should only be diagonal if the diagonal exists. No need to check for null.
                    currentPixel.parent.child = currentPixel.rightNeighbor;
                    currentPixel.rightNeighbor.parent = currentPixel.parent;
                }
                case STRAIGHT_UP -> {
                    // Don't need to change parent and child relationship
                    // amongst nodes that are both being removed.
                }
            }
            currentSeam = currentSeam.getPreviousNode();
        }

        width--;
        return seam;

    }

    /**
     * Inserts a given seam into the image.
     * May replace nodes that replaced the original seam, or may insert the seam where it was cut out.
     * Intended for use with the undo functionality.
     * @param seam The seam to insert into the image.
     * @param affectedWidth Whether the insertion affected the width of the image.
     * @return The seam that was inserted. Should be the same as the parameter, and be unedited.
     */
    public SeamNode insertSeam(SeamNode seam, boolean affectedWidth) {
        SeamNode currentSeam = seam;

        while (currentSeam != null) {
            Pixel currentPixel = currentSeam.getPixel();

            // If they exist, set the left/right neighbors' pointers back to the currentPixel.
            if (currentPixel.leftNeighbor != null) {
                currentPixel.leftNeighbor.rightNeighbor = currentPixel;
            }
            if (currentPixel.rightNeighbor != null) {
                currentPixel.rightNeighbor.leftNeighbor = currentPixel;
            }

            // Handling parent/child relationships
            switch (currentSeam.getRelationship()) {
                case DIAGONAL_LEFT -> {
                    // Should only be diagonal if the diagonal exists. No need to check for null.
                    currentPixel.parent.child = currentPixel;
                    currentPixel.leftNeighbor.parent = currentSeam.previousNode.getPixel();
                }
                case DIAGONAL_RIGHT -> {
                    // Should only be diagonal if the diagonal exists. No need to check for null.
                    currentPixel.parent.child = currentPixel;
                    currentPixel.rightNeighbor.parent = currentSeam.previousNode.getPixel();
                }
                case STRAIGHT_UP -> {
                    // The only case in which the parent might be null
                    // (the first row is always a STRAIGHT_UP relationship)
                    if (currentPixel.parent != null) {
                        currentPixel.parent.child = currentPixel;
                    } else {
                        if (currentPixel.leftNeighbor == null) {
                            // If we reinsert at the head (which is the only node with a STRAIGHT_UP relationship,
                            // a null parent, and a null left neighbor), we set the head accordingly.
                            head = currentPixel;
                        }
                    }
                }
            }
            currentSeam = currentSeam.getPreviousNode();
        }

        if (affectedWidth) {
            width++;
        }
        return seam;
    }
}
