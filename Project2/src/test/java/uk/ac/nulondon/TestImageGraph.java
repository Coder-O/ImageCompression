package uk.ac.nulondon;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TestImageGraph {

    File originalFile;
    BufferedImage oldImg;
    public TestImageGraph() throws IOException {
        // file path to get the file

        // Should these three lines below be outside all the testing methods?
        originalFile = new File("src/main/resources/beach.png");
        oldImg = ImageIO.read(originalFile);
    }

    /**
     * Helper function that creates an image graph where every pixel is a different color.
     * @return The built image graph.
     */
    private ImageGraph makeImageGraph() {
        BufferedImage bufferedImage = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);

        // Row 1                                                  //   R,   G,   B
        bufferedImage.setRGB(0,0, Color.RED.getRGB());      // 255,   0,   0
        bufferedImage.setRGB(1,0, Color.ORANGE.getRGB());   // 255, 200,   0
        bufferedImage.setRGB(2,0, Color.YELLOW.getRGB());   // 255, 255,   0

        // Row 2
        bufferedImage.setRGB(0,1, Color.GREEN.getRGB());    //   0, 255,   0
        bufferedImage.setRGB(1,1, Color.BLACK.getRGB());    //   0,   0,   0
        bufferedImage.setRGB(2,1, Color.BLUE.getRGB());     //   0,   0, 255

        // Row 3
        bufferedImage.setRGB(0,2, Color.MAGENTA.getRGB());  // 255,   0, 255
        bufferedImage.setRGB(1,2, Color.PINK.getRGB());     // 255, 175, 175
        bufferedImage.setRGB(2,2, Color.CYAN.getRGB());     //   0, 255, 255

        return new ImageGraph(bufferedImage);
    }

    // -------- Pixel Tests -------- \\
    @Test
    void testGetRGB(){
        ImageGraph.Pixel myPixel = new ImageGraph.Pixel();
        ImageGraph.Pixel myPixel2 = new ImageGraph.Pixel(200);
        Assertions.assertThat(myPixel.getRGB()).isEqualTo(0);
        Assertions.assertThat(myPixel2.getRGB()).isEqualTo(200);
    }

    @Test
    void testSetRGB(){
        ImageGraph.Pixel myPixel = new ImageGraph.Pixel();
        myPixel.setRGB(40);
        Assertions.assertThat(myPixel.getRGB()).isEqualTo(40);
    }

    @Test
    void testGetBrightness(){
        ImageGraph.Pixel myPixel1 = new ImageGraph.Pixel(Color.WHITE.getRGB());
        ImageGraph.Pixel myPixel2 = new ImageGraph.Pixel(Color.BLACK.getRGB());
        ImageGraph.Pixel myPixel3 = new ImageGraph.Pixel(Color.GRAY.getRGB());


        Assertions.assertThat(
                myPixel1.getBrightness()
        ).isEqualTo(
                (Color.WHITE.getRed() + Color.WHITE.getGreen() + Color.WHITE.getBlue()) / 3.0
        );
        Assertions.assertThat(
                myPixel2.getBrightness()
        ).isEqualTo(
                (Color.BLACK.getRed() + Color.BLACK.getGreen() + Color.BLACK.getBlue()) / 3.0
        );
        Assertions.assertThat(
                myPixel3.getBrightness()
        ).isEqualTo(
                (Color.GRAY.getRed() + Color.GRAY.getGreen() + Color.GRAY.getBlue()) / 3.0
        );

    }
    // ----------------------------- \\

    @Test
    void testSeamNode() {
        ImageGraph.Pixel myPixel1 = new ImageGraph.Pixel(Color.BLUE.getRGB());
        ImageGraph.Pixel myPixel2 = new ImageGraph.Pixel(Color.GREEN.getRGB());
        ImageGraph.Pixel myPixel3 = new ImageGraph.Pixel(Color.RED.getRGB());

        ImageGraph.SeamNode seamNode1 = new ImageGraph.SeamNode(myPixel1, new ImageGraph.BlueEnergy());
        ImageGraph.SeamNode seamNode2 = new ImageGraph.SeamNode(myPixel2, seamNode1, ImageGraph.SeamNode.PreviousSeamRelationship.DIAGONAL_LEFT, 200);
        ImageGraph.SeamNode seamNode3 = new ImageGraph.SeamNode(myPixel3, seamNode2, ImageGraph.SeamNode.PreviousSeamRelationship.DIAGONAL_RIGHT, new ImageGraph.BlueEnergy());

        Assertions.assertThat(seamNode1.getPixel()).isEqualTo(myPixel1);
        Assertions.assertThat(seamNode2.getPixel()).isEqualTo(myPixel2);
        Assertions.assertThat(seamNode3.getPixel()).isEqualTo(myPixel3);

        Assertions.assertThat(seamNode1.getPreviousNode()).isEqualTo(null);
        Assertions.assertThat(seamNode2.getPreviousNode()).isEqualTo(seamNode1);
        Assertions.assertThat(seamNode3.getPreviousNode()).isEqualTo(seamNode2);

        seamNode1.setPreviousNode(seamNode2);
        seamNode2.setPreviousNode(seamNode3);
        seamNode3.setPreviousNode(seamNode1);

        Assertions.assertThat(seamNode1.getPreviousNode()).isEqualTo(seamNode2);
        Assertions.assertThat(seamNode2.getPreviousNode()).isEqualTo(seamNode3);
        Assertions.assertThat(seamNode3.getPreviousNode()).isEqualTo(seamNode1);

        Assertions.assertThat(seamNode1.getRelationship()).isEqualTo(ImageGraph.SeamNode.PreviousSeamRelationship.STRAIGHT_UP);
        Assertions.assertThat(seamNode2.getRelationship()).isEqualTo(ImageGraph.SeamNode.PreviousSeamRelationship.DIAGONAL_LEFT);
        Assertions.assertThat(seamNode3.getRelationship()).isEqualTo(ImageGraph.SeamNode.PreviousSeamRelationship.DIAGONAL_RIGHT);

        Assertions.assertThat(seamNode1.getCost()).isEqualTo(0);
        Assertions.assertThat(seamNode2.getCost()).isEqualTo(200);
        Assertions.assertThat(seamNode3.getCost()).isEqualTo(455);
    }

    // ------ Energy Calculation ------ \\
    @Test
    void testBlueEnergy() {
        ImageGraph.BlueEnergy blueEnergy = new ImageGraph.BlueEnergy();

        ImageGraph.Pixel pixel1 = new ImageGraph.Pixel(Color.BLUE.getRGB());
        ImageGraph.Pixel pixel2 = new ImageGraph.Pixel(Color.RED.getRGB());
        ImageGraph.Pixel pixel3 = new ImageGraph.Pixel(new Color(100, 100, 100).getRGB());
        ImageGraph.Pixel pixel4 = new ImageGraph.Pixel(Color.BLACK.getRGB());
        ImageGraph.Pixel pixel5 = new ImageGraph.Pixel(Color.WHITE.getRGB());

        Assertions.assertThat(blueEnergy.energyFormula(pixel1)).isEqualTo(0);
        Assertions.assertThat(blueEnergy.energyFormula(pixel2)).isEqualTo(255);
        Assertions.assertThat(blueEnergy.energyFormula(pixel3)).isEqualTo(155);
        Assertions.assertThat(blueEnergy.energyFormula(pixel4)).isEqualTo(255);
        Assertions.assertThat(blueEnergy.energyFormula(pixel5)).isEqualTo(0);
    }

    @Test
    void testBrightnessEnergy() {
        ImageGraph.BrightnessEnergy brightnessEnergy = new ImageGraph.BrightnessEnergy();

        ImageGraph imageGraph = makeImageGraph();

        double A = imageGraph.getHead().getBrightness();
        double B = imageGraph.getHead().rightNeighbor.getBrightness();
        double C = imageGraph.getHead().rightNeighbor.rightNeighbor.getBrightness();
        double D = imageGraph.getHead().child.getBrightness();
        double E = imageGraph.getHead().child.rightNeighbor.getBrightness();
        double F = imageGraph.getHead().child.rightNeighbor.rightNeighbor.getBrightness();
        double G = imageGraph.getHead().child.child.getBrightness();
        double H = imageGraph.getHead().child.child.rightNeighbor.getBrightness();
        double I = imageGraph.getHead().child.child.rightNeighbor.rightNeighbor.getBrightness();

        double expected1 = Math.sqrt(
                Math.pow(4*A - (A + 2*D + E), 2)
                        + Math.pow(4*A - (A + 2*B + E), 2)
        );
        double expected2 = Math.sqrt(
                Math.pow(A + 2*B + C - (G + 2*H + I), 2)
                        + Math.pow(A + 2*D + G - (C + 2*F + I), 2)
        );
        double expected3 = Math.sqrt(
                Math.pow(E + 2*F + I - (4*I), 2)
                        + Math.pow(E + 2*H + I - (4*I), 2)
        );

        Assertions.assertThat(brightnessEnergy.energyFormula(imageGraph.getHead())).isEqualTo(expected1);
        Assertions.assertThat(brightnessEnergy.energyFormula(imageGraph.getHead().child.rightNeighbor)).isEqualTo(expected2);
        Assertions.assertThat(brightnessEnergy.energyFormula(imageGraph.getHead().child.rightNeighbor.child.rightNeighbor)).isEqualTo(expected3);
    }
    // -------------------------------- \\

    @Test
    void testGetWidth(){
        ImageGraph imageGraph = makeImageGraph();

        Assertions.assertThat(imageGraph.getWidth()).isEqualTo(3);

        ImageGraph.SeamNode seam = imageGraph.findSeam(new ImageGraph.BlueEnergy());

        imageGraph.highlightSeam(seam, Color.RED);
        Assertions.assertThat(imageGraph.getWidth()).isEqualTo(3);


        imageGraph.removeSeam(seam);
        Assertions.assertThat(imageGraph.getWidth()).isEqualTo(2);

        imageGraph.insertSeam(seam, true);
        Assertions.assertThat(imageGraph.getWidth()).isEqualTo(3);

        imageGraph.insertSeam(seam, false);
        Assertions.assertThat(imageGraph.getWidth()).isEqualTo(3);
    }

    @Test
    void testFindBlueSeam() {
        ImageGraph.BlueEnergy blueEnergy = new ImageGraph.BlueEnergy();

        ImageGraph imageGraph = makeImageGraph();

        ImageGraph.SeamNode actualBlueSeam = imageGraph.findSeam(blueEnergy);

        ImageGraph.SeamNode expectedBlueSeamTop = new ImageGraph.SeamNode(
                imageGraph.getHead().rightNeighbor.rightNeighbor,
                blueEnergy
        );
        ImageGraph.SeamNode expectedBlueSeamMid = new ImageGraph.SeamNode(
                imageGraph.getHead().child.rightNeighbor.rightNeighbor,
                expectedBlueSeamTop,
                ImageGraph.SeamNode.PreviousSeamRelationship.STRAIGHT_UP,
                255
        );
        ImageGraph.SeamNode expectedBlueSeam = new ImageGraph.SeamNode(
                imageGraph.getHead().child.child.rightNeighbor.rightNeighbor,
                expectedBlueSeamMid,
                ImageGraph.SeamNode.PreviousSeamRelationship.STRAIGHT_UP,
                255
        );

        Assertions.assertThat(compareSeamNodes(actualBlueSeam, expectedBlueSeam));
    }

    @Test
    void testFindBrightSeam() {
        ImageGraph.BrightnessEnergy brightnessEnergy = new ImageGraph.BrightnessEnergy();

        ImageGraph imageGraph = makeImageGraph();

        ImageGraph.SeamNode actualBrightSeam = imageGraph.findSeam(brightnessEnergy);

        ImageGraph.SeamNode expectedBlueSeamTop = new ImageGraph.SeamNode(
                imageGraph.getHead(),
                brightnessEnergy
        );
        ImageGraph.SeamNode expectedBlueSeamMid = new ImageGraph.SeamNode(
                imageGraph.getHead().child.rightNeighbor,
                expectedBlueSeamTop,
                ImageGraph.SeamNode.PreviousSeamRelationship.DIAGONAL_LEFT,
                (97.78093429248419 + 203.59273071502332)
        );
        ImageGraph.SeamNode expectedBlueSeam = new ImageGraph.SeamNode(
                imageGraph.getHead().child.child,
                expectedBlueSeamMid,
                ImageGraph.SeamNode.PreviousSeamRelationship.DIAGONAL_RIGHT,
                (97.78093429248419 + 203.59273071502332 + 356.33941373047384)
        );

        Assertions.assertThat(compareSeamNodes(actualBrightSeam, expectedBlueSeam));
    }

    /**
     * Compares two SeamNodes to see if they are equivalent.
     * @param a The first seam node to compare.
     * @param b The second seam node to be compared to
     * @return Whether they are the same in all values.
     */
    private boolean compareSeamNodes(ImageGraph.SeamNode a, ImageGraph.SeamNode b) {
        if (a == null) {
            return b == null;
        }

        if (a.equals(b)) {
            return true;
        }

        return a.getPixel() == b.getPixel() &&
                a.getCost() == b.getCost() &&
                a.getRelationship() == b.getRelationship() &&
                compareSeamNodes(a.getPreviousNode(), b.getPreviousNode());
    }

    @Test
    void testHighlightSeam() {
        ImageGraph.BlueEnergy blueEnergy = new ImageGraph.BlueEnergy();

        ImageGraph imageGraph = makeImageGraph();

        ImageGraph.SeamNode ourSeam2 = new ImageGraph.SeamNode(
                imageGraph.getHead().rightNeighbor.rightNeighbor,
                blueEnergy
        );
        ImageGraph.SeamNode ourSeam1 = new ImageGraph.SeamNode(
                imageGraph.getHead().child.rightNeighbor.rightNeighbor,
                ourSeam2,
                ImageGraph.SeamNode.PreviousSeamRelationship.STRAIGHT_UP,
                255
        );
        ImageGraph.SeamNode ourSeam = new ImageGraph.SeamNode(
                imageGraph.getHead().child.child.rightNeighbor.rightNeighbor,
                ourSeam1,
                ImageGraph.SeamNode.PreviousSeamRelationship.STRAIGHT_UP,
                255
        );

        ImageGraph.SeamNode actualSeam = imageGraph.highlightSeam(ourSeam, Color.DARK_GRAY);

        // go through the graph and make sure that the seam is inserted
        Assertions.assertThat(imageGraph.getHead().rightNeighbor.rightNeighbor.getRGB()).isEqualTo(Color.DARK_GRAY.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.rightNeighbor.rightNeighbor.getRGB()).isEqualTo(Color.DARK_GRAY.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.child.rightNeighbor.rightNeighbor.getRGB()).isEqualTo(Color.DARK_GRAY.getRGB());

        Assertions.assertThat(actualSeam.getPixel().leftNeighbor.leftNeighbor.equals(imageGraph.getHead().child.child)).isEqualTo(true);
        Assertions.assertThat(actualSeam.getPreviousNode().getPixel().leftNeighbor.leftNeighbor.equals(imageGraph.getHead().child)).isEqualTo(true);
        Assertions.assertThat(actualSeam.getPreviousNode().getPreviousNode().getPixel().leftNeighbor.leftNeighbor.equals(imageGraph.getHead())).isEqualTo(true);
    }

    @Test
    void testRemoveSeam() {
        ImageGraph.BrightnessEnergy brightnessEnergy = new ImageGraph.BrightnessEnergy();
        ImageGraph imageGraph = makeImageGraph();
        ImageGraph.SeamNode seam = imageGraph.findSeam(brightnessEnergy);

        ImageGraph.SeamNode actualSeam = imageGraph.removeSeam(seam);

        Assertions.assertThat(compareSeamNodes(seam, actualSeam)).isEqualTo(true);
        Assertions.assertThat(imageGraph.getWidth()).isEqualTo(2);

        // go through the graph and make sure that the seam is inserted
        Assertions.assertThat(imageGraph.getHead().rightNeighbor.rightNeighbor).isEqualTo(null);
        Assertions.assertThat(imageGraph.getHead().child.rightNeighbor.rightNeighbor).isEqualTo(null);
        Assertions.assertThat(imageGraph.getHead().child.child.rightNeighbor.rightNeighbor).isEqualTo(null);

        Assertions.assertThat(imageGraph.getHead().getRGB()).isEqualTo(Color.ORANGE.getRGB());
        Assertions.assertThat(imageGraph.getHead().rightNeighbor.getRGB()).isEqualTo(Color.YELLOW.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.getRGB()).isEqualTo(Color.GREEN.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.rightNeighbor.getRGB()).isEqualTo(Color.BLUE.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.child.getRGB()).isEqualTo(Color.PINK.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.child.rightNeighbor.getRGB()).isEqualTo(Color.CYAN.getRGB());
    }

    @Test
    void testReinsertSeam() {
        ImageGraph.BrightnessEnergy brightnessEnergy = new ImageGraph.BrightnessEnergy();
        ImageGraph imageGraph = makeImageGraph();

        ImageGraph.SeamNode originalSeam = imageGraph.findSeam(brightnessEnergy);

        ImageGraph.SeamNode highlightedSeam = imageGraph.highlightSeam(originalSeam, new Color(79, 113, 128));

        imageGraph.insertSeam(originalSeam, false);

        Assertions.assertThat(imageGraph.getWidth()).isEqualTo(3);
        Assertions.assertThat(imageGraph.getHead().getRGB()).isEqualTo(Color.RED.getRGB());
        Assertions.assertThat(imageGraph.getHead().rightNeighbor.getRGB()).isEqualTo(Color.ORANGE.getRGB());
        Assertions.assertThat(imageGraph.getHead().rightNeighbor.rightNeighbor.getRGB()).isEqualTo(Color.YELLOW.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.getRGB()).isEqualTo(Color.GREEN.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.rightNeighbor.getRGB()).isEqualTo(Color.BLACK.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.rightNeighbor.rightNeighbor.getRGB()).isEqualTo(Color.BLUE.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.child.getRGB()).isEqualTo(Color.MAGENTA.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.child.rightNeighbor.getRGB()).isEqualTo(Color.PINK.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.child.rightNeighbor.rightNeighbor.getRGB()).isEqualTo(Color.CYAN.getRGB());


        imageGraph.removeSeam(originalSeam);

        Assertions.assertThat(imageGraph.getWidth()).isEqualTo(2);

        imageGraph.insertSeam(originalSeam, true);

        Assertions.assertThat(imageGraph.getWidth()).isEqualTo(3);
        Assertions.assertThat(imageGraph.getHead().getRGB()).isEqualTo(Color.RED.getRGB());
        Assertions.assertThat(imageGraph.getHead().rightNeighbor.getRGB()).isEqualTo(Color.ORANGE.getRGB());
        Assertions.assertThat(imageGraph.getHead().rightNeighbor.rightNeighbor.getRGB()).isEqualTo(Color.YELLOW.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.getRGB()).isEqualTo(Color.GREEN.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.rightNeighbor.getRGB()).isEqualTo(Color.BLACK.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.rightNeighbor.rightNeighbor.getRGB()).isEqualTo(Color.BLUE.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.child.getRGB()).isEqualTo(Color.MAGENTA.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.child.rightNeighbor.getRGB()).isEqualTo(Color.PINK.getRGB());
        Assertions.assertThat(imageGraph.getHead().child.child.rightNeighbor.rightNeighbor.getRGB()).isEqualTo(Color.CYAN.getRGB());
    }
}
