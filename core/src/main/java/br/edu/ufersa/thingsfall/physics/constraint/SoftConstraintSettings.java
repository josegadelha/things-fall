package br.edu.ufersa.thingsfall.physics.constraint;

public class SoftConstraintSettings {
    public double hertz;
    public double dampingRatio;

    public SoftConstraintSettings(double hertz, double dampingRatio) {
        this.hertz = hertz;
        this.dampingRatio = dampingRatio;
    }
}
