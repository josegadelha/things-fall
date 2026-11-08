package br.edu.ufersa.thingsfall.physics.constraint;

public class ConstraintSettings {
    public String mode = "soft"; // 'off' | 'baumgarte' | 'soft'
    public double baumgarteFactor = 0.1;
    public SoftConstraintSettings contactSoftConstraint = new SoftConstraintSettings(30.0, 10.0);
    public SoftConstraintSettings jointSoftConstraint = new SoftConstraintSettings(60.0, 0.0);
    public double contactSpeed = 3.0;
    public boolean warmStarting = true;
}
