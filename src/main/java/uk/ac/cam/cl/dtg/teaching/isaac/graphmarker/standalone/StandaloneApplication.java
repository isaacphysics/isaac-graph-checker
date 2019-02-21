package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.standalone;

import org.jboss.resteasy.plugins.interceptors.CorsFilter;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("")
public class StandaloneApplication extends Application {

    private Set<Object> singletons;

    @Override
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> clazzes = new HashSet<>();
        clazzes.add(MarkerController.class);
        return clazzes;
    }

    @Override
    public Set<Object> getSingletons() {
        if (singletons == null) {
            CorsFilter corsFilter = new CorsFilter();
            corsFilter.getAllowedOrigins().add("*");

            singletons = new HashSet<Object>();
            singletons.add(corsFilter);
        }
        return singletons;
    }
}
