package com.ksmpartners.rpncalc;

public class App 
{
    private static Class[] mainClasses = {
        com.ksmpartners.rpncalc.basic.RpnCalc.class,
        com.ksmpartners.rpncalc.composite.RpnCalc.class
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
