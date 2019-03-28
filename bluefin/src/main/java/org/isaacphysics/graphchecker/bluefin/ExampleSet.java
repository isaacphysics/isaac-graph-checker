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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.cam.cl.dtg.isaac.graphmarker.dos.GraphAnswer;

import java.util.HashMap;
import java.util.Map;

public class ExampleSet {
    private final String name;
    private final String specification;
    private final GraphAnswer canonical;

    private String id;

    private final Map<String, GraphAnswer> answers;

    private final Map<String, AnswerStatus> results;

    @JsonCreator
    public ExampleSet(@JsonProperty("name") String name,
                      @JsonProperty("specification") String specification,
                      @JsonProperty("canonical") GraphAnswer canonical) {
        this.name = name;
        this.specification = specification;
        this.canonical = canonical;

        this.answers = new HashMap<>();
        this.results = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecification() {
        return specification;
    }

    public GraphAnswer getCanonical() {
        return canonical;
    }

    @JsonIgnore
    public Map<String, GraphAnswer> getAnswers() {
        return answers;
    }

    @JsonIgnore
    public Map<String, AnswerStatus> getResults() {
        return results;
    }

    public void setId(String id) {
        this.id = id;
    }
}
