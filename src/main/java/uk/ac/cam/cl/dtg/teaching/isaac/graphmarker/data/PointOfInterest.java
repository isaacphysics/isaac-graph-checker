package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data;

public class PointOfInterest extends Point {
    private final PointType pointType;

    public PointOfInterest(double x, double y, PointType pointType) {
        super(x, y);
        this.pointType = pointType;
    }

    public PointOfInterest(Point point, PointType pointType) {
        super(point.getX(), point.getY());
        this.pointType = pointType;
    }

    public PointType getPointType() {
        return pointType;
    }

    public PointOfInterest minus(Point p) {
        return new PointOfInterest(super.minus(p), this.pointType);
    }

}
