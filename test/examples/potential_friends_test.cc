#include "potential_friends.h"
#include <set>
#include <fstream>
#include <iostream>
#include <string.h>

std::set<int>* loadPrecomputedResults(size_t nodeCount) {

    std::set<int>* targetSets = new std::set<int>[nodeCount];

    std::ifstream inputFileStream;
    inputFileStream.open("data/potential_friends.txt");
    assert(inputFileStream.is_open());

    node_t currentNode = 0;

    int maxSize = 1024;
    char lineData[maxSize];

    while (!inputFileStream.eof()) {
        inputFileStream.getline(lineData, maxSize);

        char* p = strtok(lineData, " ");
        p = strtok(NULL, " ");
        p = strtok(NULL, " ");
        while(p != NULL) {
            node_t x;
            sscanf(p, "%d", &x);
            targetSets[currentNode].insert(x);
            p = strtok(NULL, " ");
        }
        currentNode++;
    }
    return targetSets;
}

int main(int argc, char** argv) {

    std::vector<VALUE_TYPE> node_schema, edge_schema;
    std::vector<void*> node_props, edge_props;

    gm_graph G;
    G.load_edge_list("graphs/potential_friends_testgraph.txt", node_schema, edge_schema, node_props, edge_props);

    set<int>* targetSets = loadPrecomputedResults(G.num_nodes());

    gm_property_of_collection_impl<gm_node_set, false> resultSets(G.num_nodes());
    potential_friends(G, resultSets);

    for(node_t i = 0; i < G.num_nodes(); i++) {
        std::set<int> targetSet = targetSets[i];
        gm_node_set actualSet = resultSets[i];

        assert(targetSet.size() == actualSet.get_size());

        gm_node_set::seq_iter actualII = actualSet.prepare_seq_iteration();
        while(actualII.has_next()) {
            node_t node = actualII.get_next();
            assert(targetSet.find(node) != targetSet.end());
        }
    }
    delete[] targetSets;
    return 0;
}
