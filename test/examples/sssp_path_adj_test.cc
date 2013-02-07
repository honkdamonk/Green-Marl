#include "sssp_path_adj.h"
#include <limits.h>

int main(int argc, char** argv) {

    std::vector<VALUE_TYPE> node_schema, edge_schema;
    std::vector<void*> node_props, edge_props;

    node_schema.push_back(GMTYPE_DOUBLE);
    node_schema.push_back(GMTYPE_NODE);
    node_schema.push_back(GMTYPE_EDGE);
    edge_schema.push_back(GMTYPE_DOUBLE);

    gm_graph G;
    G.load_edge_list("graphs/sssp_path_adj_testgraph.txt", node_schema, edge_schema, node_props, edge_props);

    double* edgeCosts = (double*) edge_props[0];
    double* actual_dist;
    node_t* actual_prev;
    edge_t* actual_prev_e;

    node_t root = 0;

    double* target_dist = (double*) node_props[0];
    node_t* target_prev = (node_t*) node_props[1];
    edge_t* target_prev_e = (edge_t*) node_props[2];

    for (node_t end = 0; end < G.num_nodes(); end++) {

        actual_dist = new double[G.num_nodes()];
        actual_prev = new node_t[G.num_nodes()];
        actual_prev_e = new edge_t[G.num_nodes()];

        sssp_path(G, actual_dist, edgeCosts, root, end, actual_prev, actual_prev_e);

        const double tolerance = 0.000005;

        node_t x = end;
        while (x != root) {
            double actual_d = actual_dist[x];
            double target_d = target_dist[x];
            double diff = std::abs(actual_d - target_d);
            assert(diff < tolerance || (actual_d == gm_get_max<double>() && target_d == -1));

            node_t actual_p = actual_prev[x];
            node_t target_p = target_prev[x];
            assert(actual_p == target_p);
            //the test could crash here though its correct if there is another shortest path
            //but honestyl the likelyhood is very very low ;)

            edge_t actual_e = actual_prev_e[x];
            edge_t target_e = target_prev_e[x];
            assert(actual_e == target_e);

            x = target_prev[x];
        }
    }
}
