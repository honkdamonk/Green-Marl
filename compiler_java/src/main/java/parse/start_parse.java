package parse;

import java.io.IOException;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTreeNodeStream;

public class start_parse {

	public static int parse(String fname) {
		CharStream cs;
		try {
			cs = new ANTLRFileStream(fname);
		} catch (IOException e) {
			return 1; // TODO: error?
		}
		
		GMLexer lexer = new GMLexer(cs);
		
		CommonTokenStream tokens = new CommonTokenStream();
		tokens.setTokenSource(lexer);

		GMParser parser = new GMParser(tokens);
		GMParser.prog_return root;
		try {
			root = parser.prog();
		} catch (RecognitionException e) {
			return 1; // TODO: error?
		}
		CommonTreeNodeStream nodes = new CommonTreeNodeStream(root.tree);
		GMTreeParser treeParser = new GMTreeParser(nodes);
		try {
			treeParser.prog();
		} catch (RecognitionException e) {
			return 1; // TODO: error?
		}

		return 0;
	}

}
