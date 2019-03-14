package uk.ac.cam.cl.dtg.isaac.graphmarker.features;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Settings {
    public static final Settings NONE = new Settings(Collections.emptyMap());

    interface HasSettings {
        String tag();
        Map<String, Castable> defaults();
    }

    private final Set<Settings> children = new HashSet<>();
    private final Map<String, Castable> settings;
    private final HasSettings item;

    public Settings(Map<String, Castable> settings) {
        this.settings = settings;
        this.item = null;
    }

    public Settings(Map<String, Castable> settings, HasSettings item) {
        this.settings = settings;
        this.item = item;
    }

    public Castable get(String name) {
        if (this.item == null) {
            throw new IllegalStateException("Item not set");
        }
        return get(this.item, name);
    }

    public Castable get(HasSettings forItem, String name) {
        String key = forItem.tag() + ":" + name;
        return this.settings.containsKey(key) ? this.settings.get(key) : forItem.defaults().get(name);
    }

    public Settings getFor(HasSettings item) {
        Settings settings = new Settings(this.settings, item);
        this.children.add(settings);
        return settings;
    }

    public Map<String, Castable> getAll() {
        Map<String, Castable> results = new HashMap<>();
        getAllInternal(results);
        return results;
    }

    private void getAllInternal(Map<String, Castable> results) {
        if (item != null) {
            item.defaults().forEach((key, value) -> results.putIfAbsent(item.tag() + ":" + key, value));
        }
        results.putAll(settings);
        children.forEach(child -> {
            child.getAllInternal(results);
        });
    }

}
