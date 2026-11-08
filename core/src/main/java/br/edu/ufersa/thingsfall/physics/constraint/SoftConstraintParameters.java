package br.edu.ufersa.thingsfall.physics.constraint;

public class SoftConstraintParameters {
    public double biasRate;
    public double massScale;
    public double impulseScale;

    public SoftConstraintParameters(double biasRate, double massScale, double impulseScale) {
        this.biasRate = biasRate;
        this.massScale = massScale;
        this.impulseScale = impulseScale;
    }

    public static SoftConstraintParameters getSoftConstraintParameters(
        double hertz,
        double dampingRatio,
        double timeStep
    ) {
        if (hertz == 0.0) return new SoftConstraintParameters(0.0, 0.0, 0.0);

        double angularFrequency = 2.0 * Math.PI * hertz;
        double firstCoefficient = 2.0 * dampingRatio + timeStep * angularFrequency;
        double secondCoefficient = timeStep * angularFrequency * firstCoefficient;
        double thirdCoefficient = 1.0 / (1.0 + secondCoefficient);

        return new SoftConstraintParameters(
            angularFrequency / firstCoefficient,
            secondCoefficient * thirdCoefficient,
            thirdCoefficient
        );
    }
}
