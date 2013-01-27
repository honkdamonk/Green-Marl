#include "kosaraju.h"
#include <map>

int main(int argc, char** argv) {

    std::vector<VALUE_TYPE> node_schema, edge_schema;
    std::vector<void*> node_props, edge_props;

    node_schema.push_back(GMTYPE_INT);

    gm_graph G;
    G.load_edge_list("graphs/kosaraju_testgraph.txt", node_schema, edge_schema, node_props, edge_props);

    int* actual_memb = new int[G.num_nodes()];
    int num_components = kosaraju(G, actual_memb);

    int* target_memb = (int*) node_props.front();

    std::map<int, int> mapping;

    for(node_t node = 0; node < G.num_nodes(); node++) {
        int actual = actual_memb[node];
        int target = target_memb[node];

        if(mapping.find(actual) == mapping.end()) {
            mapping[actual] = target;
        } else {
            assert(mapping[actual] == target);
        }
    }
}
