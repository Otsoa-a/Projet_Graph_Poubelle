public class Arc {
    public final String nom;
    public final int nbBatiments;
    public final double longueur;
    public final boolean sensUnique;

    public final Intersection depart;
    public final Intersection arrivee;

    boolean utilise = false;

    public Arc(String nom, int nbBatiments, double longueur,
               boolean sensUnique, Intersection depart, Intersection arrivee) {

        this.nom = nom;
        this.nbBatiments = nbBatiments;
        this.longueur = longueur;
        this.sensUnique = sensUnique;
        this.depart = depart;
        this.arrivee = arrivee;
    }

    @Override
    public String toString() {
        return depart.id + " -> " + arrivee.id +
                " : " + nom + " (" + longueur + "m, " + nbBatiments + " bat.)";
    }
}
