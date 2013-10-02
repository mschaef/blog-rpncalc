package com.ksmpartners.rpncalc.functionalrf;

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

    private class State
    {
        State prev;
        Deque<Double> stack = null;
        Double[] regs = null;

        State()
        {
            prev = null;
            stack = new LinkedList<Double>();
            regs = new Double[NUM_REGISTERS];
        }

        State(State original)
        {
            prev = original;
            stack = new LinkedList<Double>(original.stack);
            regs = Arrays.copyOf(original.regs, original.regs.length);
        }
    }

    private Map<String, Command> cmds = new HashMap<String, Command>();
    
    abstract class Command
    {
        void update(State s)
        {
        }

        State execute(State in)
        {
            State out = new State(in);

            update(out);

            return out;
        }
    }

    private class PushNumberCommand extends Command
    {
        private Double number;

        PushNumberCommand(Double number)
        {
            this.number = number;
        }

        public void update(State s)
        {
            s.stack.push(number);
        }
    }

    private class CompositeCommand extends Command
    {
        private List<Command> subCmds = new LinkedList<Command>();

        CompositeCommand(Collection<Command> subCmds)
        {
            this.subCmds.addAll(subCmds);
        }
        
        public State execute(State in)
        {
            State out = new State(in);

            for(Command subCmd : subCmds)
                out = subCmd.execute(out);

            return out;
        }
    }

    public RpnCalc()
    {
        cmds.put("+", new Command() {
                public void update(State s) {
                    Double x = s.stack.pop();
                    Double y = s.stack.pop();

                    s.stack.push(x + y);
                }
            });

        cmds.put("-", new Command() {
                public void update(State s) {
                    Double x = s.stack.pop();
                    Double y = s.stack.pop();

                    s.stack.push(y - x);
                }
            });

        cmds.put("*", new Command() {
                public void update(State s) {
                    Double x = s.stack.pop();
                    Double y = s.stack.pop();

                    s.stack.push(x * y);
                }
            });

        cmds.put("/", new Command() {
                public void update(State s) {
                    Double x = s.stack.pop();
                    Double y = s.stack.pop();

                    s.stack.push(y / x);
                }
            });

        cmds.put("sto", new Command() {
                public void update(State s) {
                    Double rnum = s.stack.pop();

                    s.regs[rnum.intValue()] = s.stack.pop();
                }
            });

        cmds.put("rcl", new Command() {
                public void update(State s) {
                    Double rnum = s.stack.pop();

                    s.stack.push(s.regs[rnum.intValue()]);
                }
            });
                
        cmds.put("drop", new Command() {
                public void update(State s) {
                    s.stack.pop();
                }
            });

        cmds.put("undo", new Command() {
                public State execute(State in) {
                    return in.prev.prev;
                }
            });

        cmds.put("quit", new Command() {
                public State execute(State in) {
                    return null;
                }
            });
    }

    private void showStack(State state)
    {
        int ii = 0;

        for (Iterator<Double> it = state.stack.descendingIterator(); it.hasNext(); ) {
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
        State state = new State();

        while(state != null) {
            System.out.println();
            showStack(state);
            System.out.print("> ");

            String cmdLine = System.console().readLine();

            if (cmdLine == null)
                break;

            Command cmd = parseCommandString(cmdLine);

            state = cmd.execute(state);
        }
    }
}
