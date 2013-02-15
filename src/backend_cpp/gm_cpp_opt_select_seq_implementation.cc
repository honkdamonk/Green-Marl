#include <stdio.h>
#include <algorithm>
#include <vector>
#include "gm_traverse.h"
#include "gm_builtin.h"

#include "gm_backend_cpp_opt_steps.h"

class sequence_front_usage_filter: public gm_apply
{
private:
    std::vector<gm_symtab_entry*> candidates;

public:
    std::vector<gm_symtab_entry*> get_candidates() {
        return candidates;
    }

    virtual bool apply(gm_symtab_entry* e, int symtab_type) {
        if (symtab_type != GM_SYMTAB_VAR) return true;
        if (gm_is_sequence_collection_type(e->getType()->getTypeSummary())) {
            candidates.push_back(e);
        }
        return true;
    }

    virtual bool apply(ast_expr* expr) {
        if (expr->get_nodetype() != AST_EXPR_BUILTIN) return true;

        ast_expr_builtin* builtIn = (ast_expr_builtin*) expr;
        gm_symtab_entry* driverEntry = builtIn->get_driver()->getSymInfo();

        std::vector<gm_symtab_entry*>::iterator position = std::find(candidates.begin(), candidates.end(), driverEntry);
        if (position == candidates.end()) return true;

        gm_builtin_def* def = builtIn->get_builtin_def();
        gm_method_id_t methodId = (gm_method_id_t) def->get_method_id();

        switch (methodId) {
            case GM_BLTIN_SET_ADD_BACK:
            case GM_BLTIN_SET_REMOVE_BACK:
            case GM_BLTIN_SET_PEEK_BACK:
                candidates.erase(position);
                break;
        }
        return true;
    }

};

class seq_front_to_back_transformer: public gm_apply
{
private:
    std::vector<gm_symtab_entry*> candidates;

public:
    seq_front_to_back_transformer(std::vector<gm_symtab_entry*> candidate_seqs) :
            candidates(candidate_seqs) {
    }

    virtual bool apply(ast_expr* expr) {
        if (expr->get_nodetype() != AST_EXPR_BUILTIN) return true;

        ast_expr_builtin* builtIn = (ast_expr_builtin*) expr;
        gm_symtab_entry* driverEntry = builtIn->get_driver()->getSymInfo();

        std::vector<gm_symtab_entry*>::iterator position = std::find(candidates.begin(), candidates.end(), driverEntry);
        if (position == candidates.end()) return true;

        gm_builtin_def* def = builtIn->get_builtin_def();
        gm_method_id_t methodId = (gm_method_id_t) def->get_method_id();
        gm_method_id_t newMethodId = methodId;

        switch (methodId) {
            case GM_BLTIN_SET_ADD:
                newMethodId = GM_BLTIN_SET_ADD_BACK;
                break;
            case GM_BLTIN_SEQ_POP_FRONT:
                newMethodId = GM_BLTIN_SET_REMOVE_BACK;
                break;
            case GM_BLTIN_SET_PEEK:
                newMethodId = GM_BLTIN_SET_PEEK_BACK;
                break;
        }

        if (methodId != newMethodId) {
            gm_builtin_manager manager;
            gm_builtin_def* newBuiltInDef = BUILT_IN.find_builtin_def(def->get_source_type_summary(), newMethodId, 0);
            builtIn->set_builtin_def(newBuiltInDef);
        }

        return true;
    }
};

class sequence_back_usage_filtler: public gm_apply
{
private:
    std::vector<gm_symtab_entry*> candidates;

public:
    std::vector<gm_symtab_entry*> get_candidates() {
        return candidates;
    }

    virtual bool apply(gm_symtab_entry* e, int symtab_type) {
        if (symtab_type != GM_SYMTAB_VAR) return true;
        if (gm_is_sequence_collection_type(e->getType()->getTypeSummary())) {
            candidates.push_back(e);
        }
        return true;
    }

    virtual bool apply(ast_expr* expr) {
        if (expr->get_nodetype() != AST_EXPR_BUILTIN) return true;

        ast_expr_builtin* builtIn = (ast_expr_builtin*) expr;
        gm_symtab_entry* driverEntry = builtIn->get_driver()->getSymInfo();

        std::vector<gm_symtab_entry*>::iterator position = std::find(candidates.begin(), candidates.end(), driverEntry);
        if (position == candidates.end()) return true;

        gm_builtin_def* def = builtIn->get_builtin_def();
        gm_method_id_t methodId = (gm_method_id_t) def->get_method_id();

        switch (methodId) {
            case GM_BLTIN_SET_ADD:
            case GM_BLTIN_SEQ_POP_FRONT:
            case GM_BLTIN_SET_PEEK:
                candidates.erase(position);
                break;
        }
        return true;
    }

};

class sequence_to_vector_transformer
{
private:
    std::vector<gm_symtab_entry*> candidates;

public:
    sequence_to_vector_transformer(std::vector<gm_symtab_entry*> candidate_seqs) :
            candidates(candidate_seqs) {
    }

    void transform_all() {
        std::vector<gm_symtab_entry*>::iterator II;
        for (II = candidates.begin(); II != candidates.end(); II++) {
            gm_symtab_entry* entry = *II;
            entry->add_info_bool("seq_vector", true);
        }
    }
};

void gm_cpp_opt_select_seq_implementation::process(ast_procdef* p) {
    sequence_front_usage_filter frontFilter;
    gm_traverse_symtabs(p, &frontFilter, false);
    gm_traverse_exprs(p, &frontFilter, false);
    std::vector<gm_symtab_entry*> frontCandidates = frontFilter.get_candidates();
    seq_front_to_back_transformer frontToBack(frontCandidates);
    gm_traverse_exprs(p, &frontToBack, false);

    sequence_back_usage_filtler transformFilter;
    gm_traverse_symtabs(p, &transformFilter, false);
    gm_traverse_exprs(p, &transformFilter, false);
    std::vector<gm_symtab_entry*> transformCandidates = transformFilter.get_candidates();

    sequence_to_vector_transformer transformer(transformCandidates);
    transformer.transform_all();

}
