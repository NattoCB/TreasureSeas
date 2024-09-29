package io.github.nattocb.treasure_seas.core.utility;

import io.github.nattocb.treasure_seas.TreasureSeas;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WaveGenerator {
    private final double maxYChange;
    private final double[] yRange;
    private final float[] flatSegmentRange;
    private final float[] nonFlatSegmentRange;
    private double initialY;
    private final List<Segment> segments;
    private double currentY;
    private double xStart;
    private final Random random;

    public WaveGenerator(double maxYChange, double[] yRange, float[] flatSegmentRange, float[] nonFlatSegmentRange) {
        this.random = TreasureSeas.RANDOM;
        this.maxYChange = maxYChange;
        this.yRange = yRange;
        this.flatSegmentRange = flatSegmentRange;
        this.nonFlatSegmentRange = nonFlatSegmentRange;
        this.initialY = getRandomInRange(yRange);
        this.segments = new ArrayList<>();
        this.currentY = this.initialY;
        this.xStart = 0;
    }

    private double getRandomInRange(double[] range) {
        return range[0] + (range[1] - range[0]) * random.nextDouble();
    }

    private void generateSegment() {
        float flatSegmentLength = random.nextFloat() * (flatSegmentRange[1] - flatSegmentRange[0]) + flatSegmentRange[0];
        float nonFlatSegmentLength = random.nextFloat() * (nonFlatSegmentRange[1] - nonFlatSegmentRange[0]) + nonFlatSegmentRange[0];
        double xEndFlat = xStart + flatSegmentLength;
        // add Flat segment
        segments.add(new Segment(xStart, xEndFlat, currentY, currentY));
        xStart = xEndFlat;
        double nonFlatYChange = (2 * random.nextDouble() - 1) * maxYChange;
        double nextY = Math.max(Math.min(currentY + nonFlatYChange, yRange[1]), yRange[0]);
        double xEndNonFlat = xStart + nonFlatSegmentLength;
        // add Non-flat segment
        segments.add(new Segment(xStart, xEndNonFlat, currentY, nextY));
        currentY = nextY;
        xStart = xEndNonFlat;
    }

    public double getY(double x) {
        // Generate new segments if x is beyond the current segments
        while (x >= xStart) {
            generateSegment();
        }
        for (Segment segment : segments) {
            if (segment.xStart <= x && x < segment.xEnd) {
                if (segment.yStart == segment.yEnd) {
                    return segment.yStart;
                } else {
                    return segment.yStart + (segment.yEnd - segment.yStart) * (x - segment.xStart) / (segment.xEnd - segment.xStart);
                }
            }
        }
        // This line should never be reached if the logic is correct
        return 0;
    }

    private class Segment {
        double xStart, xEnd, yStart, yEnd;

        Segment(double xStart, double xEnd, double yStart, double yEnd) {
            this.xStart = xStart;
            this.xEnd = xEnd;
            this.yStart = yStart;
            this.yEnd = yEnd;
        }
    }

}
