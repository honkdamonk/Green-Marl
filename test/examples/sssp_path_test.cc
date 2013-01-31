#include "sssp_path.h"

int main(int argc, char** argv) {

    std::vector<VALUE_TYPE> node_schema, edge_schema;
    std::vector<void*> node_props, edge_props;

    node_schema.push_back(GMTYPE_INT);
    node_schema.push_back(GMTYPE_NODE);
    edge_schema.push_back(GMTYPE_INT);

    gm_graph G;
    G.load_edge_list("graphs/sssp_path_testgraph.txt", node_schema, edge_schema, node_props, edge_props);

    int* edgeCosts = (int*) edge_props.front();

    int* actual_dist = new int[G.num_nodes()];
    node_t* actual_prev = new node_t[G.num_nodes()];
    node_t root = 0;

    sssp_path(G, actual_dist, edgeCosts, root, actual_prev);

    int* target_dist = (int*) node_props.front();
    node_t* target_prev = (node_t*) node_props.back();

    for (int i = 0; i < G.num_nodes(); i++) {
        int actual_d = actual_dist[i];
        int target_d = target_dist[i];
        assert(actual_d == target_d || (actual_d == gm_get_max<int>() && target_d == -1));

        node_t actual_p = actual_prev[i];
        node_t target_p = target_prev[i];
        assert(actual_p == target_p);
    }
}
