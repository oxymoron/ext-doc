package extdoc.parser;

import extdoc.comment.Preprocessor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: Andrey Zubkov
 * Date: 27.12.2008
 * Time: 0:37:39
 */
public class Context {

    public Logger logger = null;

    public List<File> files = new ArrayList<File>(); 

    public File currentFile = null;

    public String startComment = null;

    public String endComment = null;

    public StringBuilder code = new StringBuilder();

    public StringBuilder comment = new StringBuilder();

    /**
     * Current position in file
     */
    public long position = 0;

    public List<Preprocessor> preprocessors = new ArrayList<Preprocessor>();

}
