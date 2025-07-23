package electrosphere.server.physics.terrain.generation.continentphase;

/**
 *
 * @author satellite
 */
class Hotspot {

    int x;
    int y;
    int life_current;
    int life_max;
    int magnitude_current;
    int magnitude_max;
    TectonicSimulation parent;
    
    
    protected Hotspot(int x, int y, int life_max, int magnitude, TectonicSimulation parent) {
        this.x = x;
        this.y = y;
        this.life_current = 0;
        this.life_max = life_max;
        this.magnitude_current = 1;
        this.magnitude_max = magnitude;
        this.parent = parent;
    }

    protected void simulate() {
        if ((1.0f - (Math.abs((life_max / 2) - life_current) / (life_max / 2))) > 0.8f) {
            magnitude_current = magnitude_max;
        } else if ((1.0f - (Math.abs((life_max / 2) - life_current) / (life_max / 2))) > 0.6f) {
            magnitude_current = (int) (0.8f * magnitude_max);
        } else if ((1.0f - (Math.abs((life_max / 2) - life_current) / (life_max / 2))) > 0.4f) {
            magnitude_current = (int) (0.6f * magnitude_max);
        } else if ((1.0f - (Math.abs((life_max / 2) - life_current) / (life_max / 2))) > 0.2f) {
            magnitude_current = (int) (0.4f * magnitude_max);
        } else {
            magnitude_current = (int) (0.2f * magnitude_max);
        }
        //affect asthenosphere heat
        if (magnitude_current == 1) {
            parent.asthenosphereHeat[x][y] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
        } else if (magnitude_current == 2) {
            parent.asthenosphereHeat[x][y] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
            if (x + 1 < parent.DIMENSION) {
                parent.asthenosphereHeat[x + 1][y] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
            }
            if (x - 1 >= 0) {
                parent.asthenosphereHeat[x - 1][y] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
            }
            if (y + 1 < parent.DIMENSION) {
                parent.asthenosphereHeat[x][y + 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
            }
            if (y - 1 >= 0) {
                parent.asthenosphereHeat[x][y - 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
            }
        } else if (magnitude_current == 3) {
            parent.asthenosphereHeat[x][y] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
            if (x + 1 < parent.DIMENSION) {
                parent.asthenosphereHeat[x + 1][y] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
            }
            if (x - 1 >= 0) {
                parent.asthenosphereHeat[x - 1][y] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
            }
            if (y + 1 < parent.DIMENSION) {
                parent.asthenosphereHeat[x][y + 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                if (x + 1 < parent.DIMENSION) {
                    parent.asthenosphereHeat[x + 1][y + 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                }
                if (x - 1 >= 0) {
                    parent.asthenosphereHeat[x - 1][y + 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                }
            }
            if (y - 1 >= 0) {
                parent.asthenosphereHeat[x][y - 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                if (x + 1 < parent.DIMENSION) {
                    parent.asthenosphereHeat[x + 1][y - 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                }
                if (x - 1 >= 0) {
                    parent.asthenosphereHeat[x - 1][y - 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                }
            }
        } else if (magnitude_current == 4) {
            parent.asthenosphereHeat[x][y] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
            if (x + 1 < parent.DIMENSION) {
                parent.asthenosphereHeat[x + 1][y] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
            }
            if (x - 1 >= 0) {
                parent.asthenosphereHeat[x - 1][y] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
            }
            if (y + 1 < parent.DIMENSION) {
                parent.asthenosphereHeat[x][y + 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                if (x + 1 < parent.DIMENSION) {
                    parent.asthenosphereHeat[x + 1][y + 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                }
                if (x - 1 >= 0) {
                    parent.asthenosphereHeat[x - 1][y + 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                }
            }
            if (y - 1 >= 0) {
                parent.asthenosphereHeat[x][y - 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                if (x + 1 < parent.DIMENSION) {
                    parent.asthenosphereHeat[x + 1][y - 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                }
                if (x - 1 >= 0) {
                    parent.asthenosphereHeat[x - 1][y - 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                }
            }
        } else if (magnitude_current == 5) {
            parent.asthenosphereHeat[x][y] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
            if (x + 1 < parent.DIMENSION) {
                parent.asthenosphereHeat[x + 1][y] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
            }
            if (x - 1 >= 0) {
                parent.asthenosphereHeat[x - 1][y] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
            }
            if (y + 1 < parent.DIMENSION) {
                parent.asthenosphereHeat[x][y + 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                if (x + 1 < parent.DIMENSION) {
                    parent.asthenosphereHeat[x + 1][y + 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                }
                if (x - 1 >= 0) {
                    parent.asthenosphereHeat[x - 1][y + 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                }
            }
            if (y - 1 >= 0) {
                parent.asthenosphereHeat[x][y - 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                if (x + 1 < parent.DIMENSION) {
                    parent.asthenosphereHeat[x + 1][y - 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                }
                if (x - 1 >= 0) {
                    parent.asthenosphereHeat[x - 1][y - 1] = (int) (100.0f - Math.abs((life_max / 2) - life_current) * 100 / (life_max / 2));
                }
            }
        }
        life_current++;
    }
    
    protected void add_to_elevation(int x, int y, int magnitude){
        parent.elevation[x][y] = parent.elevation[x][y] + magnitude;
        if(parent.elevation[x][y] > 100){
            parent.elevation[x][y] = 100;
        }
    }
}
