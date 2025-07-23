package electrosphere.server.physics.terrain.generation.continentphase;

/**
 * Int based 2 dimension vector
 */
class Vector {
    public int x;
    public int y;
    protected Vector(){

    }
    protected Vector(int x, int y){
        this.x = x;
        this.y = y;
    }
}
