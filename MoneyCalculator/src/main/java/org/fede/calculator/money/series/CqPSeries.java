/*
 * Copyright (C) 2014 fede
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fede.calculator.money.series;

import java.math.BigDecimal;

/**
 *
 * @author fede
 */
public final class CqPSeries extends ArrayIndexSeries {
        private static final BigDecimal[] TABLE = {
        new BigDecimal("48.45688507"), new BigDecimal("48.81083389"), new BigDecimal("49.17781936"), new BigDecimal("49.69188897"), new BigDecimal("50.33120549"), new BigDecimal("50.69324133"), new BigDecimal("50.85631830"), new BigDecimal("50.86447328"), new BigDecimal("51.28363930"), new BigDecimal("51.57353754"), new BigDecimal("51.60302006"), new BigDecimal("51.59624690"),
        new BigDecimal("51.64820092"), new BigDecimal("51.64637738"), new BigDecimal("51.71836774"), new BigDecimal("51.84426025"), new BigDecimal("52.02395299"), new BigDecimal("52.22527908"), new BigDecimal("52.70744233"), new BigDecimal("52.81618673"), new BigDecimal("53.17766758"), new BigDecimal("53.34807271"), new BigDecimal("53.46868714"), new BigDecimal("53.58496357"),
        new BigDecimal("54.25261865"), new BigDecimal("54.25113490"), new BigDecimal("54.00739158"), new BigDecimal("54.25438557"), new BigDecimal("54.26579121"), new BigDecimal("54.15403400"), new BigDecimal("54.37374277"), new BigDecimal("54.24197187"), new BigDecimal("54.33116698"), new BigDecimal("54.51587729"), new BigDecimal("54.39135527"), new BigDecimal("54.44648067"),
        new BigDecimal("54.60970488"), new BigDecimal("54.43224344"), new BigDecimal("54.13858484"), new BigDecimal("54.13941166"), new BigDecimal("54.09112738"), new BigDecimal("54.09253185"), new BigDecimal("54.38581668"), new BigDecimal("54.34369393"), new BigDecimal("54.44236920"), new BigDecimal("54.71680696"), new BigDecimal("54.63182527"), new BigDecimal("54.47605380"),
        new BigDecimal("54.73048921"), new BigDecimal("54.94093302"), new BigDecimal("54.67009704"), new BigDecimal("54.48940759"), new BigDecimal("54.44419275"), new BigDecimal("54.56815978"), new BigDecimal("54.68941982"), new BigDecimal("54.77941909"), new BigDecimal("54.75318724"), new BigDecimal("54.66741270"), new BigDecimal("54.56176039"), new BigDecimal("54.65472717"),
        new BigDecimal("54.99827839"), new BigDecimal("55.19004503"), new BigDecimal("55.12071637"), new BigDecimal("55.12693454"), new BigDecimal("55.08712238"), new BigDecimal("55.19228765"), new BigDecimal("55.36478815"), new BigDecimal("55.37596727"), new BigDecimal("55.35884181"), new BigDecimal("55.15631513"), new BigDecimal("55.02534840"), new BigDecimal("55.01789565"),
        new BigDecimal("55.27725802"), new BigDecimal("55.18845934"), new BigDecimal("54.77409570"), new BigDecimal("54.72045405"), new BigDecimal("54.45103387"), new BigDecimal("54.44772657"), new BigDecimal("54.54892762"), new BigDecimal("54.34359200"), new BigDecimal("54.23532330"), new BigDecimal("54.22683985"), new BigDecimal("54.05543801"), new BigDecimal("54.02182137"),
        new BigDecimal("54.47843234"), new BigDecimal("54.48066363"), new BigDecimal("54.19281546"), new BigDecimal("54.13187963"), new BigDecimal("53.92146980"), new BigDecimal("53.82177516"), new BigDecimal("54.05552862"), new BigDecimal("53.93922954"), new BigDecimal("53.85639985"), new BigDecimal("53.95291178"), new BigDecimal("53.68769368"), new BigDecimal("53.62767529"),
        new BigDecimal("53.67080607"), new BigDecimal("53.54854932"), new BigDecimal("53.65057719"), new BigDecimal("54.00920380"), new BigDecimal("54.04415696"), new BigDecimal("53.65434887"), new BigDecimal("53.47885821"), new BigDecimal("53.28758993"), new BigDecimal("53.24724543"), new BigDecimal("53.01233667"), new BigDecimal("52.83847701"), new BigDecimal("52.79830240"),
        new BigDecimal("54.00876207"), new BigDecimal("55.70489616"), new BigDecimal("57.90829271"), new BigDecimal("63.92417680"), new BigDecimal("66.48801218"), new BigDecimal("68.89622334"), new BigDecimal("71.09308458"), new BigDecimal("72.75805984"), new BigDecimal("73.74084830"), new BigDecimal("73.90202244"), new BigDecimal("74.27828419"), new BigDecimal("74.41737192"),
        new BigDecimal("75.39914101"), new BigDecimal("75.82591834"), new BigDecimal("76.26855258"), new BigDecimal("76.31068665"), new BigDecimal("76.01801345"), new BigDecimal("75.95288686"), new BigDecimal("76.29018593"), new BigDecimal("76.30876117"), new BigDecimal("76.33900256"), new BigDecimal("76.78899893"), new BigDecimal("76.97837571"), new BigDecimal("77.14181512"),
        new BigDecimal("77.46608887"), new BigDecimal("77.54401424"), new BigDecimal("78.00409108"), new BigDecimal("78.67336583"), new BigDecimal("79.24817872"), new BigDecimal("79.69670267"), new BigDecimal("80.06401660"), new BigDecimal("80.33902068"), new BigDecimal("80.84462949"), new BigDecimal("81.16459923"), new BigDecimal("81.16641145"), new BigDecimal("81.84497381"),
        new BigDecimal("83.06108534"), new BigDecimal("83.84679509"), new BigDecimal("85.14287073"), new BigDecimal("85.56036045"), new BigDecimal("86.07435077"), new BigDecimal("86.86266559"), new BigDecimal("87.73490875"), new BigDecimal("88.11819286"), new BigDecimal("89.14300212"), new BigDecimal("89.83957340"), new BigDecimal("90.92339302"), new BigDecimal("91.93563002"),
        new BigDecimal("93.10858810"), new BigDecimal("93.47760099"), new BigDecimal("94.60434751"), new BigDecimal("95.52438792"), new BigDecimal("95.97143944"), new BigDecimal("96.43616009"), new BigDecimal("97.03124717"), new BigDecimal("97.57706457"), new BigDecimal("98.45463112"), new BigDecimal("99.29731248"), new BigDecimal("100.00000000"), new BigDecimal("101.77210724"),
        
        /*2007*/
        new BigDecimal("103.18259740"), new BigDecimal("104.01509547"), new BigDecimal("105.03497220"), new BigDecimal("107.08974927"), new BigDecimal("109.49377193"), new BigDecimal("112.24125310"), new BigDecimal("115.15958546"), new BigDecimal("119.83169121"), new BigDecimal("122.86316457"), new BigDecimal("125.85327848"), new BigDecimal("126.83152654"), new BigDecimal("128.15612487"),
        
        new BigDecimal("129.96545750"), new BigDecimal("133.13592268"), new BigDecimal("137.48126591"), new BigDecimal("142.34105336"), new BigDecimal("144.05034149"), new BigDecimal("146.77908128"), new BigDecimal("148.91144375"), new BigDecimal("150.55430639"), new BigDecimal("152.44273701"), new BigDecimal("154.36550503"), new BigDecimal("155.57259717"), new BigDecimal("156.05693449"),
        new BigDecimal("157.10570291"), new BigDecimal("158.14343910"), new BigDecimal("160.26383352"), new BigDecimal("161.95094117"), new BigDecimal("162.96053841"), new BigDecimal("163.79817635"), new BigDecimal("165.90620013"), new BigDecimal("168.53505649"), new BigDecimal("170.81950046"), new BigDecimal("173.73629738"), new BigDecimal("175.85022647"), new BigDecimal("179.50254135"),
        new BigDecimal("182.47995051"), new BigDecimal("188.46544884"), new BigDecimal("193.99204512"), new BigDecimal("197.68536599"), new BigDecimal("200.77256199"), new BigDecimal("203.99947141"), new BigDecimal("207.52399645"), new BigDecimal("210.60474682"), new BigDecimal("214.08168072"), new BigDecimal("219.70177339"), new BigDecimal("224.07940012"), new BigDecimal("227.81249171"),
        /*2011*/
        new BigDecimal("232.37512966"), new BigDecimal("235.60651680"), new BigDecimal("240.64474082"), new BigDecimal("245.86492153"), new BigDecimal("250.73504644"), new BigDecimal("255.33647468"), new BigDecimal("260.67974052"), new BigDecimal("265.47442336"), new BigDecimal("270.77050328"), new BigDecimal("273.65619385"), new BigDecimal("277.73027068"), new BigDecimal("282.95249305"),
        new BigDecimal("286.94590043"), new BigDecimal("292.10309863"), new BigDecimal("297.92722674"), new BigDecimal("304.92671556"), new BigDecimal("310.06851580"), new BigDecimal("315.04905266"), new BigDecimal("321.75202526"), new BigDecimal("328.41210199"), new BigDecimal("334.00655283"), new BigDecimal("339.09253503"), new BigDecimal("345.60875096"), new BigDecimal("351.86610921"),
        new BigDecimal("358.25632184"), new BigDecimal("364.02916542"), new BigDecimal("369.70395789"), new BigDecimal("375.79927706"), new BigDecimal("381.87885145"), new BigDecimal("389.08714403"), new BigDecimal("398.71969357"), new BigDecimal("406.74923890"), new BigDecimal("414.06054611"), new BigDecimal("425.58740210"), new BigDecimal("437.31358173"), new BigDecimal("448.58877361"),
        /*2014*/
        new BigDecimal("469.16138816"), new BigDecimal("492.34508009"), new BigDecimal("508.56469764"), new BigDecimal("522.71773419"), new BigDecimal("536.75258249"), new BigDecimal("546.35471209"), new BigDecimal("558.57819988") , new BigDecimal("571.34672585")
    };

    public CqPSeries(){
        super(1993, TABLE);
    }



    
}
