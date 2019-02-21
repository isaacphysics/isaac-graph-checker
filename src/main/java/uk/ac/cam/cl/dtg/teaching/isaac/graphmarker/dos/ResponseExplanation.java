package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ResponseExplanation {

    private String encoding;
    private String[] tags;
    private String type;
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
