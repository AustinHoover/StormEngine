package electrosphere.server.physics.terrain.generation.continentphase;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author satellite
 */
class InterpolationDisplay extends JPanel{

    TerrainGenerator parent;

    protected InterpolationDisplay(TerrainGenerator parent){
        this.parent = parent;
    }

    @Override
    public void paint(Graphics g) {
        int width = parent.continentPhaseDimension;
        if(parent.displayToggle == 0) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < width; y++) {
                    if (parent.mountainParsed[x][y] > TerrainGenerator.MOUNTAIN_THRESHOLD - 1) {
                        g.setColor(new Color((int) (parent.elevation[x][y] / 100.0 * 254 * (parent.brightness / 100.0)), 1, 1));
                    } else if (parent.oceanParsed[x][y] > TerrainGenerator.OCEAN_THRESHOLD - 1) {
                        g.setColor(
                                new Color(
                                        1,
                                        (int) ((parent.elevation[x][y] + 50) / 100.0 * 254 * (parent.brightness / 100.0)),
                                        (int) ((parent.elevation[x][y] + 50) / 100.0 * 254 * (parent.brightness / 100.0))
                                )
                        );
                    } else {
                        g.setColor(new Color(1, (int) (parent.elevation[x][y] / 100.0 * 254 * (parent.brightness / 100.0)), 1));
                    }
                    g.fillRect(x * 2 + 25, y * 2 + 25, 2, 2);
                }
            }
        } else if(parent.displayToggle == 1) {
            for (int x = 0; x < parent.continentPhaseDimension * TerrainGenerator.EROSION_INTERPOLATION_RATIO; x++) {
                for (int y = 0; y < parent.continentPhaseDimension * TerrainGenerator.EROSION_INTERPOLATION_RATIO; y++) {
                    if (parent.erosionHeightmap[x][y] > TerrainGenerator.MOUNTAIN_THRESHOLD - 1) {
                        float color = Math.max(0,Math.min(parent.erosionHeightmap[x][y],100));
                        g.setColor(new Color((int) (color / 100.0 * 254 * (parent.brightness / 100.0)), 1, 1));
                    } else if (parent.erosionHeightmap[x][y] < TerrainGenerator.OCEAN_THRESHOLD - 1) {
                        float color = Math.max(0,Math.min(parent.erosionHeightmap[x][y],100));
                        g.setColor(
                                new Color(
                                        1,
                                        (int) (color / 100.0 * 254 * (parent.brightness / 100.0)),
                                        (int) (color / 100.0 * 254 * (parent.brightness / 100.0))
                                )
                        );
                    } else {
                        float color = Math.max(0,Math.min(parent.erosionHeightmap[x][y],100));
                        g.setColor(new Color(1, (int) (color / 100.0 * 254 * (parent.brightness / 100.0)), 1));
                    }
                    g.fillRect(x + 25, y + 25, 1, 1);
                }
            }
        } else if(parent.displayToggle == 2){
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < width; y++) {
                    if (parent.precipitationChart[x][y] > 0) {
                        g.setColor(
                                new Color(
                                        1,
                                        (int) ((parent.elevation[x][y]) / 100.0 * 254 * (parent.brightness / 100.0)),
                                        (int) ((parent.elevation[x][y]) / 100.0 * 254 * (parent.brightness / 100.0))
                                )
                        );
                    } else {
                        g.setColor(new Color((int) (parent.elevation[x][y] / 100.0 * 254 * (parent.brightness / 100.0)), 1, 1));
                    }
                    g.fillRect(x * 2 + 25, y * 2 + 25, 2, 2);
                }
            }
        } else if(parent.displayToggle == 3){
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < width; y++) {
//                    if (TerrainInterpolator.precipitation_Chart[x][y] > 0) {
                        g.setColor(
                                new Color(
                                        (int) ((parent.temperatureChart[x][y]) / 100.0 * 254 * (parent.brightness / 100.0)),
                                        (int) ((parent.elevation[x][y]) / 100.0 * 254 * (parent.brightness / 100.0)),
                                        1
                                )
                        );
//                    } else {
//                        g.setColor(new Color((int) (TerrainInterpolator.elevation[x][y] / 100.0 * 254 * (TerrainInterpolator.brightness / 100.0)), 1, 1));
//                    }
                    g.fillRect(x * 2 + 25, y * 2 + 25, 2, 2);
                }
            }
        } else if(parent.displayToggle == 4){
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < width; y++) {
                    if (parent.climateCategory[x][y] == 0) {
                        g.setColor(Color.BLUE);
//                        g.setColor(
//                                new Color(
//                                        1,
//                                        (int) ((TerrainInterpolator.elevation[x][y]) / 100.0 * 254 * (TerrainInterpolator.brightness / 100.0)),
//                                        (int) ((TerrainInterpolator.elevation[x][y]) / 100.0 * 254 * (TerrainInterpolator.brightness / 100.0))
//                                )
//                        );
                    } else if(parent.climateCategory[x][y] == 1){
                        g.setColor(Color.RED);
//                        g.setColor(
//                                new Color(
//                                        1,
//                                        (int) ((TerrainInterpolator.elevation[x][y]) / 100.0 * 254 * (TerrainInterpolator.brightness / 100.0)),
//                                        1
//                                )
//                        );
                    } else if(parent.climateCategory[x][y] == 2){
                        g.setColor(Color.GREEN);
//                        g.setColor(
//                                new Color(
//                                        1,
//                                        (int) ((TerrainInterpolator.elevation[x][y]) / 100.0 * 254 * (TerrainInterpolator.brightness / 100.0)),
//                                        1
//                                )
//                        );
                    } else if(parent.climateCategory[x][y] == 3){
                        g.setColor(Color.YELLOW);
//                        g.setColor(
//                                new Color(
//                                        1,
//                                        1,
//                                        (int) ((TerrainInterpolator.elevation[x][y]) / 100.0 * 254 * (TerrainInterpolator.brightness / 100.0))
//                                )
//                        );
                    } else if(parent.climateCategory[x][y] == 4){
                        g.setColor(
                                new Color(
                                        (int) ((parent.elevation[x][y]) / 100.0 * 254 * (parent.brightness / 100.0)),
                                        (int) ((parent.elevation[x][y]) / 100.0 * 254 * (parent.brightness / 100.0)),
                                        1
                                )
                        );
                        g.setColor(Color.ORANGE);
                    } else if(parent.climateCategory[x][y] == 5){
                        g.setColor(
                                new Color(
                                        (int) ((parent.elevation[x][y]) / 100.0 * 254 * (parent.brightness / 100.0)),
                                        1,
                                        (int) ((parent.elevation[x][y]) / 100.0 * 254 * (parent.brightness / 100.0))
                                )
                        );
                        g.setColor(Color.BLACK);
                    } else if(parent.climateCategory[x][y] == 6){
                        g.setColor(
                                new Color(
                                        (int) ((parent.elevation[x][y]) / 100.0 * 254 * (parent.brightness / 100.0)),
                                        (int) ((parent.elevation[x][y]) / 100.0 * 254 * (parent.brightness / 100.0)),
                                        (int) ((parent.elevation[x][y]) / 100.0 * 254 * (parent.brightness / 100.0))
                                )
                        );
                    } else {
                        g.setColor(new Color((int) (parent.elevation[x][y] / 100.0 * 254 * (parent.brightness / 100.0)), 1, 1));
                    }
                    g.fillRect(x * 2 + 25, y * 2 + 25, 2, 2);
                }
            }
        } else if(parent.displayToggle == 5){
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < width; y++) {
                    if (parent.continentIdField[x][y] > 8) {
                        g.setColor(Color.PINK);
                    } else if (parent.continentIdField[x][y] > 7) {
                        g.setColor(Color.DARK_GRAY);
                    } else if (parent.continentIdField[x][y] > 6) {
                        g.setColor(Color.CYAN);
                    } else if (parent.continentIdField[x][y] > 5) {
                        g.setColor(Color.GRAY);
                    } else if (parent.continentIdField[x][y] > 4) {
                        g.setColor(Color.orange);
                    } else if (parent.continentIdField[x][y] > 3) {
                        g.setColor(Color.green);
                    } else if (parent.continentIdField[x][y] > 2) {
                        g.setColor(Color.yellow);
                    } else if (parent.continentIdField[x][y] > 1) {
                        g.setColor(Color.blue);
                    } else if (parent.continentIdField[x][y] > 0) {
                        g.setColor(Color.red);
                    } else {
                        g.setColor(Color.BLACK);
                    }
                    g.fillRect(x * 2 + 25, y * 2 + 25, 2, 2);
                }
            }
        } else if (parent.displayToggle == 6) {
            Continent current = parent.continents.get(parent.current_Continent);
            g.drawString("dim_x: " + current.dim_x, 20, 20);
            g.drawString("dim_y: " + current.dim_y, 20, 30);
            for (int x = 0; x < current.dim_x; x++) {
                for (int y = 0; y < current.dim_y; y++) {
                    if (current.elevation[x][y] > 10) {
                        g.setColor(new Color(1, (int) (current.elevation[x][y] / 100.0 * 254 * (parent.brightness / 100.0)), 1));
                        g.fillRect(50 + x * 2, 50 + y * 2, 2, 2);
                    }
                }
            }
        } else if (parent.displayToggle == 7){
            // Continent current = parent.continents.get(parent.current_Continent);
            // for(int x = 0; x < Region.REGION_DIMENSION; x++){
            //     for(int y = 0; y < Region.REGION_DIMENSION; y++){
            //         if(current.regions[parent.current_Region_X][parent.current_Region_Y].chart_Drainage[x][y] > 0){
            //             g.setColor(Color.BLUE);
            //         } else {
            //             g.setColor(Color.BLACK);
            //         }
            //         g.fillRect(50 + x * 2, 50 + y * 2, 2, 2);
            //     }
            // }
        }
