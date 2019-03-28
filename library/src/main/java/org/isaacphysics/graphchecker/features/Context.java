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
package org.isaacphysics.graphchecker.features;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import org.isaacphysics.graphchecker.data.Input;
import org.isaacphysics.graphchecker.data.Line;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * An immutable set of possible mappings from names to lines.
 *
 * The idea of a context is keep making new contexts as we try and match each feature, and if we have a Context left at
 * the end, then there is one or more valid mappings between names and lines.
 */
public class Context {

    /**
     * Get the standard name given to this numbered line.
     * @param index The number of the line.
     * @return The letters A to Z for lines 0-25.
     */
    static String standardLineName(int index) {
        return Character.toString((char) (index + 'A'));
    }

    private ImmutableSet<ImmutableBiMap<String, Line>> assignments;
    private final ImmutableSet<String> names;
    private final ImmutableSet<Line> lines;

    /**
     * Create an empty assignment context.
     *
     * @param input The input for this context.
     */
    Context(Input input) {
        assignments = ImmutableSet.of(ImmutableBiMap.of());
        names = ImmutableSet.of();
        this.lines = ImmutableSet.copyOf(input.getLines());
    }

    /**
     * Create a new Context.
     * @param names The names in this context.
     * @param lines The lines in this context.
     * @param assignments The assignments in this context.
     */
    private Context(ImmutableSet<String> names, ImmutableSet<Line> lines,
                    ImmutableSet<ImmutableBiMap<String, Line>> assignments) {
        this.names = names;
        this.lines = lines;
        this.assignments = assignments;
    }

    /**
     * Try to create a new Context based on an existing context and a predicate if a mapping is valid.
     *
     * @param isValidAssignment Returns true if this is a valid mapping of names to lines.
     * @param names Any new names to add.
     * @return A new context if the predicate returns true for any mapping, or null if no mappings are valid.
     */
    @Nullable
    public Context makeNewContext(Predicate<ImmutableBiMap<String, Line>> isValidAssignment, String... names) {
        Context context = this;
        for (String name : names) {
            context = context.putIfAbsent(name);
        }

        Set<ImmutableBiMap<String, Line>> assignments = context.getAssignmentsCopy();

        assignments.removeIf(isValidAssignment.negate());

        if (assignments.isEmpty()) {
            return null;
        } else {
            return context.withFulfilledAssignments(assignments);
        }
    }

    /**
     * @return A copy of the possible assignments from names to lines.
     */
    Set<ImmutableBiMap<String, Line>> getAssignmentsCopy() {
        return new HashSet<>(assignments);
    }

    /**
     * Create a context with a new set of possible assignments.
     * @param fulfilledAssignments The new assignments.
     * @return A new context with these assignmnets.
     * @throws IllegalArgumentException If the new assignments are empty or any names are unknown or lines are unknown.
     */
    @VisibleForTesting
    Context withFulfilledAssignments(Set<ImmutableBiMap<String, Line>> fulfilledAssignments) {
        if (fulfilledAssignments.isEmpty()) {
            throw new IllegalArgumentException("Fulfilled assignments must be non-empty.");
        }
        if (fulfilledAssignments.stream().anyMatch(assignment ->
            assignment.entrySet().stream().anyMatch(entry ->
                !names.contains(entry.getKey()) || !lines.contains(entry.getValue())))) {
            throw new IllegalArgumentException("Fulfilled assignments must only contain known names and lines.");
        }
        return new Context(names, lines, ImmutableSet.copyOf(fulfilledAssignments));
    }

    /**
     * Add this name to the set of possible assignments.
     * @param name The name to add.
     * @return The new context.
     */
    @VisibleForTesting
    Context putIfAbsent(String name) {
        if (!names.contains(name)) {
            // Spread all possible assignments for name
            ImmutableSet<ImmutableBiMap<String, Line>> newAssignments = assignments.stream()
                .flatMap(assignment -> {
                    ImmutableSet<Line> usedLines = assignment.values();
                    return lines.stream()
                        .filter(line -> !usedLines.contains(line))
                        .map(line -> ImmutableBiMap.<String, Line>builder().putAll(assignment).put(name, line).build());
                })
                .collect(ImmutableSet.toImmutableSet());

            return new Context(
                ImmutableSet.<String>builder().addAll(names).add(name).build(),
                lines,
                newAssignments
            );
        }
        return this;
    }
}
