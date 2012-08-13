// $ANTLR 3.4 parse/TParser.g 2012-07-25 21:39:28


    package parse;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.*;


@SuppressWarnings({"all", "warnings", "unchecked"})
public class TParser extends AbstractTParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ADD", "COMMENT", "ESC_SEQ", "HEX_DIGIT", "ID", "INT", "KEYSER", "OCTAL_ESC", "SEMI", "SOZE", "STRING", "UNICODE_ESC", "WS", "SCRIPT", "18"
    };

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
    public TParser_Ruleb gRuleb;
    public AbstractTParser[] getDelegates() {
        return new AbstractTParser[] {gRuleb};
    }

    // delegators


    public TParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public TParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
        gRuleb = new TParser_Ruleb(input, state, this);
    }

protected TreeAdaptor adaptor = new CommonTreeAdaptor();

public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
    gRuleb.setTreeAdaptor(this.adaptor);
}
public TreeAdaptor getTreeAdaptor() {
    return adaptor;
}
    public String[] getTokenNames() { return TParser.tokenNames; }
    public String getGrammarFileName() { return "parse/TParser.g"; }


    public static class a_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "a"
    // parse/TParser.g:53:1: a : ( b )* EOF -> ^( SCRIPT ( b )* ) ;
    public final TParser.a_return a() throws RecognitionException {
        TParser.a_return retval = new TParser.a_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token EOF2=null;
        TParser_Ruleb.b_return b1 =null;


        Object EOF2_tree=null;
        RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
        RewriteRuleSubtreeStream stream_b=new RewriteRuleSubtreeStream(adaptor,"rule b");
        try {
            // parse/TParser.g:53:4: ( ( b )* EOF -> ^( SCRIPT ( b )* ) )
            // parse/TParser.g:53:6: ( b )* EOF
            {
            // parse/TParser.g:53:6: ( b )*
            loop1:
            do {
                int alt1=2;
                switch ( input.LA(1) ) {
                case ID:
                case INT:
                case KEYSER:
                case STRING:
                    {
                    alt1=1;
                    }
                    break;

                }

                switch (alt1) {
            	case 1 :
            	    // parse/TParser.g:53:6: b
            	    {
            	    pushFollow(FOLLOW_b_in_a182);
            	    b1=b();

            	    state._fsp--;

            	    stream_b.add(b1.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_a185);  
            stream_EOF.add(EOF2);


            // AST REWRITE
            // elements: b
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 55:7: -> ^( SCRIPT ( b )* )
            {
                // parse/TParser.g:55:10: ^( SCRIPT ( b )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(SCRIPT, "SCRIPT")
                , root_1);

                // parse/TParser.g:55:19: ( b )*
                while ( stream_b.hasNext() ) {
                    adaptor.addChild(root_1, stream_b.nextTree());

                }
                stream_b.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

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
    // $ANTLR end "a"


    public static class keyser_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "keyser"
    // parse/TParser.g:60:1: keyser : KEYSER ^ SOZE ;
    public final TParser.keyser_return keyser() throws RecognitionException {
        TParser.keyser_return retval = new TParser.keyser_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token KEYSER3=null;
        Token SOZE4=null;

        Object KEYSER3_tree=null;
        Object SOZE4_tree=null;

        try {
            // parse/TParser.g:61:4: ( KEYSER ^ SOZE )
            // parse/TParser.g:61:6: KEYSER ^ SOZE
            {
            root_0 = (Object)adaptor.nil();


            KEYSER3=(Token)match(input,KEYSER,FOLLOW_KEYSER_in_keyser218); 
            KEYSER3_tree = 
            (Object)adaptor.create(KEYSER3)
            ;
            root_0 = (Object)adaptor.becomeRoot(KEYSER3_tree, root_0);


            SOZE4=(Token)match(input,SOZE,FOLLOW_SOZE_in_keyser221); 
            SOZE4_tree = 
            (Object)adaptor.create(SOZE4)
            ;
            adaptor.addChild(root_0, SOZE4_tree);


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
    // $ANTLR end "keyser"


    public static class expression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "expression"
    // parse/TParser.g:64:1: expression : addExpr ( ADD ^ addExpr )* ;
    public final TParser.expression_return expression() throws RecognitionException {
        TParser.expression_return retval = new TParser.expression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token ADD6=null;
        TParser.addExpr_return addExpr5 =null;

        TParser.addExpr_return addExpr7 =null;


        Object ADD6_tree=null;

        try {
            // parse/TParser.g:65:4: ( addExpr ( ADD ^ addExpr )* )
            // parse/TParser.g:65:6: addExpr ( ADD ^ addExpr )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_addExpr_in_expression236);
            addExpr5=addExpr();

            state._fsp--;

            adaptor.addChild(root_0, addExpr5.getTree());

            // parse/TParser.g:65:14: ( ADD ^ addExpr )*
            loop2:
            do {
                int alt2=2;
                switch ( input.LA(1) ) {
                case ADD:
                    {
                    alt2=1;
                    }
                    break;

                }

                switch (alt2) {
            	case 1 :
            	    // parse/TParser.g:65:15: ADD ^ addExpr
            	    {
            	    ADD6=(Token)match(input,ADD,FOLLOW_ADD_in_expression239); 
            	    ADD6_tree = 
            	    (Object)adaptor.create(ADD6)
            	    ;
            	    root_0 = (Object)adaptor.becomeRoot(ADD6_tree, root_0);


            	    pushFollow(FOLLOW_addExpr_in_expression242);
            	    addExpr7=addExpr();

            	    state._fsp--;

            	    adaptor.addChild(root_0, addExpr7.getTree());

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


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
    // $ANTLR end "expression"


    public static class addExpr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "addExpr"
    // parse/TParser.g:68:1: addExpr : ( ID | INT | STRING );
    public final TParser.addExpr_return addExpr() throws RecognitionException {
        TParser.addExpr_return retval = new TParser.addExpr_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set8=null;

        Object set8_tree=null;

        try {
            // parse/TParser.g:69:4: ( ID | INT | STRING )
            // parse/TParser.g:
            {
            root_0 = (Object)adaptor.nil();


            set8=(Token)input.LT(1);

            if ( (input.LA(1) >= ID && input.LA(1) <= INT)||input.LA(1)==STRING ) {
                input.consume();
                adaptor.addChild(root_0, 
                (Object)adaptor.create(set8)
                );
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


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
    // $ANTLR end "addExpr"

    // Delegated rules
    public TParser_Ruleb.b_return b() throws RecognitionException { return gRuleb.b(); }


 

    public static final BitSet FOLLOW_b_in_a182 = new BitSet(new long[]{0x0000000000004700L});
    public static final BitSet FOLLOW_EOF_in_a185 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KEYSER_in_keyser218 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_SOZE_in_keyser221 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_addExpr_in_expression236 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_ADD_in_expression239 = new BitSet(new long[]{0x0000000000004300L});
    public static final BitSet FOLLOW_addExpr_in_expression242 = new BitSet(new long[]{0x0000000000000012L});

}