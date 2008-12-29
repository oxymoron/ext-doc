package extdoc.config;

import java.text.MessageFormat;
import java.util.logging.Logger;

/**
 * User: Andrey Zubkov
 * Date: 28.12.2008
 * Time: 20:59:00
 */
public class SyntaxFileReader {

    private String syntaxFileName;

    public SyntaxFileReader(String syntaxFileName) {
        this.syntaxFileName = syntaxFileName;
    }

    void read(Config config){
        Logger logger = config.getLogger();
        logger.fine(MessageFormat.format("Processing syntax file: {0}", 
                syntaxFileName));
        
    }

}
