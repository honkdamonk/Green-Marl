#include "sssp_path_adj.h"

int main(int argc, char** argv) {

    std::vector<VALUE_TYPE> node_schema, edge_schema;
    std::vector<void*> node_props, edge_props;

    node_schema.push_back(GMTYPE_DOUBLE);
    node_schema.push_back(GMTYPE_NODE);
    node_schema.push_back(GMTYPE_EDGE);
    edge_schema.push_back(GMTYPE_DOUBLE);

    gm_graph G;
    G.load_edge_list("graphs/test.txt", node_schema, edge_schema, node_props, edge_props);

    double* edgeCosts = (double*) edge_props[0];

    double* actual_dist = new double[G.num_nodes()];
    node_t* actual_prev = new node_t[G.num_nodes()];
    edge_t* actual_prev_e = new edge_t[G.num_nodes()];
    node_t root = 0;
    node_t end = 4;

    sssp_path(G, actual_dist, edgeCosts, root, end, actual_prev, actual_prev_e);

    double* target_dist = (double*) node_props[0];
    node_t* target_prev = (node_t*) node_props[1];
    edge_t* target_prev_e = (edge_t*) node_props[2];

    const double tolerance = 0.000005;

    for (node_t i = 0; i < G.num_nodes(); i++) {
        double actual_d = actual_dist[i];
        double target_d = target_dist[i];

        double diff = std::abs(actual_d - target_d);
        printf("%d: %lf %lf\n", i, actual_d, target_d);
        assert(diff < tolerance || (actual_d == gm_get_max<int>() && target_d == -1));

        node_t actual_p = actual_prev[i];
        node_t target_p = target_prev[i];
        printf("%d: %d %d\n", i, actual_p, target_p);
        assert(actual_p == target_p);
    }

    for(node_t i = 0; i < G.num_nodes(); i++) {
        edge_t actual_e = actual_prev_e[i];
        edge_t target_e = target_prev_e[i];
        if(actual_e != target_e) {
            printf("%d: %d %d\n", i, actual_e, target_e);
            assert(actual_e != target_e);
        }
    }
}
