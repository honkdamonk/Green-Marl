#include "common_main.h"
#include "dijkstra_adj.h"
#include "gm_rand.h"

int main(int argc, char** argv) {

    //    int num = atoi(argv[5]);
    //    printf("running with %d threads\n", num);
    gm_rt_set_num_threads(1); // gm_runtime.h

    gm_graph G;
    G.load_binary("test.bin");

    //------------------------------
    // Print graph details for manual verification 
    //------------------------------
    //    printf ("Number of nodes = %d\n", G.num_nodes());
    //    printf ("Number of edges = %d\n", G.num_edges());


    double* edge_costs = new double[G.num_edges()];
    for(int i = 0; i < G.num_edges(); i++) {
        edge_costs[i] = 1.0;
    }
    node_t* prev_nodes = new node_t[G.num_nodes()];
    edge_t* prev_edges = new edge_t[G.num_nodes()];
    gm_node_seq Q;

    //    node_t src_node_key = 199535084;
    //    node_t dst_node_key = 199926436;

    node_t src_node_key = rand() % G.num_nodes();// atol(argv[2]);
    node_t dst_node_key = rand() % G.num_nodes();// atol(argv[3]);

    node_t src_node_id = src_node_key; //G.nodekey_to_nodeid(src_node_key);
    node_t dst_node_id = dst_node_key; //G.nodekey_to_nodeid(dst_node_key);

    struct timeval T1, T2;    
    gettimeofday(&T1, NULL);
    // compute all shortest paths from root
    bool res = dijkstra(G, edge_costs, src_node_id, dst_node_id, prev_nodes, prev_edges);
    assert(res);
    printf("DONE\n");
    fflush(stdout);
    // get specific instance from root to end
    double total_cost = get_path(G, src_node_id, dst_node_id, prev_nodes, prev_edges, edge_costs, Q);
    gettimeofday(&T2, NULL);
    printf("GM DIJKSTRA - COMPUTATION RUNNING TIME (ms): %lf\n", (T2.tv_sec - T1.tv_sec) * 1000 + (T2.tv_usec - T1.tv_usec) * 0.001);
        

}
