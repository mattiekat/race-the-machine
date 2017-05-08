package plu.teamtwo.rtm.ii;

import plu.teamtwo.rtm.ii.util.Line;
import plu.teamtwo.rtm.ii.util.Polygon;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Utility Structure Class representing a collection of data that results in an individual step in <code>RTSProcessor</code>.
 */
public class ProcessedData {

    public final BufferedImage capturedImage;
    public final BufferedImage processedImage;
    public final List<Polygon> polygons;

    public ProcessedData(BufferedImage capturedImage, BufferedImage processedImage, List<Polygon> polygons) {
        this.capturedImage = capturedImage;
        this.processedImage = processedImage;
        this.polygons = polygons;
    }
}
