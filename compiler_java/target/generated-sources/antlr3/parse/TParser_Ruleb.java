// $ANTLR 3.4 Ruleb.g 2012-07-25 21:39:28


    package parse;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.*;


@SuppressWarnings({"all", "warnings", "unchecked"})
public class TParser_Ruleb extends AbstractTParser {
    public static final int EOF=-1;
    public static final int ADD=4;
    public static final int COMMENT=5;
    public static final int ESC_SEQ=6;
    public static final int HEX_DIGIT=7;
    public static final int ID=8;
    public static final int INT=9;
    public static final int KEYSER=10;
    public static final int OCTAL_ESC=11;
    public static final int SEMI=12;
    public static final int SOZE=13;
    public static final int STRING=14;
    public static final int UNICODE_ESC=15;
    public static final int WS=16;
    public static final int SCRIPT=17;

    // delegates
    public AbstractTParser[] getDelegates() {
        return new AbstractTParser[] {};
    }

    // delegators
    public TParser gTParser;
    public TParser gParent;


    public TParser_Ruleb(TokenStream input, TParser gTParser) {
        this(input, new RecognizerSharedState(), gTParser);
    }
    public TParser_Ruleb(TokenStream input, RecognizerSharedState state, TParser gTParser) {
        super(input, state);
        this.gTParser = gTParser;
        gParent = gTParser;
    }

protected TreeAdaptor adaptor = new CommonTreeAdaptor();

public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
}
public TreeAdaptor getTreeAdaptor() {
    return adaptor;
}
    public String[] getTokenNames() { return TParser.tokenNames; }
    public String getGrammarFileName() { return "Ruleb.g"; }


    public static class b_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "b"
    // Ruleb.g:8:1: b : ( keyser SEMI !| expression SEMI !);
    public final TParser_Ruleb.b_return b() throws RecognitionException {
        TParser_Ruleb.b_return retval = new TParser_Ruleb.b_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token SEMI2=null;
        Token SEMI4=null;
        TParser.keyser_return keyser1 =null;

        TParser.expression_return expression3 =null;


        Object SEMI2_tree=null;
        Object SEMI4_tree=null;

        try {
            // Ruleb.g:9:4: ( keyser SEMI !| expression SEMI !)
            int alt1=2;
            switch ( input.LA(1) ) {
            case KEYSER:
                {
                alt1=1;
                }
                break;
            case ID:
            case INT:
            case STRING:
                {
                alt1=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;

            }

            switch (alt1) {
                case 1 :
                    // Ruleb.g:9:6: keyser SEMI !
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_keyser_in_b20);
                    keyser1=gTParser.keyser();

                    state._fsp--;

                    adaptor.addChild(root_0, keyser1.getTree());

                    SEMI2=(Token)match(input,SEMI,FOLLOW_SEMI_in_b22); 

                    }
                    break;
                case 2 :
                    // Ruleb.g:10:6: expression SEMI !
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_expression_in_b30);
                    expression3=gTParser.expression();

                    state._fsp--;

                    adaptor.addChild(root_0, expression3.getTree());

                    SEMI4=(Token)match(input,SEMI,FOLLOW_SEMI_in_b32); 

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "b"

    // Delegated rules


 

    public static final BitSet FOLLOW_keyser_in_b20 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_SEMI_in_b22 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_b30 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_SEMI_in_b32 = new BitSet(new long[]{0x0000000000000002L});

}