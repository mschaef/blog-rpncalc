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

package com.ksmpartners.rpncalc.basic;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Deque;
import java.util.List;
import java.util.LinkedList;

import com.ksmpartners.rpncalc.Calculator;

public class RpnCalc extends Calculator
{
    private static final int NUM_REGISTERS = 20;

    private boolean running = true;

    private Deque<Double> stack = new LinkedList<Double>();
    private Double[] regs = new Double[NUM_REGISTERS];

    private Map<String, Command> cmds = new HashMap<String, Command>();

    interface Command
    {
        void execute();
    }

    private class PushNumberCommand implements Command
    {
        Double number;

        PushNumberCommand(Double number)
        {
            this.number = number;
        }

        public void execute()
        {
            stack.push(number);
        }
    }

    public RpnCalc()
    {
        cmds.put("+", new Command() {
                public void execute() {
                    Double x = stack.pop();
                    Double y = stack.pop();

                    stack.push(x + y);
                }
            });

        cmds.put("-", new Command() {
                public void execute() {
                    Double x = stack.pop();
                    Double y = stack.pop();

                    stack.push(y - x);
                }
            });

        cmds.put("*", new Command() {
                public void execute() {
                    Double x = stack.pop();
                    Double y = stack.pop();

                    stack.push(x * y);
                }
            });

        cmds.put("/", new Command() {
                public void execute() {
                    Double x = stack.pop();
                    Double y = stack.pop();

                    stack.push(y / x);
                }
            });

        cmds.put("sto", new Command() {
                public void execute() {
                    Double rnum = stack.pop();

                    regs[rnum.intValue()] = stack.pop();
                }
            });

        cmds.put("rcl", new Command() {
                public void execute() {
                    Double rnum = stack.pop();

                    stack.push(regs[rnum.intValue()]);
                }
            });

        cmds.put("drop", new Command() {
                public void execute() {
                    stack.pop();
                }
            });

        cmds.put("quit", new Command() {
                public void execute() {
                    running = false;
                }
            });
    }

    private void showStack()
    {
        int ii = 0;

        for (Iterator<Double> it = stack.descendingIterator(); it.hasNext(); ) {
            Double val = it.next();

            System.out.println((ii + 1) + "> " + val);
            ii++;
        }
    }

    private Command parseCommandString(String cmdStr)
        throws Exception
    {
        Command cmd = cmds.get(cmdStr);

        if (cmd != null)
            return cmd;
        else
            return new PushNumberCommand(Double.parseDouble(cmdStr));
    }

    public void main()
        throws Exception
    {
        while(running) {
            System.out.println();
            showStack();
            System.out.print("> ");

            String cmdLine = System.console().readLine();

            if (cmdLine == null)
                break;

            Command cmd = parseCommandString(cmdLine.trim());

            cmd.execute();
        }
    }
}
