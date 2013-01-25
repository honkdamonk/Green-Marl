#include "common_main.h"
#include "bc_random.h"  // defined in generated
#include "gm_rand.h"
class my_main: public main_t
{
public:
    float* BC;

    ~my_main() {
        delete[] BC;
    }

    my_main() {
        BC = NULL;
    }

    virtual bool prepare() {

        std::vector<VALUE_TYPE> p1, p2;
        std::vector<void*> p3, p4;
        G.load_edge_list("mini.txt", p1, p2, p3, p4);

        BC = new float[G.num_nodes()];
        return true;
    }

    virtual bool run() {
        bc_random(G, BC, 10);
        return true;
    }

    virtual bool post_process() {
        for(int i = 0; i < G.num_nodes(); i++)
            printf("BC[%d] = %0.9lf\n", i, BC[i]);
        return true;
    }
};

int main(int argc, char** argv) {
    my_main M;
    M.main(argc, argv);
}
