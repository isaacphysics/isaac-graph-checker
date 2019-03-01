package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NthLineSelector implements LineSelector {

    private final int n;
    private final String item;

    NthLineSelector() {
        n = -1;
        item = "";
    }

    private NthLineSelector(int n, String item) {
        this.n = n;
        this.item = item;
    }

    @Override
    public String TAG() {
        return "line";
    }

    @Override
    public String item() {
        return item;
    }

    @Override
    public NthLineSelector parse(String item) {
        Pattern pattern = Pattern.compile("\\s*([1-9][0-9]*);\\s*(.*)");
        Matcher matcher = pattern.matcher(item);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Not an NthLineSelector: " + item);
        }
        return new NthLineSelector(Integer.valueOf(matcher.group(1)), matcher.group(2));
    }

    @Override
    public Predicate<Input> matcher(Predicate<Line> linePredicate) {
        return input -> {
            List<Line> lines = input.getLines();
            if (n > lines.size()) {
                return false; // Not enough lines
            }
            return linePredicate.test(lines.get(n - 1));
        };
    }
}
