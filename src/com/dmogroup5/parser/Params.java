package com.dmogroup5.parser;

import org.apache.commons.cli.*;


public class Params {

    // Default parameter values
    private String instanceName;
    private Double TLIM = Double.POSITIVE_INFINITY;
    private boolean DEBUG = false;
    private boolean HELP = false;

    public Options options = new Options();
    private String[] args;

    public Params(String[] args) {
        this.instanceName = args[0];
        this.args = args;
    }

    public void parse() throws ParseException {
        this.options.addOption(new Option("h", "help", false, "Print this message"));
        this.options.addOption(new Option("d", "debug", false, "Enable debug mode:" +
                " prints additional message and save execution iterations in a log file"));

        Option tlim = Option.builder("t")
                .longOpt("time-lim")
                .hasArg()
                .argName("seconds")
                .desc("Limit the execution time for `tlim` seconds. If not specified, the program will not stop by itself.")
                .build();

        this.options.addOption(tlim);

        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse( this.options, this.args );

        if (line.hasOption("h")) {
            this.HELP = true;
            String header = "\nSpecify the absolute or relative path and the instance name\n" +
                    "e.g. `instance01.exm` is in folder `/home/user/instances/`, then write `/home/user/instances/instance01`." +
                    " If the instance files are in the same folder of the solver, just insert the instance name `instance01`\n\n";
            String footer = "\nFor further information take a look at the README at http://github.com/toyo97/ETPsolver\n" +
                    "Please report issues at mailto:vittorio.zampinetti@studenti.polito.it";
            new HelpFormatter().printHelp("java -jar ETPsolver_DMOgroup05.jar <instance_name_path> [-t <tlim>] [-h] [-d]",
                    header, this.options, footer,false);
            throw new ParseException("Help option is selected");
        } else {

            if (line.hasOption("t")) {
                this.TLIM = Double.parseDouble(line.getOptionValue("t"));
                if (this.TLIM <= 0) {
                    throw new ParseException("Time limit value not valid, please insert a positive number (seconds)");
                }
            }

            if (line.hasOption("d")) {
                this.DEBUG = true;
            }
        }

    }

    public Double getTLIM() {
        return TLIM;
    }

    public boolean isDEBUG() {
        return DEBUG;
    }

    public boolean isHELP() {
        return HELP;
    }

    public String[] getArgs() {
        return args;
    }

    public String getInstanceName() {
        return instanceName;
    }
}
