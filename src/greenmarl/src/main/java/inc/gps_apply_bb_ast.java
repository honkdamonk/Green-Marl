package inc;

import ast.AST_NODE_TYPE;
import ast.ast_expr;
import ast.ast_foreach;
import ast.ast_if;
import ast.ast_sent;
import ast.ast_while;
import backend_cpp.*;
import backend_giraph.*;
import backend_gps.gm_gps_basic_block;
import backend_gps.gm_gps_bbtype_t;
import backend_gps.gm_gps_comm_t;
import backend_gps.gm_gps_comm_unit;
import common.*;
import frontend.*;
import opt.*;
import tangible.*;

//C++ TO JAVA CONVERTER TODO TASK: Multiple inheritance is not available in Java:
public class gps_apply_bb_ast extends gm_apply, gps_apply_bb
{
	public gps_apply_bb_ast()
	{
		this._curr = null;
		this._under_receiver = false;
		this._is_post = false;
		this._is_pre = true;
		this._receiver_type = gm_gps_comm_t.GPS_COMM_NESTED.getValue();
		this._check_receiver = true;
	}

	// defined in gm_gps_misc.cc
	public void apply(gm_gps_basic_block b)
	{
		_curr = b;
		int type = _curr.get_type();
		//printf("visiting :%d\n", _curr->get_id());
		if (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_SEQ.getValue())
		{
			// traverse sentence block and apply this
			_curr.prepare_iter();
			ast_sent s;
			s = _curr.get_next();
			while (s != null)
			{
				s.traverse(this, is_post(), is_pre());
				s = _curr.get_next();
			}
		}
		else if (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_BEGIN_VERTEX.getValue())
		{
    
			// traverse receiver
			if (_curr.has_receiver() && is_check_receiver())
			{
				java.util.LinkedList<gm_gps_comm_unit> R = _curr.get_receivers();
				java.util.Iterator<gm_gps_comm_unit> I;
				set_under_receiver_traverse(true);
				for (I = R.iterator(); I.hasNext();)
				{
					gm_gps_comm_unit U = I.next();
					set_receiver_type(U.get_type());
					if (U.get_type() == gm_gps_comm_t.GPS_COMM_NESTED)
					{
						ast_foreach fe = U.fe;
						fe.traverse(this, is_post(), is_pre());
					}
					else if (U.get_type() == gm_gps_comm_t.GPS_COMM_RANDOM_WRITE)
					{
						gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();
						java.util.LinkedList<ast_sent> LL = info.get_random_write_sents(U);
						java.util.Iterator<ast_sent> II;
						for (II = LL.iterator(); II.hasNext();)
						{
							ast_sent s = II.next();
							s.traverse(this, is_post(), is_pre());
						}
					}
					else
					{
						assert false;
					}
				}
				set_under_receiver_traverse(false);
			}
    
			// traverse body
			if (_curr.get_num_sents() == 0)
				return;
    
			java.util.LinkedList<ast_sent> sents = _curr.get_sents();
			java.util.Iterator<ast_sent> I;
			for (I = sents.iterator(); I.hasNext();)
			{
				ast_sent s = I.next();
				assert s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH;
				ast_foreach fe = (ast_foreach) s;
				fe.traverse(this, is_post(), is_pre());
			}
		}
		else if (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_IF_COND.getValue())
		{
			assert _curr.get_num_sents() == 1;
			// traverse cond expr
			java.util.LinkedList<ast_sent> sents = new java.util.LinkedList<ast_sent>();
			ast_sent s = _curr.get_1st_sent();
			assert s.get_nodetype() == AST_NODE_TYPE.AST_IF;
			ast_if i = (ast_if) s;
			ast_expr c = i.get_cond();
    
			c.traverse(this, is_post(), is_pre());
		}
		else if (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_WHILE_COND.getValue())
		{
			assert _curr.get_num_sents() == 1;
    
			// traverse cond expr
			java.util.LinkedList<ast_sent> sents = new java.util.LinkedList<ast_sent>();
			ast_sent s = _curr.get_1st_sent();
			assert s.get_nodetype() == AST_NODE_TYPE.AST_WHILE;
			ast_while w = (ast_while) s;
			ast_expr c = w.get_cond();
    
			c.traverse(this, is_post(), is_pre());
		}
		else if (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE1.getValue())
		{
			// nothing
    
		}
		else if (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE2.getValue())
		{
			// nothing
		}
		else if (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_MERGED_TAIL.getValue())
		{
			// nothing
		}
		else if (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_MERGED_IF.getValue())
		{
			// nothing
		}
		else
		{
			assert false;
		}
    
	}
	public final gm_gps_basic_block get_curr_BB()
	{
		return _curr;
	}
	public final void set_is_post(boolean b)
	{
		_is_post = b;
	}
	public final void set_is_pre(boolean b)
	{
		_is_pre = b;
	}
	public final boolean is_post()
	{
		return _is_post;
	}
	public final boolean is_pre()
	{
		return _is_pre;
	}
	public final boolean is_check_receiver()
	{
		return _check_receiver;
	}
	public final void set_check_receiver(boolean b)
	{
		_check_receiver = b;
	}

	// set by traverse engine
	protected gm_gps_basic_block _curr;
	protected boolean _under_receiver;
	protected boolean _is_post;
	protected boolean _is_pre;
	protected boolean _check_receiver;
	protected int _receiver_type; // GPS_COMM_NESTED, COMM_RAND_WRITE

	protected final boolean is_under_receiver_traverse()
	{
		return _under_receiver;
	}
	protected final void set_under_receiver_traverse(boolean b)
	{
		_under_receiver = b;
	}
	protected final boolean get_receiver_type()
	{
		return _receiver_type;
	}
	protected final void set_receiver_type(int i)
	{
		_receiver_type = i;
	}

}