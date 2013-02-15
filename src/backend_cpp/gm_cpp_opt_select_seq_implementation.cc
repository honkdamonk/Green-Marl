#include <stdio.h>
#include "gm_traverse.h"

class select_seq_impl_t: public gm_apply
{
public:
    virtual bool apply(ast_expr_builtin* builtIn) {
        return false;
    }

    virtual bool apply(ast_vardecl* varDecl) {
        return false;
    }
};
