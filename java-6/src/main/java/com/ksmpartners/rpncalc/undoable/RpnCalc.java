package com.ksmpartners.rpncalc.undoable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
    private Command lastCmd = null;

    private Map<String, Command> cmds = new HashMap<String, Command>();
    
    abstract class Command
    {
        private Deque<Double> oldStack;
        private Double[] oldRegs;

        void saveState()
        {
            oldStack = new LinkedList<Double>(stack);
            oldRegs = Arrays.copyOf(regs, regs.length);
        }

        void restoreState()
        {
            stack = new LinkedList<Double>(oldStack);
            regs = Arrays.copyOf(oldRegs, oldRegs.length);
        }

        abstract void execute();
    }

    private class PushNumberCommand extends Command
    {
        private Double number;

        PushNumberCommand(Double number)
        {
            this.number = number;
        }

        public void execute()
        {
            saveState();

            stack.push(number);
        }
    }

    private class CompositeCommand extends Command
    {
        private List<Command> subCmds = new LinkedList<Command>();

        CompositeCommand(Collection<Command> subCmds)
        {
            this.subCmds.addAll(subCmds);
        }

        public void execute()
        {
            saveState();

            for(Command subCmd : subCmds)
                subCmd.execute();
        }
    }

    public RpnCalc()
    {
        cmds.put("+", new Command() {
                public void execute() {
                    saveState();

                    Double x = stack.pop();
                    Double y = stack.pop();

                    stack.push(x + y);
                }
            });

        cmds.put("-", new Command() {
                public void execute() {
                    saveState();

                    Double x = stack.pop();
                    Double y = stack.pop();

                    stack.push(y - x);
                }
            });

        cmds.put("*", new Command() {
                public void execute() {
                    saveState();

                    Double x = stack.pop();
                    Double y = stack.pop();

                    stack.push(x * y);
                }
            });

        cmds.put("/", new Command() {
                public void execute() {
                    saveState();

                    Double x = stack.pop();
                    Double y = stack.pop();

                    stack.push(y / x);
                }
            });

        cmds.put("sto", new Command() {
                public void execute() {
                    saveState();

                    Double rnum = stack.pop();

                    regs[rnum.intValue()] = stack.pop();
                }
            });

        cmds.put("rcl", new Command() {
                public void execute() {
                    saveState();

                    Double rnum = stack.pop();

                    stack.push(regs[rnum.intValue()]);
                }
            });
                
        cmds.put("drop", new Command() {
                public void execute() {
                    saveState();

                    stack.pop();
                }
            });

        cmds.put("undo", new Command() {
                public void execute() {
                    saveState();

                    lastCmd.restoreState();
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

            cmd.execute();

            lastCmd = cmd;
        }
    }
}
