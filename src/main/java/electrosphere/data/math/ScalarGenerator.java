package electrosphere.data.math;

/**
 * A defintion of a function that generates a scalar
 */
public class ScalarGenerator {
    
    /**
     * The constant value
     */
    float constant;

    /**
     * The linear value
     */
    float linear;

    /**
     * The quadratic value
     */
    float quadratic;

    public float getConstant() {
        return constant;
    }

    public void setConstant(float constant) {
        this.constant = constant;
    }

    public float getLinear() {
        return linear;
    }

    public void setLinear(float linear) {
        this.linear = linear;
    }

    public float getQuadratic() {
        return quadratic;
    }

    public void setQuadratic(float quadratic) {
        this.quadratic = quadratic;
    }

    
    /**
     * Calculates the scalar's value given an input t
     * @param t The input
     * @return The scalar's value at t
     */
    public double calculate(double t){
        return constant + t * linear + t * t * quadratic;
    }
    

}
