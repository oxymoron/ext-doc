package extdoc;

import extdoc.exceptions.WrongCliException;
import extdoc.jsdoc.processor.FileProcessor;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: oxymoron
 * Date: Jul 2, 2010
 * Time: 10:52:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainTest {

    // all the following strings are incorrect
    private static final String[] wrongCommands = {
        "test",
        "-p projectFile",
        "-o output",
        "-p projectFile -o outputFolder -t templateFile -q -verbose",
        "-s source"
    };

    @BeforeClass
    public static void setUp(){
        Main.prepareOptions();
    }

    @Test
    public void wrongParamsTest(){
        FileProcessor fileProcessor = createMock(FileProcessor.class);
        for (String s : wrongCommands){
            try {
                Main.processCli(fileProcessor, s.split(" "));
            } catch (WrongCliException e) {
                continue;
            }
            // If exception not thrown then test fails.
            // Exception must be thrown for every wrong command.
            fail();
        }
    }

    @Test
    public void allTest() throws WrongCliException {
        FileProcessor fileProcessor = createMock(FileProcessor.class);
        fileProcessor.process("projectFile", null);
        fileProcessor.setQuiet();
        fileProcessor.saveToFolder("outputFolder", "templateFile");
        replay(fileProcessor);        
        Main.processCli(fileProcessor, "-p projectFile -o outputFolder -t templateFile -q".split(" "));
        verify(fileProcessor);
    }

    @Test
    public void sourceTest() throws WrongCliException {
        FileProcessor fileProcessor = createMock(FileProcessor.class);
        fileProcessor.process(isNull(String.class), aryEq(new String[]{"source1", "source2"}));        
        fileProcessor.saveToFolder("outputFolder", "templateFile");
        replay(fileProcessor);
        Main.processCli(fileProcessor, "-s source1 -s source2 -o outputFolder -t templateFile".split(" "));
        verify(fileProcessor);
    }

}
