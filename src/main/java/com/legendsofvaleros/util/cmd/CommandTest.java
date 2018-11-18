package com.legendsofvaleros.util.cmd;

import java.util.Scanner;

public class CommandTest {
    public static void main(String[] args) {
        Command cmd = new Command("lov") {
            @Override
            public String getDescription() {
                return "This is some fuckin shitty LOV command thing for testing.";
            }

            @Override
            public void onCommand(CommandContext context) {
                System.out.println("Ran " + context.getArgument("wew") + " " + context.getArgument("byte") + " " + context.getArgument("kek") + " " + context.getArgument("test"));
            }
        };
        cmd.addArgument("wew", new CommandArgument<String>() {
            @Override
            public String getDescription() {
                return "This is a wew";
            }

            @Override
            public String doParse(String arg) {
                return arg;
            }
        });
        cmd.addOptionalArgument("byte", new CommandArgument<Byte>() {
            @Override
            public String getArgumentLabel() {
                return "0-128";
            }

            @Override
            public String getDescription() {
                return "A number between 0 and 128";
            }

            @Override
            public Byte doParse(String arg) {
                return Byte.parseByte(arg);
            }
        });
        cmd.addSwitch("kek", "This is true by default.", true, false);
        cmd.addSwitch("test", new CommandSwitch<Integer>() {
            @Override
            public String getDescription() { return "Do the testing!"; }

            @Override
            public String getArgumentLabel() { return "int"; }

            @Override
            public Integer getDefault() { return 0; }

            @Override
            public Integer doParse(String arg) {
                return Integer.parseInt(arg);
            }
        });

        Scanner scan = new Scanner(System.in);

        String line;
        while((line = scan.nextLine()) != null) {
            cmd.onCommandExecuted(null, line);
        }
    }
}
