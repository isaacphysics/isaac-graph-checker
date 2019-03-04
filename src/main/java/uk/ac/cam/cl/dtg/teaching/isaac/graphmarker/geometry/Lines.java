package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.PointOfInterest;

import java.util.ArrayList;
import java.util.List;

public class Lines {
    public static List<Line> splitOnPointsOfInterest(Line line) {
        List<Line> lines = new ArrayList<>(line.getPointsOfInterest().size() + 1);
        Line remainder = line;
        for (PointOfInterest point : line.getPointsOfInterest()) {
            double x = point.getX();
            Line left = Sector.leftOfX(x).clip(remainder);
            remainder = Sector.rightOfX(x).clip(remainder);
            lines.add(left);
        }
        lines.add(remainder);
        return lines;
    }

    public static Point getSize(Line line) {
        if (line.getPoints().isEmpty()) return new Point(0, 0);

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (Point p : line.getPoints()) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getY() > maxY) maxY = p.getY();
        }

        double centreX = (maxX + minX) / 2;
        double centreY = (maxY + minY) / 2;

        double diffX = maxX - minX;
        double diffY = maxY - minY;

        double startX = line.getPoints().get(0).getX();
        double startY = line.getPoints().get(0).getY();

        double x = startX < centreX ? diffX : -diffX;
        double y = startY < centreY ? diffY : -diffY;

        return new Point(x, y);
    }
}
