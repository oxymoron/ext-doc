package extdoc;

import extdoc.config.CliReader;
import extdoc.config.Config;
import extdoc.config.ContextBuilder;
import extdoc.config.ProjectFileReader;
import extdoc.parser.Context;
import extdoc.parser.Parser;
import extdoc.parser.impl.FileParser;

/**
 * User: Andrey Zubkov
 * Date: 25.10.2008
 * Time: 2:16:18
 */

public class Main {
    public static void main(String[] args) {

        // create config
        Config config = new Config();

        // read command line options to config
        new CliReader(args).read(config);

        // add more config information from project file if specified
        new ProjectFileReader().read(config);

        // create context form config
        Context context = new ContextBuilder().build(config);

        // start parsing using context
        Parser parser = new FileParser();
        parser.parse(context);
    }    
}
