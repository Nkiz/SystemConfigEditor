package cnc.msl.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class Value {

    private List<Map<String, String>> profiles = new ArrayList<Map<String, String>>();

    @JsonAnySetter
    public void setDynamicProperty(String name, Map<String, String> map) {
        profiles.add(map);
    }

    public List<Map<String, String>> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<Map<String, String>> profiles) {
        this.profiles = profiles;
    }
}
