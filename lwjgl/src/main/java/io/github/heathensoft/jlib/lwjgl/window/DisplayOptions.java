package io.github.heathensoft.jlib.lwjgl.window;

import org.lwjgl.glfw.GLFWVidMode;

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Utility class if you need to populate a menu or something with
 * a monitors' resolution options. All options in map are unique.
 *
 * @author Frederik Dahl
 * 27/10/2022
 */


public class DisplayOptions {
    
    private final Map<Resolution, List<Integer>> map;
    
    protected DisplayOptions(long monitor) {
        map = new HashMap<>();
        Set<Resolution> set = new HashSet<>();
        GLFWVidMode.Buffer modes = glfwGetVideoModes(monitor);
        if (modes != null) {
            for (int i = 0; i < modes.capacity(); i++) {
                modes.position(i);
                List<Integer> list;
                Resolution res = new Resolution(modes.width(),modes.height());
                int refreshRate = modes.refreshRate();
                if (set.add(res)) {
                    list = new ArrayList<>();
                    list.add(refreshRate);
                    map.put(res,list);
                } else { list = map.get(res);
                    if (list != null) {
                        if (!list.contains(refreshRate)) {
                            list.add(refreshRate);
                        }
                    }
                }
            }
        }
        for (Map.Entry<Resolution,List<Integer>> entry :
                map.entrySet()) {
            entry.getValue().sort(Collections.reverseOrder());
        }
    }
    
    public Map<Resolution,List<Integer>> get() {
        return map;
    }
    
    public List<Resolution> supportedResolutions() {
        List<Resolution> options =
        new ArrayList<>(map.keySet());
        options.sort(Comparator.naturalOrder());
        return options;
    }
    
    public List<Integer> refreshRates(Resolution resolution) {
        List<Integer> list = map.get(resolution);
        return list == null ? new ArrayList<>() : List.copyOf(list);
    }
    
    public boolean supports(Resolution resolution, int hz) {
        List<Integer> options = refreshRates(resolution);
        if (hz == GLFW_DONT_CARE) {
            return !options.isEmpty();
        } return options.contains(hz);
    }
    
    public boolean supports(Resolution resolution) {
        return map.containsKey(resolution);
    }
    
}
