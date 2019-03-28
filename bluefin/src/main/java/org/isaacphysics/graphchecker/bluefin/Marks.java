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
package org.isaacphysics.graphchecker.bluefin;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Marks {
    public static class Mark {
        private final ImmutableList<String> passes;
        private final ImmutableList<String> fails;

        public Mark(Collection<String> passes, Collection<String> fails) {
            this.passes = ImmutableList.copyOf(passes);
            this.fails = ImmutableList.copyOf(fails);
        }

        public Mark(Collection<Mark> marks) {
            Function<Function<Mark, Collection<String>>, ImmutableList<String>> getMark = chooser ->
                ImmutableList.copyOf(marks
                    .stream()
                    .flatMap(mark -> chooser.apply(mark).stream())
                    .collect(Collectors.toList()));
            this.passes = getMark.apply(Mark::getPasses);
            this.fails = getMark.apply(Mark::getFails);
        }

        public ImmutableList<String> getPasses() {
            return passes;
        }

        public ImmutableList<String> getFails() {
            return fails;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Mark mark = (Mark) o;
            return passes == mark.passes && fails == mark.fails;
        }

        @Override
        public int hashCode() {
            return Objects.hash(passes, fails);
        }

        public ImmutableList<String> get(boolean passed) {
            return passed ? passes : fails;
        }
    }

    private final Mark unknown;
    private final Mark correct;
    private final Mark incorrect;

    public Marks(Mark unknown, Mark correct, Mark incorrect) {
        this.unknown = unknown;
        this.correct = correct;
        this.incorrect = incorrect;
    }

    public Marks(Collection<Marks> marks) {
        Function<Function<Marks, Mark>, Mark> getMark = chooser ->
            new Mark(marks.stream().map(chooser).collect(Collectors.toList()));

        this.unknown = getMark.apply(Marks::getUnknown);
        this.correct = getMark.apply(Marks::getCorrect);
        this.incorrect = getMark.apply(Marks::getIncorrect);
    }

    public Mark getUnknown() {
        return unknown;
    }

    public Mark getCorrect() {
        return correct;
    }

    public Mark getIncorrect() {
        return incorrect;
    }

    public Mark get(AnswerStatus status) {
        switch (status) {
            case CORRECT:
                return correct;
            case INCORRECT:
                return incorrect;
            case UNKNOWN:
                return unknown;
        }
        throw new IllegalArgumentException("Unknown status: " + status);
    }

    public boolean allCorrect() {
        return unknown.passes.isEmpty()
            && unknown.fails.isEmpty()
            && incorrect.passes.isEmpty()
            && correct.fails.isEmpty();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Marks marks = (Marks) o;
        return Objects.equals(unknown, marks.unknown)
            && Objects.equals(correct, marks.correct)
            && Objects.equals(incorrect, marks.incorrect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unknown, correct, incorrect);
    }
}
