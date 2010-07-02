package extdoc;

import extdoc.exceptions.WrongCliException;
import extdoc.jsdoc.processor.FileProcessor;
import org.junit.Test;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: oxymoron
 * Date: Jul 2, 2010
 * Time: 10:52:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainTest {

    @Test (expected = WrongCliException.class)
    public void firstTest() throws WrongCliException {
        FileProcessor fileProcessor = createMock(FileProcessor.class);
        Main.prepareOptions();
        Main.processCli(fileProcessor, new String[]{"test"});
    }

}
