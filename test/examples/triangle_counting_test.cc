#include "triangle_counting.h"

int main(int argc, char** argv) {

    std::vector<VALUE_TYPE> node_schema, edge_schema;
    std::vector<void*> node_props, edge_props;

    gm_graph G;
    G.load_edge_list("graphs/triangle_counting_testgraph.txt", node_schema, edge_schema, node_props, edge_props);

    int count = triangle_counting(G);

    assert(count == 15);

}
