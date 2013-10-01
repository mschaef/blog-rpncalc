import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class RpnCalc
{
    private static final int NUM_REGISTERS = 20;

    private static boolean running = true;

    private static Stack<Double> stack = new Stack<Double>();
    private static Double[] regs = new Double[NUM_REGISTERS];

    private static Map<String, Command> cmds = new HashMap<String, Command>();

    interface Command
    {
        void execute();
    }

    private static class PushNumberCommand implements Command
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

    static {
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

    private static void showStack()
    {
        for(int ii = 0; ii < stack.size(); ii++)
            System.out.println((ii + 1) + "> " + stack.elementAt(ii));
    }

    private static Command parseCommandString(String cmdStr)
        throws Exception
    {
        Command cmd = cmds.get(cmdStr);

        if (cmd != null)
            return cmd;
        else
            return new PushNumberCommand(Double.parseDouble(cmdStr));
    }

    private static void repl()
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

            if (cmd == null)
                System.err.println("Invalid command: " + cmdLine);
            else
                cmd.execute();
        }
    }

    public static void main(String[] args)
    {
        try {
            repl();
        } catch(Exception ex) {
            System.err.println("Uncaught Exception: " + ex.getMessage());
            ex.printStackTrace();
        }

        System.out.println("end run.");
    }
}
