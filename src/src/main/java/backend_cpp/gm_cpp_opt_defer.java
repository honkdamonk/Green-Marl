package backend_cpp;

import ast.ast_foreach;
import ast.ast_procdef;
import inc.gm_compile_step;
import frontend.GlobalMembersGm_rw_analysis;
import frontend.gm_symtab_entry;

public class gm_cpp_opt_defer extends gm_compile_step {
  private gm_cpp_opt_defer() {
    set_description("Handle deferred writes");
  }

  public void process(ast_procdef proc) {
    java.util.LinkedList<gm_symtab_entry> S = new java.util.LinkedList<gm_symtab_entry>();
    java.util.LinkedList<ast_foreach> F = new java.util.LinkedList<ast_foreach>();
    boolean b = GlobalMembersGm_cpp_opt_defer.find_deferred_writes(proc, S, F); // return
                                                                                // found
                                                                                // defer
    if (b) {
      GlobalMembersGm_cpp_opt_defer.post_process_deferred_writes(S, F);

      GlobalMembersGm_rw_analysis.gm_redo_rw_analysis(proc.get_body());
    }

    set_affected(b);
  }

  @Override
  public gm_compile_step get_instance() {
    return new gm_cpp_opt_defer();
  }

  public static gm_compile_step get_factory() {
    return new gm_cpp_opt_defer();
  }
}