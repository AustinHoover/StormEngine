package electrosphere.client.ui.components.imgui;

import org.ode4j.ode.DBody;
import org.ode4j.ode.DBox;
import org.ode4j.ode.DCapsule;
import org.ode4j.ode.DCylinder;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;

import electrosphere.data.entity.collidable.CollidableTemplate;
import imgui.ImGui;

/**
 * Block for editing collidable data
 */
public class CollidableEditBlock {

    /**
     * The minimum offset
     */
    static final float MIN_OFFSET = -10;

    /**
     * The maximum offset
     */
    static final float MAX_OFFSET = 10;

    /**
     * Min scale
     */
    static final float MIN_SCALE = 0.001f;

    /**
     * Max scale
     */
    static final float MAX_SCALE = 10f;

    /**
     * Minimum mass value
     */
    static final float MIN_MASS = 0;

    /**
     * Maximum mass value
     */
    static final float MAX_MASS = 1.0f;

    /**
     * Storage for the modified scale of the collidable
     */
    static float[] scale = new float[3];

    /**
     * Storage for the modified offset of the collidable
     */
    static float[] offset = new float[3];

    /**
     * Radius slider
     */
    static float[] radius = new float[1];

    /**
     * Length slider
     */
    static float[] length = new float[1];

    /**
     * Mass slider
     */
    static float[] mass = new float[1];
    
    /**
     * Draws collidable editing controls
     * @param physicsBody The body to edit
     * @param template The template data to edit
     */
    public static void drawCollidableEdit(DBody physicsBody, CollidableTemplate template){
        if(physicsBody != null && physicsBody.getFirstGeom() != null && ImGui.collapsingHeader("Modify")){
            DGeom geom = physicsBody.getFirstGeom();
            if(geom instanceof DBox){
                DBox box = (DBox)geom;
                if(ImGui.sliderFloat3("Offset", offset, MIN_OFFSET, MAX_OFFSET)){
                    box.setOffsetPosition(offset[0], offset[1], offset[2]);
                    template.setOffsetX(offset[0]);
                    template.setOffsetY(offset[1]);
                    template.setOffsetZ(offset[2]);
                }
                if(ImGui.sliderFloat3("Scale",scale,MIN_SCALE,MAX_SCALE)){
                    box.setLengths(scale[0], scale[1], scale[2]);
                    template.setDimension1(scale[0]);
                    template.setDimension2(scale[1]);
                    template.setDimension3(scale[2]);
                }
                if(physicsBody.getMass() instanceof DMass && ImGui.sliderFloat("Mass",mass,MIN_MASS,MAX_MASS)){
                    DMass massObj = (DMass)physicsBody.getMass();
                    float adjusted = (float)Math.log(mass[0] + 1);
                    massObj.setMass(adjusted);
                }
            } else if(geom instanceof DCylinder){
                DCylinder cylinder = (DCylinder)geom;
                if(ImGui.sliderFloat3("Offset", offset, MIN_OFFSET, MAX_OFFSET)){
                    cylinder.setOffsetPosition(offset[0], offset[1], offset[2]);
                    template.setOffsetX(offset[0]);
                    template.setOffsetY(offset[1]);
                    template.setOffsetZ(offset[2]);
                }
                if(ImGui.sliderFloat("Radius",radius,MIN_SCALE,MAX_SCALE)){
                    cylinder.setParams(radius[0], cylinder.getLength());
                    template.setDimension1(radius[0]);
                }
                if(ImGui.sliderFloat("Length",length,MIN_SCALE,MAX_SCALE)){
                    cylinder.setParams(cylinder.getRadius(), length[0]);
                    template.setDimension2(length[0]);
                }
                if(physicsBody.getMass() instanceof DMass && ImGui.sliderFloat("Mass",mass,MIN_MASS,MAX_MASS)){
                    DMass massObj = (DMass)physicsBody.getMass();
                    float adjusted = (float)Math.log(mass[0] + 1);
                    massObj.setMass(adjusted);
                }
            } else if(geom instanceof DCapsule){
                DCapsule cylinder = (DCapsule)geom;
                if(ImGui.sliderFloat3("Offset", offset, MIN_OFFSET, MAX_OFFSET)){
                    cylinder.setOffsetPosition(offset[0], offset[1], offset[2]);
                    template.setOffsetX(offset[0]);
                    template.setOffsetY(offset[1]);
                    template.setOffsetZ(offset[2]);
                    geom.enable();
                }
                if(ImGui.sliderFloat("Radius",radius,MIN_SCALE,MAX_SCALE)){
                    cylinder.setParams(radius[0], cylinder.getLength());
                    template.setDimension1(radius[0]);
                    template.setDimension3(radius[0]);
                    geom.enable();
                }
                if(ImGui.sliderFloat("Length",length,MIN_SCALE,MAX_SCALE)){
                    cylinder.setParams(cylinder.getRadius(), length[0]);
                    template.setDimension2(length[0]);
                    geom.enable();
                }
                if(physicsBody.getMass() instanceof DMass && ImGui.sliderFloat("Mass",mass,MIN_MASS,MAX_MASS)){
                    DMass massObj = (DMass)physicsBody.getMass();
                    float adjusted = (float)Math.log(mass[0] + 1);
                    massObj.setMass(adjusted);
                    geom.enable();
                }
            } else {
                throw new Error("Unsupported geom type! " + geom);
            }
        }
    }

}
