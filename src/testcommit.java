import java.util.*;

public class testcommit {

    static class Edge {
        int from, to;
        int distance;
        Edge(int f, int t, int d) {
            from = f; to = t; distance = d;
        }
    }

    static class House {
        String name;
        int u, v;
        int pos; // distance depuis u
        House(String n, int u, int v, int p) {
            name = n; this.u = u; this.v = v; pos = p;
        }
    }

    static class Building {
        String name;
        int u, v;
        int pos;
        Building(String n, int u, int v, int p) {
            name = n; this.u = u; this.v = v; pos = p;
        }
    }

    public static void main(String[] args) {
        // --- Sommets
        Set<Integer> nodes = new HashSet<>();
        for (int i = 1; i <= 10; i++) nodes.add(i);

        // --- Arêtes (rues)
        List<Edge> edges = Arrays.asList(
                new Edge(1,2,120), new Edge(2,3,80), new Edge(3,4,150),
                new Edge(2,5,200), new Edge(5,6,140), new Edge(6,7,90),
                new Edge(3,8,110), new Edge(8,9,130), new Edge(9,10,160),
                new Edge(4,10,180)
        );

        // --- Habitations
        List<House> houses = Arrays.asList(
                new House("H1",1,2,40),
                new House("H2",2,3,70),
                new House("H3",3,8,45),
                new House("H4",5,6,100),
                new House("H5",9,10,40)
        );

        // --- Bâtiments
        List<Building> buildings = Arrays.asList(
                new Building("Mairie",3,4,60),
                new Building("Ecole",2,5,50)
        );

        // --- Génération ASCII très simple ---
        // Pour simplifier, on affiche chaque sommet verticalement et les arêtes comme lignes
        Map<Integer,Integer> levels = new HashMap<>();
        int level = 0;
        for(int node: nodes) {
            levels.put(node, level++);
        }

        System.out.println("Carte ASCII simplifiée du village :\n");

        for(Edge e: edges) {
            String marker = "";
            for(House h: houses) {
                if ((h.u==e.from && h.v==e.to) || (h.u==e.to && h.v==e.from)) {
                    marker += h.name + "|"+h.pos+" ";
                }
            }
            for(Building b: buildings) {
                if ((b.u==e.from && b.v==e.to) || (b.u==e.to && b.v==e.from)) {
                    marker += b.name + "|"+b.pos+" ";
                }
            }
            System.out.println("(" + e.from + ")---" + e.distance + "---(" + e.to + ") " + marker);
        }

        System.out.println("\nSommets : " + nodes);
    }
}
