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
package uk.ac.cam.cl.dtg.isaac.graphmarker.settings;

import uk.ac.cam.cl.dtg.isaac.graphmarker.features.SlopeFeature;
import uk.ac.cam.cl.dtg.isaac.graphmarker.features.SymmetryFeature;
import uk.ac.cam.cl.dtg.isaac.graphmarker.geometry.SectorBuilder;
import uk.ac.cam.cl.dtg.isaac.graphmarker.geometry.SectorClassifier;

/**
 * Any customised settings must inherit from this wrapper in order to have all the required settings.
 *
 * This is basically tying a knot so all of the settings are available in one object, using interfaces with defaults to
 * allow the necessary multiple-inheritance.
 */
@SuppressWarnings("interfaceIsType")
public interface SettingsWrapper extends SettingsInterface.None,
    SlopeFeature.Settings,
    SymmetryFeature.Settings,
    SectorBuilder.Settings,
    SectorClassifier.Settings {

    /**
     * The default set of settings for everything.
     */
    SettingsWrapper DEFAULT = new SettingsWrapper() { };
}
