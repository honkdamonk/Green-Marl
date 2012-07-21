import ast.ast_node;
import frontend.gm_symtab_entry;

public class GlobalMembersGm_resolve_nc
{
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define TO_STR(X) #X
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define DEF_STRING(X) static const char *X = "X"
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

	//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	///#if false
	////--------------------------------------------------------------------------------
	//// If any subblock inside S defines a varaible v which
	//// has a name conflict with e, rename v with v'
	//// returns true if name conflict has happend at least once.
	////--------------------------------------------------------------------------------
	//bool gm_resolve_name_conflict(ast_sent *s, gm_symtab_entry *e, bool is_scalar);
	//
	//// apply class for resolve nc
	//class gm_resolve_nc_t : public gm_apply {
	//public:
	//    virtual bool apply(gm_symtab_entry* i, int symtab_type)
	//    {
	//        if (_for_scalar && (symtab_type != GM_SYMTAB_VAR)) return true;
	//        if (!_for_scalar && (symtab_type != GM_SYMTAB_FIELD)) return true;
	//
	//        if (i==_target_sym) return true;  // don't conflict with itself
	//
	//        char* name = i->getId()->get_orgname(); (assert(name!=NULL));
	//        if (gm_is_same_string(name, _target)) {
	//            _found = true;
	//
	//            // save conflicting symbol table entry
	//            //conflicts.push_back(i);
	//
	//            // rename symbol table entry
	//            const char* new_name = TEMP_GEN.getTempName(name);
	//            i->getId()->set_orgname(new_name);
	//            delete [] new_name;
	//        }
	//        return true;
	//    }
	//
	//public:
	//    bool replace_conflicting_symtab_entries(ast_sent* s, gm_symtab_entry *e, bool is_for_scalar)
	//    {
	//        _found = false;
	//        //_top = s;
	//        _for_scalar = is_for_scalar;
	//        _target = e->getId()->get_orgname(); assert(_target!=NULL);
	//        _target_sym = e;
	//
	//        // traverse for symtab entry
	//        set_all(false); set_for_symtab(true);
	//        s->traverse_pre(this);
	//        set_all(false);
	//
	//        return _found;
	//    }
	//    //void rename_all() {
	//    //    std::list<gm_symtab_entry*>::iterator i;
	//    //    for(i=conflicts.begin(); i != conflicts.end(); i++)  {
	//    //        gm_reflect_symbol_entry_name(*i, _top);
	//    //    }
	//    //    conflicts.clear();
	//    //}
	//
	//protected:
	//    //std::list<gm_symtab_entry*> conflicts;
	//    //ast_sent* _top;
	//    bool _found;
	//    bool _for_scalar;
	//    char* _target;
	//    gm_symtab_entry* _target_sym;
	//};
	//bool gm_resolve_name_conflict(ast_sent *s, gm_symtab_entry *e, bool is_scalar)
	//{
	//    // return true if there was a conflict
	//    // return false otherwise
	//
	//    gm_resolve_nc_t R;
	//    bool ret = R.replace_conflicting_symtab_entries(s, e, is_scalar);
	//    //if (ret) R.rename_all();
	//    return ret;
	//}
	///#endif

	//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	///#if false
	////------------------------------------------------------------------------------------
	//// For the subtree top,
	////   Reflect the name in the symbol-table e (which has been modifid), to all id nodes
	////------------------------------------------------------------------------------------
	////bool gm_reflect_symbol_entry_name(gm_symtab_entry *e_modified, ast_node* top);
	//class gm_reflect_symbol_entry_name_t : public gm_apply {
	//public:
	//    virtual bool apply(ast_id* i)
	//    {
	//        assert(_curr != NULL);
	//        assert(i->getSymInfo() != NULL);
	//        if (i->getSymInfo() == _curr) {
	//            char* new_name = i->getSymInfo()->getId()->get_orgname();
	//            i->set_orgname(new_name); // old name is deleted. new name is copied
	//        }
	//        return true;
	//    }
	//    bool is_changed() {return _changed;}
	//    void do_reflect(gm_symtab_entry *e_modified, ast_node* top) {
	//        set_all(false); set_for_id(true);
	//        _curr = e_modified; _changed = false;
	//        top->traverse(this, GM_PRE_APPLY);
	//    }
	//
	//protected:
	//    gm_symtab_entry* _curr;
	//    bool _changed;
	//};
	//bool gm_reflect_symbol_entry_name(gm_symtab_entry *e_modified, ast_node* top)
	//{
	//    gm_reflect_symbol_entry_name_t T;
	//    T.do_reflect(e_modified, top);
	//    return T.is_changed();
	//}
	///#endif

	//---------------------------------------------------------------------------------------
	// For the subtree(top), 
	// replace any id's symbol entry refrence e_old into e_new.
	// If the new symbol has different orgname() from the old one, modify the name in the id node as well.
	// (Assumption. e_new is a valid symbol entry that does not break scoping rule)
	//---------------------------------------------------------------------------------------
	public static boolean gm_replace_symbol_entry(gm_symtab_entry e_old, gm_symtab_entry e_new, ast_node top)
	{
		gm_replace_symbol_entry_t T = new gm_replace_symbol_entry_t();
		T.do_replace(e_old, e_new, top);
		return T.is_changed();
	}
}