package application;

import domein.Prik2GoException;
import org.apache.commons.cli.*;

import static domein.BezoekInfo.schoonPostcodesOp;

public class Application {

    public static void main(String[] args) {

        String bronbestand = null;
        String doelbestand = null;

        Options options = commandLineOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("bronbestand")) {
                bronbestand = cmd.getOptionValue("bronbestand");
            }

            if (cmd.hasOption("doelbestand")) {
                doelbestand = cmd.getOptionValue("doelbestand");
            }
        } catch( ParseException e ) {
            e.printStackTrace();
        }

        try {
            schoonPostcodesOp(bronbestand, doelbestand);
        } catch (Prik2GoException e) {
            e.printStackTrace();
        }

    }

    private static Options commandLineOptions() {
        Options options = new Options();
        options.addOption("b", "bronbestand", true, "CSV bezoekinfo invoerbestand");
        options.addOption("d", "doelbestand", true, "CSV bezoekinfo uitvoerbestand");
        return options;
    }
}
