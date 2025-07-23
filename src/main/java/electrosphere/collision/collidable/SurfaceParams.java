package electrosphere.collision.collidable;

import org.ode4j.ode.OdeConstants;

/**
 * The surface params for the collidable
 */
public class SurfaceParams {
    
    /**
     * <p> The mode flags for the surface </p>
     * <p> This must always be set. This is a combination of one or more of the following flags. </p>
     * <p>
     * Possible values:
     * <ul>
     * <li> dContactMu2 - If not set, use mu for both friction directions. If set, use mu for friction direction 1, use mu2 for friction direction 2. </li>
     * <li> dContactFDir1 - If set, take fdir1 as friction direction 1, otherwise automatically compute friction direction 1 to be perpendicular to the contact normal (in which case its resulting orientation is unpredictable).  </li>
     * <li> dContactBounce - If set, the contact surface is bouncy, in other words the bodies will bounce off each other. The exact amount of bouncyness is controlled by the bounce parameter. </li>
     * <li> dContactSoftERP - If set, the error reduction parameter of the contact normal can be set with the soft_erp parameter. This is useful to make surfaces soft. </li>
     * <li> dContactSoftCFM  - If set, the constraint force mixing parameter of the contact normal can be set with the soft_cfm parameter. This is useful to make surfaces soft. </li>
     * <li> dContactMotion1 - If set, the contact surface is assumed to be moving independently of the motion of the bodies. This is kind of like a conveyor belt running over the surface. When this flag is set, motion1 defines the surface velocity in friction direction 1. </li>
     * <li> dContactMotion2 - The same thing as above, but for friction direction 2. </li>
     * <li> dContactMotionN - The same thing as above, but along the contact normal. </li>
     * <li> dContactSlip1 - Force-dependent-slip (FDS) in friction direction 1. </li>
     * <li> dContactSlip2 - Force-dependent-slip (FDS) in friction direction 2. </li>
     * <li> dContactRolling - Enables rolling/spinning friction. </li>
     * <li> dContactApprox1_1 - Use the friction pyramid approximation for friction direction 1. If this is not specified then the constant-force-limit approximation is used (and mu is a force limit). </li>
     * <li> dContactApprox1_2 - Use the friction pyramid approximation for friction direction 2. If this is not specified then the constant-force-limit approximation is used (and mu is a force limit). </li>
     * <li> dContactApprox1_N - Use the friction pyramid approximation for spinning (rolling around normal).  </li>
     * <li> dContactApprox1 - Equivalent to dContactApprox1_1, dContactApprox1_2 and dContactApprox1_N. </li>
     * </ul>
     * </p>
     */
    int mode;

    /**
     * <p> Coulomb friction coefficient </p>
     * <p> Ranges [0,infinity) </p>
     * <p> 0 is a frictionless contact, dInfinity results in a contact that never slips. </p>
     * <p> Note that frictionless contacts are less time consume to compute than ones with friction. </p>
     * <p> <b> This must always be set. </b> </p>
     */
    Double mu;
    
    /**
     * <p> Rolling friction coefficient around direction 1. </p>
     */
    Double rho;

    /**
     * <p> Rolling friction coefficient around direction 2. </p>
     */
    Double rho2;

    /**
     * <p> Rolling friction coefficient around the normal direction. </p>
     */
    Double rhoN;

    /**
     * <p> Restitution parameter </p>
     * <p> Ranges (0,1) </p>
     * <p> 0 means the surfaces are not bouncy at all. 1 is maximum bounciness. </p>
     * <p> <b> Note that mode must be set with dContactBounce. </b> </p>
     */
    Double bounce;

    /**
     * <p> Minimum velocity to trigger a bounce </p>
     * <p> All velocities below this threshold will effectively have bounce parameter of 0. </p>
     * <p> <b> Note that mode must be set with ??put flat here??. </b> </p>
     */
    Double bounceVel;

    /**
     * <p> Error Reduction Parameter </p>
     * <p> How much of the penetration is corrected per time step </p>
     * <p> <b> Note that mode must be set with dContactSoftERP. </b> </p>
     */
    Double softErp;

    /**
     * <p> Constraint Force Mixing </p>
     * <p> How soft or spongy the contact is </p>
     * <p> <b> Note that mode must be set with dContactSoftCFM. </b> </p>
     */
    Double softCfm;

    /**
     * Constructor
     */
    public SurfaceParams(){
        mode = OdeConstants.dContactApprox1 & OdeConstants.dContactRolling & OdeConstants.dContactBounce;
        mu = 0.0001;
        rho = 10.0;
        rho2 = 10.0;
        rhoN = 10.0;
        bounce = 0.001;
        bounceVel = 100.0;
        softErp = 0.2;
        softCfm = 1e-5;
    }

    /**
     * Gets the mode for the surface
     * @return The mode
     */
    public int getMode(){
        return mode;
    }

    /**
     * Gets the mu for the surface
     * @return The mu
     */
    public Double getMu(){
        return mu;
    }

    /**
     * Gets the rho for the surface;
     * @return The rho
     */
    public Double getRho(){
        return rho;
    }

    /**
     * Gets the rho2 for the surface;
     * @return The rho2
     */
    public Double getRho2(){
        return rho2;
    }

    /**
     * Gets the rhoN for the surface;
     * @return The rhoN
     */
    public Double getRhoN(){
        return rhoN;
    }

    /**
     * Gets the bounce for the surface;
     * @return The bounce
     */
    public Double getBounce(){
        return bounce;
    }

    /**
     * Gets the bounce minimum velocity for the surface;
     * @return The bounce minimum velocity
     */
    public Double getBounceVel(){
        return bounceVel;
    }

    /**
     * Gets the error reduction parameter
     * @return The error reduction parameter
     */
    public Double getSoftErp(){
        return softErp;
    }

    /**
     * Gets the constraining force mixing
     * @return The constraining force mixing
     */
    public Double getSoftCfm(){
        return softCfm;
    }

    /**
     * Sets the rolling friction of the surface params
     * @param friction The rolling friction
     */
    public void setRollingFriction(double friction){
        this.rho = this.rho2 = this.rhoN = friction;
    }

    /**
     * Sets the lienar friction
     * @param linearFriction The linear friction
     */
    public void setLinearFriction(double linearFriction){
        this.mu = linearFriction;
    }

    /**
     * Sets the error reduction parameter
     * @param softErp The error reduction parameter
     */
    public void setSoftErp(double softErp){
        this.softErp = softErp;
    }

    /**
     * Sets the constraint force mixing
     * @param softCfm The constraint force mixing
     */
    public void setSoftCfm(double softCfm){
        this.softCfm = softCfm;
    }

}
