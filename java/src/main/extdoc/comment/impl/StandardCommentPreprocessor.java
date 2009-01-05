package extdoc.comment.impl;

import extdoc.comment.Preprocessor;
import extdoc.parser.Context;

/**
 * User: Andrey Zubkov
 * Date: 04.01.2009
 * Time: 21:09:36
 */
public class StandardCommentPreprocessor implements Preprocessor {
    public void process(Context context) {
        System.out.println("I'm STANDARD PREPROCESSOR");    
    }
}
