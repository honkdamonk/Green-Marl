#include "sssp.h"

int main(int argc, char** argv) {

    std::vector<VALUE_TYPE> node_schema, edge_schema;
    std::vector<void*> node_props, edge_props;

    node_schema.push_back(GMTYPE_INT);
    edge_schema.push_back(GMTYPE_INT);

    gm_graph G;
    G.load_edge_list("graphs/sssp_testgraph.txt", node_schema, edge_schema, node_props, edge_props);

    int* edgeCosts = (int*) edge_props.front();

    int* actual_dist = new int[G.num_nodes()];
    node_t root = 0;

    sssp(G, actual_dist, edgeCosts, root);

    int* target_dist = (int*) node_props.front();

    for(int i = 0; i < G.num_nodes(); i++) {
        int actual_d = actual_dist[i];
        int target_d = target_dist[i];
        assert(actual_d == target_d || (actual_d ==  gm_get_max<int>() && target_d == -1));
    }

}
