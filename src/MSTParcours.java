import java.util.*;

class MSTParcours {
    public static void parcoursPrefixe(String depotId, Map<String, List<String>> arbre, Set<String> visites, List<String> ordre) {
        visites.add(depotId);
        ordre.add(depotId);

        for (String voisin : arbre.getOrDefault(depotId, new ArrayList<>())) {
            if (!visites.contains(voisin)) {
                parcoursPrefixe(voisin, arbre, visites, ordre);
            }
        }
    }
}