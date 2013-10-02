package com.ksmpartners.rpncalc.functional;

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
        State execute(State in)
        {
            return in;
        }
    }

    private class PushNumberCommand extends Command
    {
        private Double number;

        PushNumberCommand(Double number)
        {
            this.number = number;
        }

        public State execute(State in)
        {
            State out = new State(in);

            out.stack.push(number);

            return out;
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
                public State execute(State in) {
                    State out = new State(in);

                    Double x = out.stack.pop();
                    Double y = out.stack.pop();

                    out.stack.push(x + y);

                    return out;
                }
            });

        cmds.put("-", new Command() {
                public State execute(State in) {
                    State out = new State(in);

                    Double x = out.stack.pop();
                    Double y = out.stack.pop();

                    out.stack.push(y - x);

                    return out;
                }
            });

        cmds.put("*", new Command() {
                public State execute(State in) {
                    State out = new State(in);

                    Double x = out.stack.pop();
                    Double y = out.stack.pop();

                    out.stack.push(x * y);

                    return out;
                }
            });

        cmds.put("/", new Command() {
                public State execute(State in) {
                    State out = new State(in);

                    Double x = out.stack.pop();
                    Double y = out.stack.pop();

                    out.stack.push(y / x);

                    return out;
                }
            });

        cmds.put("sto", new Command() {
                public State execute(State in) {
                    State out = new State(in);

                    Double rnum = out.stack.pop();

                    out.regs[rnum.intValue()] = out.stack.pop();

                    return out;
                }
            });

        cmds.put("rcl", new Command() {
                public State execute(State in) {
                    State out = new State(in);

                    Double rnum = out.stack.pop();

                    out.stack.push(out.regs[rnum.intValue()]);

                    return out;
                }
            });
                
        cmds.put("drop", new Command() {
                public State execute(State in) {
                    State out = new State(in);

                    out.stack.pop();

                    return out;
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

            if (cmd == null)
                System.err.println("Invalid command: " + cmdLine);
            else 
                state = cmd.execute(state);
        }
    }
}
