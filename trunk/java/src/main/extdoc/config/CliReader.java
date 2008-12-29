package extdoc.config;

import org.apache.commons.cli.*;

import java.util.logging.*;

/**
 * User: Andrey Zubkov
 * Date: 27.12.2008
 * Time: 0:59:48
 */
public class CliReader{

    private String[] args = null;

    private Options options = null;

    private void wrongCli(String msg){
          System.err.println("Wrong command line arguments: "+ msg);
          showHelp();
      }

      private void showHelp(){
          HelpFormatter formatter = new HelpFormatter();
          formatter.printHelp( "java -jar ext-doc.jar [-p project] -o output -t template [-s source1 -s source2 ...]", options);
      }

    public void read(Config config){
        options = new Options();

        Option quiet = new Option("q", "quiet", false, "be extra quiet");
        Option verbose = new Option("v", "verbose", false, "be extra verbose");

        Option project = OptionBuilder.withArgName("project")
                .hasArg()
                .withDescription("Project XML file.")
                .withLongOpt("project")
                .create('p');

        Option output = OptionBuilder.withArgName("output")
                .hasArg()
                .withDescription("Directory where documentation should be created.")
                .withLongOpt("output")
                .create('o');

        Option source = OptionBuilder.withArgName("source")
                .hasArg()
                .withDescription("Source files")
                .hasOptionalArgs()
                .withLongOpt("source")
                .create('s');

        options.addOption(quiet);
        options.addOption(verbose);
        options.addOption(project);
        options.addOption(output);
        options.addOption(source);

        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse( options, args);
            if(cmd.hasOption("project")){
                config.setProject(cmd.getOptionValue("project"));
            }
            if(cmd.hasOption("output")){
                config.setOutput(cmd.getOptionValue("output"));
            }
            if(cmd.hasOption("source")){
                String[] sources = cmd.getOptionValues("source");
                for(String src : sources){
                    config.addSource(src);
                }
            }

            Logger logger = Logger.getAnonymousLogger();
            logger.setUseParentHandlers(false);
            Handler logHandler = new ConsoleHandler();
            logHandler.setFormatter(new Formatter() {
                public String format(LogRecord record) {
                    return record.getMessage() + "\n";
                }
            });
            logger.addHandler(logHandler);
            if(cmd.hasOption("quiet")){
                logger.setLevel(Level.OFF);
            }else if (cmd.hasOption("verbose")){
                logger.setLevel(Level.FINE);
                logHandler.setLevel(Level.FINE);
            }
            config.setLogger(logger);

        } catch (ParseException e) {
            wrongCli(e.getMessage());
        }
    }
    
    public CliReader(String[] args) {
        this.args = args;
    }
}
