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
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A set of possible mappings from names to lines.
 */
public class Context {

    private final List<Line> lines;

    /**
     * Get the standard name given to this numbered line.
     * @param index The number of the line.
     * @return The letters A to Z for lines 0-25.
     */
    static String standardLineName(int index) {
        return Character.toString((char) (index + 'A'));
    }

    private Set<ImmutableMap<String, Line>> assignments;
    private final Set<String> names;

    /**
     * Create an empty assignment context.
     *
     * @param input The input for this context.
     */
    Context(Input input) {
        assignments = new HashSet<>();
        assignments.add(ImmutableMap.of());
        names = new HashSet<>();
        this.lines = input.getLines();
    }

    /**
     * @return A copy of the possible assignments from names to lines.
     */
    public Set<ImmutableMap<String, Line>> getAssignmentsCopy() {
        return new HashSet<>(assignments);
    }

    /**
     * Set a new set of possible assignments.
     * @param fulfilledAssignments The new assignments.
     * @throws IllegalArgumentException If the new assignments are empty or any names are unknown or lines are unknown.
     */
    public void setFulfilledAssignments(Set<ImmutableMap<String, Line>> fulfilledAssignments) {
        if (fulfilledAssignments.isEmpty()) {
            throw new IllegalArgumentException("Fulfilled assignments must be non-empty.");
        }
        if (fulfilledAssignments.stream().anyMatch(assignment ->
            assignment.entrySet().stream().anyMatch(entry ->
                !names.contains(entry.getKey()) || !lines.contains(entry.getValue())))) {
            throw new IllegalArgumentException("Fulfilled assignments must only contain known names and lines.");
        }
        assignments = fulfilledAssignments;
    }

    /**
     * Add this name to the set of possible assignments.
     * @param name The name to add.
     */
    public void putIfAbsent(String name) {
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
}
