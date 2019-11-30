package com.dmogroup5.parser;

import org.apache.commons.cli.*;


public class Params {

    // Default parameter values
    private String instanceName;
    private Double TLIM = Double.POSITIVE_INFINITY;
    private boolean DEBUG = false;

    private Options options = new Options();
    private String[] args;

    public Params(String[] args) {
        this.instanceName = args[0];
        this.args = args;
    }

    public void parse() throws ParseException {
        this.options.addOption(new Option("d", "debug", false, "Enable debug mode"));

        Option tlim = Option.builder("t")
                .longOpt("time-lim")
                .hasArg()
                .argName("tlim(s)")
                .desc("Limit the execution time for `tlim` seconds")
                .build();

        this.options.addOption(tlim);

        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse( this.options, this.args );

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

    public Double getTLIM() {
        return TLIM;
    }

    public boolean isDEBUG() {
        return DEBUG;
    }

    public String[] getArgs() {
        return args;
    }

    public String getInstanceName() {
        return instanceName;
    }
}
