package uk.ac.cam.cl.dtg.isaac.graphmarker.features;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class SettingsTest {

    static public class Item implements Settings.HasSettings {

        private final String tag;
        private final Map<String, Castable> defaults;

        Item(String tag, Map<String, Castable> defaults) {
            this.tag = tag;
            this.defaults = defaults;
        }

        @Override
        public String tag() {
            return tag;
        }

        @Override
        public Map<String, Castable> defaults() {
            return defaults;
        }
    }

    @Test
    public void testDefaultMergingWorksCorrectly() {
        Item item1 = new Item("foo", Collections.singletonMap("a", Castable.of(1)));

        Settings settings1 = new Settings(Collections.emptyMap());
        Settings settings1a = settings1.getFor(item1);

        Settings settings2 = new Settings(Collections.singletonMap("a", Castable.of(2)));
        Settings settings2a = settings2.getFor(item1);

        assertEquals(1, settings1.getAll().get("a").asInt());
        assertEquals(2, settings2.getAll().get("a").asInt());
    }

}