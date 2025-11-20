import java.util.*;

public class Intersection {
    public final String id;
    public List<Arc> sortants = new ArrayList<>();
    public List<Arc> entrants = new ArrayList<>();

    public Intersection(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Intersection " + id;
    }
}
