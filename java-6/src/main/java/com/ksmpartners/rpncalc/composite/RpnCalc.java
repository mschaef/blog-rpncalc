package com.ksmpartners.rpncalc.composite;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.List;
import java.util.LinkedList;

import com.ksmpartners.rpncalc.Calculator;

public class RpnCalc extends Calculator
{
    private static final int NUM_REGISTERS = 20;

    private boolean running = true;

    private Stack<Double> stack = new Stack<Double>();
    private Double[] regs = new Double[NUM_REGISTERS];

    private Map<String, Command> cmds = new HashMap<String, Command>();

    interface Command
    {
        void execute();
    }

    private class PushNumberCommand implements Command
    {
        private Double number;

        PushNumberCommand(Double number)
        {
            this.number = number;
        }

        public void execute()
        {
            stack.push(number);
        }
    }

    private class CompositeCommand implements Command
    {
        private List<Command> subCmds = new LinkedList<Command>();

        CompositeCommand(Collection<Command> subCmds)
        {
            this.subCmds.addAll(subCmds);
        }

        public void execute()
        {
            for(Command subCmd : subCmds)
                subCmd.execute();
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
        for(int ii = 0; ii < stack.size(); ii++)
            System.out.println((ii + 1) + "> " + stack.elementAt(ii));
    }

    private Command parseSingleCommand(String cmdStr)
        throws Exception
    {
        Command cmd = cmds.get(cmdStr);

        if (cmd != null)
            return cmd;
        else
            return new PushNumberCommand(Double.parseDouble(cmdStr));
    }

    private Command parseCommandString(String cmdStr)
        throws Exception
    {
        List<Command> subCmds = new LinkedList<Command>();

        for (String subCmdStr : cmdStr.split("\\s+"))
            subCmds.add(parseSingleCommand(subCmdStr));

        return new CompositeCommand(subCmds);
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

            Command cmd = parseCommandString(cmdLine);

            if (cmd == null)
                System.err.println("Invalid command: " + cmdLine);
            else
                cmd.execute();
        }
    }
}
