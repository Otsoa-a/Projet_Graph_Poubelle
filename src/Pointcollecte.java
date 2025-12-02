class Pointcollecte {
    Intersection inter;
    int contenance; // ci
    String id;      // juste pour référence

    public Pointcollecte(Intersection inter, int contenance) {
        this.inter = inter;
        this.contenance = contenance;
        this.id = inter.id;
    }
}