//        if(TerrainInterpolator.display_toggle == 0) {
//            g.drawString("Elevation Raws", 10, 10);
//            for (int x = 0; x < 100; x++) {
//                for (int y = 0; y < 100; y++) {
//                    g.setColor(new Color(1, (int) (TerrainInterpolator.elevation[x][y] / 100.0 * 254 * (TerrainInterpolator.brightness / 100.0)), 1));
//                    g.fillRect(x * 2 + 25, y * 2 + 25, 2, 2);
//                }
//            }
//        } else if(TerrainInterpolator.display_toggle == 1) {
//            g.drawString("Parsed Mountains", 10, 10);
//            for (int x = 0; x < 100; x++) {
//                for (int y = 0; y < 100; y++) {
//                    if(TerrainInterpolator.mountain_parsed[x][y] > TerrainInterpolator.mountain_Threshold - 1){
//                        g.setColor(new Color((int) (TerrainInterpolator.elevation[x][y] / 100.0 * 254 * (TerrainInterpolator.brightness / 100.0)), 1, 1));
//                    } else {
//                        g.setColor(new Color(1, (int) (TerrainInterpolator.elevation[x][y] / 100.0 * 254 * (TerrainInterpolator.brightness / 100.0)), 1));
//                    }
//                    g.fillRect(x * 2 + 25, y * 2 + 25, 2, 2);
//                }
//            }
//        } else if(TerrainInterpolator.display_toggle == 2){
//            g.drawString("Oceans", 10, 10);
//        } else if(TerrainInterpolator.display_toggle == 3){
//            g.drawString("Continents", 10, 10);
//        } else if(TerrainInterpolator.display_toggle == 4){
//            g.drawString("Temperatures", 10, 10);
//        }
    }
}
