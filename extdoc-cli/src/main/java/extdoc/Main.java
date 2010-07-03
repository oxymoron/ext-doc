package extdoc;

import extdoc.exceptions.WrongCliException;
import extdoc.jsdoc.processor.FileProcessor;
import extdoc.jsdoc.processor.FileProcessorImpl;
import org.apache.commons.cli.*;

import java.text.MessageFormat;

/**
 * User: Andrey Zubkov
 * Date: 25.10.2008
 * Time: 2:16:18
 */

public class Main {

    private static Options options = null;
    private static final String WRONG_CLI_ARGUMENTS = "Wrong command line arguments: {0}";
    private static final String HELP_MESSAGE = "java -jar ext-doc.jar [-p project] -o output [-s source1 -s source2 ...]";

    private static void wrongCli(String msg){
        System.err.println(MessageFormat.format(WRONG_CLI_ARGUMENTS, msg));
        showHelp();
    }

    private static void showHelp(){
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(HELP_MESSAGE, options);
    }

    public static void main(String[] args) {

        prepareOptions();
        try {
            FileProcessor fileProcessor = new FileProcessorImpl();
            processCli(fileProcessor, args);
        } catch (WrongCliException e) {
            wrongCli(e.getMessage());
        }
    }

    public static void processCli(FileProcessor processor, String[] args) throws WrongCliException {
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse( options, args);
            if(cmd.hasOption("project")||cmd.hasOption("source")){

                if (cmd.hasOption("quiet") && cmd.hasOption("verbose")){
                    throw new WrongCliException("Processing cannot be quiet and verbose at the same time");
                }
                if(cmd.hasOption("quiet")){
                    processor.setQuiet();
                }else if (cmd.hasOption("verbose")){
                    processor.setVerbose();
                }
                processor.process(
                        cmd.getOptionValue("project"),
                        cmd.getOptionValues("source")
                );
                processor.saveToFolder(
                    cmd.getOptionValue("output"),
                    cmd.getOptionValue("template"));
            }else{
                throw new WrongCliException("Project XML file or source files should be specified");
            }
        } catch (ParseException e) {
            throw new WrongCliException(e);
        }
    }

    @SuppressWarnings("static-access")
    public static void prepareOptions() {
        
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
                .isRequired()
                .withLongOpt("output")
                .create('o');

        Option template = OptionBuilder.withArgName("template")
                .hasArg()
                .withDescription("XML File containing template informaiton")
                .isRequired()
                .withLongOpt("template")
                .create('t');

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
        options.addOption(template);
        options.addOption(source);
    }
}
