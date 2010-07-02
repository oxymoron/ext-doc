package extdoc.jsdoc.processor;

/**
 * Created by IntelliJ IDEA.
 * User: oxymoron
 * Date: Jul 2, 2010
 * Time: 11:00:21 PM
 * To change this template use File | Settings | File Templates.
 */
public interface FileProcessor {
    
    void setVerbose();

    void setQuiet();

    void process(String fileName, String[] extraSrc);

    void saveToFolder(String folderName, String templateFileName);
}
