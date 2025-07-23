package electrosphere.server.physics.terrain.generation.continentphase;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
/**
 *
 * @author awhoove
 */
class Utilities {
    
    static long seed = 0;
    
    static Random rand = new Random(seed);
    
    public static void seed_Random_Functions(long seed){
        rand = new Random(seed);
    }
    
    //Finds midpoint of cluster of arbitrary points.
    public static Point average_Points(Point[] objects){
        Point rVal = null;
        int x = 0;
        int y = 0;
        for(int i = 0; i < objects.length; i++){
            x = x + objects[i].x;
            y = y + objects[i].y;
        }
        x = x / objects.length;
        y = y / objects.length;
        rVal = new Point(x,y);
        return rVal;
    }
    public static void sleep(int milliseconds){
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException ex) {
            System.out.println("Sleep failed lol.");
        }
    }
    public static int random_Integer(int Min, int Max, Random rand){
        return Min + (int)(rand.nextDouble() * ((Max - Min) + 1));
    }
    public static int random_Range_Distribution_Even(Random rand, int ... range){
        if(range.length % 2 != 0){
            System.out.println("Invalid number of parameters for range in a function call to \"random_Range_Distribution_Even\"!");
            return -1;
        } else {
            int total = 0;
            int i = 0;
            while(i < range.length/2 + 1){
                total = total + range[i+1] - range[i] + 1;
                i=i+2;
            }
            int temp = random_Integer(1,total,rand);
            int incrementer = 0;
            i = 0;
            while(incrementer + range[i+1] - range[i] + 1 < temp){
                incrementer = incrementer + range[i+1] - range[i] + 1;
                i=i+2;
            }
            return range[i] + (temp - incrementer) - 1;
        }
    }
    public static float distance_Between_Points(Point point_One, Point point_Two){
        float rVal = 0.0f;
        rVal = (float)Math.sqrt(Math.pow(point_One.y - point_Two.y, 2)    +    Math.pow(point_One.x - point_Two.x, 2));
        return rVal;
    }
    public static float angle_Between_Points(Point p1, Point p2){
        float rVal = 0.0f;
        rVal = (float)Math.atan2(p1.y - p2.y, p1.x - p2.x);
        if(rVal < 0){
            rVal = rVal + (float)(Math.PI * 2);
        }
        return rVal;
    }
    
    
    public static Point centerpoint_Of_Circle_From_Three_Points(Point p1, Point p2, Point p3){
        // Point rVal = null;
        
        final double offset = Math.pow(p2.x, 2) + Math.pow(p2.y, 2);
        final double bc = (Math.pow(p1.x, 2) + Math.pow(p1.y, 2) - offset) / 2.0;
        final double cd = (offset - Math.pow(p3.x, 2) - Math.pow(p3.y, 2)) / 2.0;
        final double det = (p1.x - p2.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p2.y);
        if (Math.abs(det) < 0.000001) {
            return null;
        }
        final double idet = 1 / det;

        final double centerx = (bc * (p2.y - p3.y) - cd * (p1.y - p2.y)) * idet;
        final double centery = (cd * (p1.x - p2.x) - bc * (p2.x - p3.x)) * idet;
        
        return new Point((int)centerx,(int)centery);
        
        /*
        float slope_1 = (p2.y - p1.y) / (p2.x - p1.x);
        float slope_2 = (p3.y - p2.y) / (p3.x - p2.x);
        //http://paulbourke.net/geometry/circlesphere/
        float intersect_X = (slope_1*slope_2*(p1.y-p3.y)+slope_2*(p1.x+p2.x)-slope_1*(p2.x+p3.x))/(2*(slope_2-slope_1));
        float intersect_Y = -(1/slope_1)*(intersect_X - (p2.x+p3.x)/2)+(p2.y+p3.y)/2;
        rVal = new Point((int)intersect_X, (int)intersect_Y);
        return rVal;
        */
    }
    public static String pull_Random_String_From_File(File source){
        String rVal = "";
        int line_To_Go_To;
        try (BufferedReader reader = new BufferedReader(new FileReader(source));){
            line_To_Go_To = random_Integer(1,Integer.parseInt(reader.readLine()),rand);
            int i = 0;
            while(i<line_To_Go_To - 1){
                reader.readLine();
                i++;
                if(i > 5000){
                    break;
                }
            }
            rVal = reader.readLine();
        } catch (FileNotFoundException ex) {
            System.out.println("Utilities/pull_Random_String_From_File failed to read from "+source+" (File not found).");
        } catch (IOException ex){
            System.out.println("Utilities/pull_Random_String_From_File failed to read from "+source+" (IOException).");
        }
        return rVal;
    }
    
    public static String pull_Ordered_String_From_File(File source, int dest){
        String rVal = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(source));){
            int i = 0;
            while(i<dest){
                reader.readLine();
                i++;
            }
            rVal = reader.readLine();
        } catch (FileNotFoundException ex) {
            System.out.println("Utilities/pull_Random_String_From_File failed to read from "+source+" (File not found).");
        } catch (IOException ex){
            System.out.println("Utilities/pull_Random_String_From_File failed to read from "+source+" (IOException).");
        }
        return rVal;
    }
    
    public static int number_Of_Char_In_String(String s, char c){
        int rVal = 0;
        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i)==c){
                rVal++;
            }
        }
        return rVal;
    }
    
    public static int get_Position_Of_Next_Instance_Of_Char_In_String(String s, char c){
        int rVal = 0;
        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i)==c){
                rVal = i;
                break;
            }
        }
        return rVal;
    }
    
    public static int get_Position_Of_Nth_Instance_Of_Char_In_String(String s, char c, int n){
        int rVal = 0;
        int itterator = n;
        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i)==c){
                if(itterator>0){
                    itterator--;
                } else {
                    rVal = i;
                    break;
                }
            }
        }
        return rVal;
    }
    
    public static float[] linearize_Float_Array_Column_Major(float[][] in){
        float[] rVal = new float[in[0].length * in.length];
        for(int x = 0; x < in.length; x++){
            for(int y = 0; y < in[0].length; y++){
                rVal[x*in[0].length + y] = in[x][y];
            }
        }
        return rVal;
    }
    public static void print_Linearized_Array(float[] in){
        for(int i = 0; i < in.length; i++){
            System.out.println(in[i]);
        }
    }
    public static float[] linearize_Float_Array_Row_Major(float[][] in){
        float[] rVal = new float[in[0].length * in.length];
        for(int y = 0; y < in[0].length; y++){
            for(int x = 0; x < in.length; x++){
                rVal[x*in[0].length + y] = in[x][y];
            }
        }
        return rVal;
    }
    public static String string_To_First_Space(String s){
        String rVal = null;
        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i)==' '){
                rVal = s.substring(0, i);
                break;
            }
        }
        if(rVal == null){
            rVal = s;
        }
        return rVal;
    }
    public static int position_To_First_Space(String s){
        int rVal = -1;
        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i)==' '){
                rVal = i;
                break;
            }
        }
        return rVal;
    }
    public static int number_Of_Spaces_In_String(String s){
        int rVal = 0;
        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i)==' '){
                rVal++;
            }
        }
        return rVal;
    }
    public static String get_Face_From_OBJ_Format(String s){
        String rVal = "";
        int counter = 0;
        for(int i = 0; i < s.length(); i++){
            counter = i;
            if(s.charAt(i)=='/'){
                break;
            }
        }
        if(counter+1 == s.length()){
            counter++;
        }
        rVal = s.substring(0, counter);
        return rVal;
    }
}
