package common;

import ast.ast_procdef;
import inc.gm_procinfo;

public class gm_prepare_genname
{

	//--------------------------------------------------------
	// Prepare Gen names
	//   (1) Crearte Gen-name out of Orginal name
	//   (2) Each symbol is checked against language vocaburary
	//       and renamed if required  
	//--------------------------------------------------------
	public static void gm_prepare_genname(ast_procdef p, gm_vocabulary lang_voca)
	{
		assert lang_voca != null;
		assert p != null;
		gm_procinfo info = gm_main.FE.get_proc_info(p);
		assert info != null;
		gm_prepare_genname_T T = new gm_prepare_genname_T(info, lang_voca);

		info.clear_voca(); // clear vocabulary
		p.traverse_pre(T); // rebuild vocabulary. create gen-names
	}
}