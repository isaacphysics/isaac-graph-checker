/**
 * Copyright 2019 University of Cambridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.cam.cl.dtg.isaac.graphmarker.features;

import com.google.common.collect.ImmutableMap;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public class AssignmentContext {

    private static final Stack<AssignmentContext> contexts = new Stack<>();

    public static void push() {
        contexts.push(new AssignmentContext());
    }

    public static void pop() {
        contexts.pop();
    }

    public static AssignmentContext Current() {
        return contexts.peek();
    }

    static String standardLineName(int index) {
        return Character.toString((char) (index + 65));
    }

    private Set<ImmutableMap<String, Line>> assignments;
    private final Set<String> names;

    private AssignmentContext() {
        assignments = new HashSet<>();
        assignments.add(ImmutableMap.of());
        names = new HashSet<>();
    }

    public Set<ImmutableMap<String, Line>> getAssignments() {
        return assignments;
    }

    boolean checkFinally() {
        return !assignments.isEmpty();
    }

    public void putIfAbsent(String name, List<Line> lines) {
        if (!names.contains(name)) {
            names.add(name);
            // Spread all possible assignments for name
            assignments = assignments.stream()
                .flatMap(assignment -> {
                    Set<Line> usedLines = new HashSet<>(assignment.values());
                    return lines.stream()
                        .filter(line -> !usedLines.contains(line))
                        .map(line -> ImmutableMap.<String, Line>builder().putAll(assignment).put(name, line).build());
                })
                .collect(Collectors.toSet());
        }
    }

    /*private boolean assign(List<String> names, int nameIndex, Set<Line> assignedLines) {
        if (nameIndex == names.size()) {
            return true;
        }

        String name = names.get(nameIndex);

        if (assignments.get(name).isEmpty()) {
            return false;
        }

        List<Line> lines = assignments.get(name);

        for (Line line : lines) {
            if (assignedLines.contains(line)) {
                continue;
            }
            ImmutableSet<Line> newAssignedLines = ImmutableSet.<Line>builder()
                .addAll(assignedLines)
                .add(line).build();
            if (assign(names, nameIndex + 1, newAssignedLines)) {
                return true;
            }
        }
        return false;
    }*/
}
