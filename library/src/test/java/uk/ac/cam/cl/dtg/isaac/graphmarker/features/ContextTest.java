package uk.ac.cam.cl.dtg.isaac.graphmarker.features;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.ac.cam.cl.dtg.isaac.graphmarker.TestHelpers;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;

import java.util.Collections;

public class ContextTest {

    @Test(expected = IllegalArgumentException.class)
    public void setFulfilledAssignmentsToBeEmpty() {
        Input input = TestHelpers.inputOf();

        Context context = new Context(input);

        context.withFulfilledAssignments(Collections.emptySet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setFulfilledAssignmentsToMissingName() {
        Line line1 = TestHelpers.lineOf(x -> x, -10, 10);
        Input input = TestHelpers.inputOf(line1);

        Context context = new Context(input);

        context.withFulfilledAssignments(Collections.singleton(ImmutableBiMap.of("a", line1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setFulfilledAssignmentsToMissingLine() {
        Line line1 = TestHelpers.lineOf(x -> x, -10, 10);
        Line line2missing = TestHelpers.lineOf(x -> -x, -10, 10);
        Input input = TestHelpers.inputOf(line1);

        Context context = new Context(input);

        context = context.putIfAbsent("a");

        context = context.withFulfilledAssignments(Collections.singleton(ImmutableBiMap.of("a", line2missing)));
    }
}