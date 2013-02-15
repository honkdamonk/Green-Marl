#include <stdio.h>
#include <algorithm>
#include <vector>
#include "gm_traverse.h"
#include "gm_builtin.h"

#include "gm_backend_cpp_opt_steps.h"

class select_seq_impl_t: public gm_apply
{
private:
    std::vector<gm_symtab_entry*> candidates;

public:
    virtual bool apply(gm_symtab_entry* e, int symtab_type) {
        if (symtab_type != GM_SYMTAB_VAR) return true;
        if (gm_is_sequence_collection_type(e->getType()->getTypeSummary())) {
            printf("Add:\t%s : %s\n", e->getId()->get_genname(), gm_get_type_string(e->getType()->getTypeSummary()));
            candidates.push_back(e);
        }
        return true;
    }

    virtual bool apply(ast_expr* expr) {
        if (expr->get_nodetype() != AST_EXPR_BUILTIN) return true;

        ast_expr_builtin* builtIn = (ast_expr_builtin*) expr;
        gm_symtab_entry* driverEntry = builtIn->get_driver()->getSymInfo();

        std::vector<gm_symtab_entry*>::iterator position =std::find(candidates.begin(), candidates.end(), driverEntry);
        if (position == candidates.end()) return true;

        gm_builtin_def* def = builtIn->get_builtin_def();
        gm_method_id_t methodId = (gm_method_id_t) def->get_method_id();

        switch(methodId) {
            case GM_BLTIN_SET_ADD:
            case GM_BLTIN_SET_REMOVE:
            case GM_BLTIN_SET_PEEK:
                printf("Erase: %s.%s\t%d\n", builtIn->get_driver()->get_genname(), builtIn->get_callname(), def->get_method_id());
                candidates.erase(position);
                break;
        }
        return true;
    }

};



void gm_cpp_opt_select_seq_implementation::process(ast_procdef* p) {
    select_seq_impl_t x;
    gm_traverse_symtabs(p, &x, false);
    gm_traverse_exprs(p, &x, false);
}
