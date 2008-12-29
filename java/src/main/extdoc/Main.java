package extdoc;

import extdoc.config.CliReader;
import extdoc.config.Config;
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

        // start parsing using config
        Context context = new Context();
        Parser parser = new FileParser(config);
        parser.parse(context);
    }    
}
