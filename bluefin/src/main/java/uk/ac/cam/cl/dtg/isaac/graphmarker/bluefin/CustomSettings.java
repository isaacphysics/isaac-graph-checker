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
package uk.ac.cam.cl.dtg.isaac.graphmarker.bluefin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import uk.ac.cam.cl.dtg.isaac.graphmarker.geometry.Sector;
import uk.ac.cam.cl.dtg.isaac.graphmarker.geometry.SectorBuilder;
import uk.ac.cam.cl.dtg.isaac.graphmarker.geometry.SectorClassifier;
import uk.ac.cam.cl.dtg.isaac.graphmarker.settings.SettingsWrapper;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CustomSettings implements SettingsWrapper {

    private static class SectorSerializer extends StdSerializer<Sector> {

        public SectorSerializer() {
            this(null);
        }

        public SectorSerializer(Class<Sector> t) {
            super(t);
        }

        @Override
        public void serialize(Sector value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
            jgen.writeString(value.toString());
        }
    }

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Sector.class, new SectorSerializer());
        OBJECT_MAPPER.registerModule(simpleModule);
        OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private final double slopeThreshold;
    private final int numberOfPointsAtEnds;
    private final double symmetrySimilarity;
    private final List<Sector> orderedSectors;
    private final double axisSlop;
    private final double originSlop;
    private final double relaxedOriginSlop;

    public CustomSettings() {
        this(
            DEFAULT.getSlopeThreshold(),
            DEFAULT.getNumberOfPointsAtEnds(),
            DEFAULT.getSymmetrySimilarity(),
            DEFAULT.getOrderedSectors().stream().map(Sector::toString).collect(Collectors.toList()),
            DEFAULT.getAxisSlop(),
            DEFAULT.getOriginSlop(),
            DEFAULT.getRelaxedOriginSlop()
        );
    }

    @JsonCreator
    public CustomSettings(@JsonProperty("slopeThreshold") double slopeThreshold,
                          @JsonProperty("numberOfPointsAtEnds") int numberOfPointsAtEnds,
                          @JsonProperty("symmetrySimilarity") double symmetrySimilarity,
                          @JsonProperty("orderedSectors") List<String> orderedSectors,
                          @JsonProperty("axisSlop") double axisSlop,
                          @JsonProperty("originSlop") double originSlop,
                          @JsonProperty("relaxedOriginSlop") double relaxedOriginSlop) {
        this.slopeThreshold = slopeThreshold;
        this.numberOfPointsAtEnds = numberOfPointsAtEnds;
        this.symmetrySimilarity = symmetrySimilarity;
        this.axisSlop = axisSlop;
        this.originSlop = originSlop;
        this.relaxedOriginSlop = relaxedOriginSlop;
        SectorBuilder sectorBuilder = this.getSectorBuilder();
        this.orderedSectors = sectorBuilder.fromList(orderedSectors.stream());
    }

    @Override
    public double getSlopeThreshold() {
        return slopeThreshold;
    }

    @Override
    public int getNumberOfPointsAtEnds() {
        return numberOfPointsAtEnds;
    }

    @Override
    public double getSymmetrySimilarity() {
        return symmetrySimilarity;
    }

    @Override
    public List<Sector> getOrderedSectors() {
        return orderedSectors;
    }

    @Override
    public double getAxisSlop() {
        return axisSlop;
    }

    @Override
    public double getOriginSlop() {
        return originSlop;
    }

    @Override
    public double getRelaxedOriginSlop() {
        return relaxedOriginSlop;
    }

    @JsonIgnore
    @Override
    public SectorClassifier getSectorClassifier() {
        return SettingsWrapper.super.getSectorClassifier();
    }

    @JsonIgnore
    @Override
    public SectorBuilder getSectorBuilder() {
        return SettingsWrapper.super.getSectorBuilder();
    }
}
