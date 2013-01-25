#include "pagerank.h"

int main(int argc, char** argv) {

    std::vector<VALUE_TYPE> node_schema, edge_schema;
    std::vector<void*> node_props, edge_props;

    node_schema.push_back(GMTYPE_DOUBLE);

    gm_graph G;
    G.load_edge_list("graphs/pagerank_testgraph.txt", node_schema, edge_schema, node_props, edge_props);

    double e = 0.001;
    double d = 0.85;
    int max_iter = 1000;
    double* actual_rank = new double[G.num_nodes()];
    pagerank(G, e, d, max_iter, actual_rank);

    double* target_rank = (double*) node_props.front();
    double tolerance = 0.000001;

    for(int i = 0; i < G.num_nodes(); i++) {
        assert(std::abs(target_rank[i] - actual_rank[i]) < tolerance);
    }
}
