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

import java.util.List;

public class ResponseExplanation {

    private final String encoding;
    private final String[] tags;
    private final String type;
    private List<ResponseExplanation> children;
    private String value;

    @JsonCreator
    public ResponseExplanation(@JsonProperty("encoding") String encoding,
                               @JsonProperty("tags") String[] tags,
                               @JsonProperty("type") String type,
                               @JsonProperty("children") List<ResponseExplanation> children) {
        this.encoding = encoding;
        this.tags = tags;
        this.type = type;
        this.children = children;
    }

    @JsonCreator
    public ResponseExplanation(@JsonProperty("encoding") String encoding,
                               @JsonProperty("tags") String[] tags,
                               @JsonProperty("type") String type,
                               @JsonProperty("value") String value) {
        this.encoding = encoding;
        this.tags = tags;
        this.type = type;
        this.value = value;
    }

    public String getEncoding() {
        return encoding;
    }

    public String[] getTags() {
        return tags;
    }

    public String getType() {
        return type;
    }

    public List<ResponseExplanation> getChildren() {
        return children;
    }

    public String getValue() {
        return value;
    }
}
