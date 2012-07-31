#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <algorithm>
#include "gm.h"
#include <omp.h>
#include <sys/time.h>

using namespace std;

#define READ true
#define WRITE false

class MapBenchmark {

private:
    int operationCount;
    bool* operations;
    int* keys;
    int* values;
    gm_map<int, int>* map;

public:
    inline void run() {
        #pragma omp parallel for
        for(int i = 0; i < operationCount; i++) {
            if(operations[i] == READ) {
                int x = map->getValue(keys[i]);
            } else {
                map->setValue_par(keys[i], values[i]);
            }
        }
    }

    MapBenchmark(gm_map<int, int>* sut, int opCount, bool* ops, int* keys, int* values) : map(sut), operationCount(opCount), operations(ops), keys(keys), values(values) {
    }



};

bool getType() {
    const int read = 1;
    const int write = 1;
    int sum = read + write;
    if(rand() % sum < read) return READ;
    else return WRITE;
}

int main(int argc, char** argv)  {
        int threadCount = atoi(argv[1]);
        int operationCount = atoi(argv[2]);

        bool* operations = new bool[operationCount];
        int* keys = new int[operationCount];
        int* values = new int[operationCount];
        for(int i = 0; i < operationCount; i++) {
            operations[i] = getType();
            keys[i]= rand();
            values[i] = rand();
        }

        gm_map_small<int, int, 0> smallMap;
        MapBenchmark benchmark_small((gm_map<int, int>*)(&smallMap), operationCount, operations, keys, values);
        struct timeval T1, T2;
        gettimeofday(&T1, NULL);
        benchmark_small.run();
        gettimeofday(&T2, NULL);
        printf("small\t%lf\t%d\t%d\n", 1000 * (T2.tv_sec - T1.tv_sec) + 0.001 * (T2.tv_usec - T1.tv_usec), threadCount, operationCount);

        gm_map_small<int, int, 0> medMap;
        MapBenchmark benchmark_med((gm_map<int, int>*)(&medMap), operationCount, operations, keys, values);
        gettimeofday(&T1, NULL);
        benchmark_med.run();
        gettimeofday(&T2, NULL);
        printf("medium\t%lf\t%d\t%d\n", 1000 * (T2.tv_sec - T1.tv_sec) + 0.001 * (T2.tv_usec - T1.tv_usec), threadCount, operationCount);

        return 0;
}
