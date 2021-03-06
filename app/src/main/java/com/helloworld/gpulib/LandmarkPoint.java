package com.helloworld.gpulib;

import android.graphics.Point;

import java.util.ArrayList;

/**
 * Created by Gicheon on 2017. 6. 2..
 */

public class LandmarkPoint {

    public static ArrayList<Point> landmark = new ArrayList<Point>(68);

    LandmarkPoint(){
        /*for(int i=0; i<68; i++){
            landmark.add(new Point((int)(dArray[i]*2), (int)(dArray[i+68]*2)));
        }*/
    }

    // get landmark point
    public static ArrayList<Point> getLandmark(){
        return landmark;
    }


    // set landmark point
    public static void setLandmark(ArrayList<Point> ldmkPoint) {
        for(int i=0; i<ldmkPoint.size(); i++){
            landmark.set(i, new Point((int)(ldmkPoint.get(i).x * 1.5), (int)(ldmkPoint.get(i).y * 1.5)));
            System.out.println(landmark.get(i));
        }
    }

    double[] dArray = new double[]{
            171.9058638092084,
            170.6418733420462,
            173.2819168438913,
            178.8769832323965,
            190.4387429392847,
            208.992090846121,
            229.2985042529168,
            252.7022526987575,
            280.2919725987762,
            308.4561112775668,
            335.9187282881255,
            359.6791187721923,
            377.5408504013046,
            389.4019141900405,
            396.8571712744464,
             402.004520430836,
             405.1807551546782,
             191.1517712208668,
             208.0077097842049,
             227.4081638867683,
             246.0421826044534,
             263.0192268416986,
             306.0893839696733,
             325.779803813271,
             345.2636434923756,
             364.8478672555654,
             380.0334625606957,
             284.3019435538187,
             282.3367541284452,
             280.4757238800036,
             278.6688548550513,
             256.4582066652812,
             267.498334803405,
             278.6885840423415,
             291.0999656256849,
             302.7043614407651,
             214.1785412458856,
             227.0959636829287,
             242.265342511916,
             254.4264880025274,
             240.4491976941212,
             225.1708419884239,
             313.7525979669304,
             327.770710075221,
             343.016480137416,
             355.2855438313865,
             343.3895224403558,
             328.3474531795865,
             233.5963836410393,
             250.4528018880904,
             266.9085211174612,
             278.1706331375449,
             291.1725967843167,
             308.0938628666037,
             325.7898254231013,
             307.2830879370407,
             290.4599434944299,
             276.659159519554,
             264.4167202118221,
             248.6480248070091,
             240.459601497513,
             266.658810977734,
             278.1028643586124,
             291.3719054138863,
             317.8413456952541,
             290.7485911417878,
             277.4586099668841,
             265.9052184589825,
             201.5854961985201,
             235.36513264159,
             270.5116810753984,
             305.0842963160991,
             336.9858544903663,
             364.8603630005152,
             387.1305369555337,
             403.0687758035102,
             407.2458427011069,
             403.1091643882415,
             387.5803333018497,
             367.3932688465667,
             342.8274316606918,
             314.1712473882218,
             282.8745847792305,
             251.1897410177252,
             219.5686784191728,
             175.7869835372595,
             165.1187543888361,
             163.9743444120993,
             169.2862563736265,
             178.5146345979626,
             180.3755636868374,
             173.5931139214773,
             171.5732802586801,
             174.3928935932236,
             185.7419071135809,
             206.4015249382583,
             226.0235519560663,
             245.5578295630113,
             265.9530603326122,
             282.4221939436579,
             286.5729963238983,
             290.4006133667262,
             287.7406219639531,
             284.6161472057126,
             206.496382094304,
             200.6688139365546,
             202.1113406715585,
             211.4953625550573,
             214.2145274769144,
             213.1885181610568,
             215.1683569479146,
             207.3203802910246,
             208.1476763613168,
             215.4295857421528,
             220.1804329379926,
             219.6040724467201,
             320.9096281836791,
             314.8293059475643,
             311.7908922234341,
             315.3639148454012,
             313.1145219218308,
             317.7688014244685,
             324.57713544643,
             337.5134570980223,
             342.4037450481955,
             343.3727219587736,
             341.8812084847225,
             335.8843039209954,
             322.1972099617915,
             323.2420661439707,
             324.8647407838427,
             323.8714143148033,
             325.4683611505075,
             326.3568929991825,
             327.3776825845833,
             325.9344668527129
    };

}
