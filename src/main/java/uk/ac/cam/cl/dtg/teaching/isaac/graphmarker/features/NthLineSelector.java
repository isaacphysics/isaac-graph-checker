package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NthLineSelector implements LineSelector<NthLineSelector.Instance> {

    public static final NthLineSelector manager = new NthLineSelector();

    @Override
    public String TAG() {
        return "line";
    }

    class Instance extends LineSelector.Instance {
        private final int n;

        private Instance(int n, String item) {
            super(item);
            this.n = n;
        }

        @Override
        Predicate<Input> matcher(Predicate<Line> linePredicate) {
            return input -> {
                List<Line> lines = input.getLines();
                if (n > lines.size()) {
                    return false; // Not enough lines
                }
                return linePredicate.test(lines.get(n - 1));
            };
        }
    }

    @Override
    public Instance deserialize(String item) {
        Pattern pattern = Pattern.compile("\\s*([1-9][0-9]*);\\s*(.*)");
        Matcher matcher = pattern.matcher(item);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Not an NthLineSelector: " + item);
        }
        return new Instance(Integer.valueOf(matcher.group(1)), matcher.group(2));
    }

    @Override
    public Map<String, Line> generate(Input input) {
        List<Line> lines = input.getLines();
        Map<String, Line> map = new HashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            map.put((i + 1) + "; ", lines.get(i));
        }
        return map;
    }

    private NthLineSelector() {
    }
}
