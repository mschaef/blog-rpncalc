/* Copyright (c) KSM Technology Partners. All rights reserved.
 *
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (https://opensource.org/licenses/EPL-2.0)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.ksmpartners.rpncalc;

public class App
{
    private static Class[] mainClasses = {
        com.ksmpartners.rpncalc.basic.RpnCalc.class,
        com.ksmpartners.rpncalc.composite.RpnCalc.class,
        com.ksmpartners.rpncalc.undoable.RpnCalc.class,
        com.ksmpartners.rpncalc.stateobject.RpnCalc.class,
        com.ksmpartners.rpncalc.functional.RpnCalc.class,
        com.ksmpartners.rpncalc.functionalrf.RpnCalc.class,
        com.ksmpartners.rpncalc.iterator.RpnCalc.class,
        com.ksmpartners.rpncalc.reducer.RpnCalc.class,
        com.ksmpartners.rpncalc.experimental.RpnCalc.class,
    };

    private static Class lookupCalculatorClassByOrdinalString(String ordinalStr)
    {
        int classNum = -1;

        try {
            classNum = Integer.parseInt(ordinalStr);
        } catch (Exception ex) {
        }

        if ((classNum >= 0) && classNum < mainClasses.length)
            return mainClasses[classNum];

        System.out.println("Bad calculator class ordinal number: " + ordinalStr);

        return null;
    }

    private static Class selectCalculatorClass()
        throws Exception
    {
        while(true) {
            System.out.println("\nPlease select a version of the calculator:");

            for(int ii = 0; ii < mainClasses.length; ii++)
                System.out.println(ii + ") " + mainClasses[ii]);

            System.out.print("Select Calculator Class >> ");

            String cmdLine = System.console().readLine();

            if (cmdLine == null)
                return null;

            Class klass = lookupCalculatorClassByOrdinalString(cmdLine.trim());

            if (klass != null)
                return klass;
        }
    }

    public static void main( String[] args )
    {
        try {
            Class mainClass = null;

            if (args.length == 0)
                mainClass = selectCalculatorClass();
            else
                mainClass = lookupCalculatorClassByOrdinalString(args[0]);

            if (mainClass != null) {

                Object calc = mainClass.newInstance();

                System.out.println("Begin: " + mainClass);

                ((Calculator)calc).main();
            }

            System.out.println("end run.");
        } catch(Exception ex) {
            System.err.println("Uncaught Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
