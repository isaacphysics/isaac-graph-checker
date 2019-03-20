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
package uk.ac.cam.cl.dtg.isaac.graphmarker.geometry;

import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.isaac.graphmarker.settings.SettingsInterface;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SectorClassifier {
    private final Settings settings;

    SectorClassifier(Settings settings) {
        this.settings = settings;
    }

    public interface Settings extends SettingsInterface, SectorBuilder.Settings {
        default List<Sector> getOrderedSectors() {
            return getSectorBuilder().getDefaultOrderedSectors();
        }

        default SectorClassifier getSectorClassifier() {
            return new SectorClassifier(this);
        }
    }

    /**
     * Identify which sector this point is in against the default priority-ordered list of sectors.
     * @param point The point.
     * @return The highest-priority sector that contains this point.
     */
    public Sector classify(Point point) {
        Set<Sector> possibleSectors = classifyAll(point);
        return settings.getOrderedSectors().stream()
            .filter(possibleSectors::contains)
            .findFirst()
            .get();
    }

    /**
     * Identify which sectors this point could be in.
     * @param point The point to be classified.
     * @return The set of sectors this point could be in.
     */
    public Set<Sector> classifyAll(Point point) {
        return settings.getOrderedSectors().stream()
            .filter(sector -> sector.contains(point))
            .collect(Collectors.toSet());
    }
}
