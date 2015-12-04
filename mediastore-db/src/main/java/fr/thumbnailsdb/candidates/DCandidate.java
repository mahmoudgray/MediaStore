package fr.thumbnailsdb.candidates;

/**
 * Created by fhuet on 26/04/2014.
 */
//we need to add a distance field to the candidates
public class DCandidate {
    private double distance;
    private Candidate candidate;

    public DCandidate(Candidate c, double d) {
        this.candidate = c;
        this.distance = d;
    }
    public Candidate getCandidate() {
        return candidate;
    }
    public double getDistance() {
        return distance;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }
}