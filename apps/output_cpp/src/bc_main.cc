#include "common_main.h"
#include "bc.h"  // defined in generated
#include "gm_rand.h"
class my_main: public main_t
{
public:
    gm_node_seq* Seeds;
    float* BC;

    virtual ~my_main() {
        delete[] BC;
        delete Seeds;
    }

    my_main() {
        Seeds = NULL;
        BC = NULL;
    }

    virtual bool prepare() {

        std::vector<VALUE_TYPE> p1, p2;
        std::vector<void*> p3, p4;
        G.load_edge_list("mini.txt", p1, p2, p3, p4);

        Seeds = new gm_node_seq();
        BC = new float[G.num_nodes()];
        return true;
    }

    virtual bool run() {
#ifdef NODE64
        gm_rand64 xorshift_rng;
#else
        gm_rand32 xorshift_rng;
#endif

        Seeds->push_back(0);
        Seeds->push_back(1);
        Seeds->push_back(2);
        Seeds->push_back(3);
        Seeds->push_back(4);
        Seeds->push_back(5);
        Seeds->push_back(6);

        comp_BC(G, BC, *Seeds);
        return true;
    }

    virtual bool post_process() {
        double min = 999999;
        double max = -1;

        for(int i = 0; i < G.num_nodes(); i++) {
            min = min > BC[i] ? BC[i] : min;
            max = max < BC[i] ? BC[i] : max;
        }

        double x[] = {0, 0.33, 0.53, 0.60, 0.53, 0.33, 0};

        printf("Min: %lf\tMax: %lf\tDiff: %lf\n", min, max , (max - min));

        for (int i = 0; i < G.num_nodes(); i++) {
            printf("BC[%d] = %0.9lf\t%lf\n", i, BC[i], (BC[i] - min) / (max - min));
        }

        return true;
    }
};

int main(int argc, char** argv) {
    my_main M;
    M.main(argc, argv);
}
