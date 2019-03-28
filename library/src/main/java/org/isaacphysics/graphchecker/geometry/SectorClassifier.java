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
package org.isaacphysics.graphchecker.geometry;

import org.isaacphysics.graphchecker.data.Point;
import org.isaacphysics.graphchecker.settings.SettingsInterface;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility to classify sectors according to the ordered sector list provided by the settings.
 *
 * By default, uses a default ordered sector list that makes sense for our problems.
 */
public class SectorClassifier {
    private final Settings settings;

    /**
     * Constructor which stores settings.
     * @param settings The settings for this builder.
     */
    SectorClassifier(Settings settings) {
        this.settings = settings;
    }

    private static final Map<Settings, SectorClassifier> SECTOR_CLASSIFIER_CACHE = new HashMap<>();

    /**
     * The type of settings for SectorClassifier.
     */
    public interface Settings extends SettingsInterface, SectorBuilder.Settings {

        /**
         * @return The sectors we classify against, in order of priority.
         */
        default List<Sector> getOrderedSectors() {
            return getSectorBuilder().getDefaultOrderedSectors();
        }

        /**
         * Factory method to get a SectorClassifier with these settings.
         *
         * SectorClassifier objects are cached by this method for performance.
         *
         * @return A SectorClassifier with these settings.
         */
        default SectorClassifier getSectorClassifier() {
            return SECTOR_CLASSIFIER_CACHE.computeIfAbsent(this, SectorClassifier::new);
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
