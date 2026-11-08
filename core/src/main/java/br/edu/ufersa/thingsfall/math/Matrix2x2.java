package br.edu.ufersa.thingsfall.math;

public class Matrix2x2 {
    // matrix * result = rightHandSide, solve for result using Cramer's rule.
    public static Vector2 solve(double[][] matrix, Vector2 rightHandSide) {
        double determinant = matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
        double inverseDeterminant = determinant != 0.0 ? 1.0 / determinant : 0.0;

        return new Vector2(
            inverseDeterminant * (matrix[1][1] * rightHandSide.x - matrix[0][1] * rightHandSide.y),
            inverseDeterminant * (matrix[0][0] * rightHandSide.y - matrix[1][0] * rightHandSide.x)
        );
    }
}
