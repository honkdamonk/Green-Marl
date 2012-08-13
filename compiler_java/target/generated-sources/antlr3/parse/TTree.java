// $ANTLR 3.4 parse/TTree.g 2012-07-25 21:39:29


    package parse;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class TTree extends TreeParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ADD", "COMMENT", "ESC_SEQ", "HEX_DIGIT", "ID", "INT", "KEYSER", "OCTAL_ESC", "SEMI", "SOZE", "STRING", "UNICODE_ESC", "WS", "SCRIPT"
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
    public TreeParser[] getDelegates() {
        return new TreeParser[] {};
    }

    // delegators


    public TTree(TreeNodeStream input) {
        this(input, new RecognizerSharedState());
    }
    public TTree(TreeNodeStream input, RecognizerSharedState state) {
        super(input, state);
    }

    public String[] getTokenNames() { return TTree.tokenNames; }
    public String getGrammarFileName() { return "parse/TTree.g"; }



    // $ANTLR start "a"
    // parse/TTree.g:29:1: a : ( ^( SCRIPT ( stuff )+ ) | SCRIPT );
    public final void a() throws RecognitionException {
        try {
            // parse/TTree.g:29:3: ( ^( SCRIPT ( stuff )+ ) | SCRIPT )
            int alt2=2;
            switch ( input.LA(1) ) {
            case SCRIPT:
                {
                switch ( input.LA(2) ) {
                case DOWN:
                    {
                    alt2=1;
                    }
                    break;
                case EOF:
                    {
                    alt2=2;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 1, input);

                    throw nvae;

                }

                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;

            }

            switch (alt2) {
                case 1 :
                    // parse/TTree.g:29:5: ^( SCRIPT ( stuff )+ )
                    {
                    match(input,SCRIPT,FOLLOW_SCRIPT_in_a114); 

                    match(input, Token.DOWN, null); 
                    // parse/TTree.g:29:14: ( stuff )+
                    int cnt1=0;
                    loop1:
                    do {
                        int alt1=2;
                        switch ( input.LA(1) ) {
                        case ADD:
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
                    	    // parse/TTree.g:29:14: stuff
                    	    {
                    	    pushFollow(FOLLOW_stuff_in_a116);
                    	    stuff();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt1 >= 1 ) break loop1;
                                EarlyExitException eee =
                                    new EarlyExitException(1, input);
                                throw eee;
                        }
                        cnt1++;
                    } while (true);


                    match(input, Token.UP, null); 


                    }
                    break;
                case 2 :
                    // parse/TTree.g:30:5: SCRIPT
                    {
                    match(input,SCRIPT,FOLLOW_SCRIPT_in_a124); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "a"



    // $ANTLR start "stuff"
    // parse/TTree.g:33:1: stuff : ( keyser | expression );
    public final void stuff() throws RecognitionException {
        try {
            // parse/TTree.g:34:3: ( keyser | expression )
            int alt3=2;
            switch ( input.LA(1) ) {
            case KEYSER:
                {
                alt3=1;
                }
                break;
            case ADD:
            case ID:
            case INT:
            case STRING:
                {
                alt3=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;

            }

            switch (alt3) {
                case 1 :
                    // parse/TTree.g:34:5: keyser
                    {
                    pushFollow(FOLLOW_keyser_in_stuff137);
                    keyser();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // parse/TTree.g:35:5: expression
                    {
                    pushFollow(FOLLOW_expression_in_stuff143);
                    expression();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "stuff"



    // $ANTLR start "keyser"
    // parse/TTree.g:38:1: keyser : ^( KEYSER SOZE ) ;
    public final void keyser() throws RecognitionException {
        try {
            // parse/TTree.g:39:3: ( ^( KEYSER SOZE ) )
            // parse/TTree.g:39:5: ^( KEYSER SOZE )
            {
            match(input,KEYSER,FOLLOW_KEYSER_in_keyser157); 

            match(input, Token.DOWN, null); 
            match(input,SOZE,FOLLOW_SOZE_in_keyser159); 

            match(input, Token.UP, null); 


             System.out.println("Found Keyser Soze!!"); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "keyser"



    // $ANTLR start "expression"
    // parse/TTree.g:43:1: expression : ( ^( ADD expression expression ) | ID | INT | STRING );
    public final void expression() throws RecognitionException {
        try {
            // parse/TTree.g:44:3: ( ^( ADD expression expression ) | ID | INT | STRING )
            int alt4=4;
            switch ( input.LA(1) ) {
            case ADD:
                {
                alt4=1;
                }
                break;
            case ID:
                {
                alt4=2;
                }
                break;
            case INT:
                {
                alt4=3;
                }
                break;
            case STRING:
                {
                alt4=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;

            }

            switch (alt4) {
                case 1 :
                    // parse/TTree.g:44:5: ^( ADD expression expression )
                    {
                    match(input,ADD,FOLLOW_ADD_in_expression180); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_expression_in_expression182);
                    expression();

                    state._fsp--;


                    pushFollow(FOLLOW_expression_in_expression184);
                    expression();

                    state._fsp--;


                    match(input, Token.UP, null); 


                    }
                    break;
                case 2 :
                    // parse/TTree.g:45:5: ID
                    {
                    match(input,ID,FOLLOW_ID_in_expression191); 

                    }
                    break;
                case 3 :
                    // parse/TTree.g:46:5: INT
                    {
                    match(input,INT,FOLLOW_INT_in_expression197); 

                    }
                    break;
                case 4 :
                    // parse/TTree.g:47:5: STRING
                    {
                    match(input,STRING,FOLLOW_STRING_in_expression203); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "expression"

    // Delegated rules


 

    public static final BitSet FOLLOW_SCRIPT_in_a114 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_stuff_in_a116 = new BitSet(new long[]{0x0000000000004718L});
    public static final BitSet FOLLOW_SCRIPT_in_a124 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_keyser_in_stuff137 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_stuff143 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KEYSER_in_keyser157 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_SOZE_in_keyser159 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ADD_in_expression180 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expression_in_expression182 = new BitSet(new long[]{0x0000000000004310L});
    public static final BitSet FOLLOW_expression_in_expression184 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ID_in_expression191 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_expression197 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_expression203 = new BitSet(new long[]{0x0000000000000002L});

}