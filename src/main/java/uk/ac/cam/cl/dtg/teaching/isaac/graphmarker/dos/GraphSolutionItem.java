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
package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GraphSolutionItem {
    private final String graphDefinition;
    private final IsaacAnswerResponse response;

    @JsonCreator
    public GraphSolutionItem(@JsonProperty("graphDefinition") String graphDefinition,
                             @JsonProperty("response") IsaacAnswerResponse response) {
        this.graphDefinition = graphDefinition;
        this.response = response;
    }

    public String getGraphDefinition() {
        return graphDefinition;
    }

    public IsaacAnswerResponse getResponse() {
        return response;
    }
}
