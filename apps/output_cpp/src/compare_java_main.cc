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

struct Task {
    int current;
    int end;
};

bool* operations;
int* keys;
int* values;

class MapBenchmark {

private:
    int threadCount;
    Task* tasks;
    gm_map<int, int>* map;

public:
    inline void run() {
        #pragma omp parallel for
        for(int i = 0; i < threadCount; i++) {
            Task& t = tasks[i];
            int current = t.current;
            int end = t.end;
            while(current < end) {
                if(operations[current] == READ) {
                    int x = map->getValue(keys[current]);
                } else {
                    map->setValue_par(keys[current], values[current]);
                }
                current++;
            }
        }
    }

    MapBenchmark(gm_map<int, int>* sut, int opCount, Task* tasks) : map(sut), threadCount(opCount), tasks(tasks) {
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

        operations = new bool[operationCount];
        keys = new int[operationCount];
        values = new int[operationCount];
        for(int i = 0; i < operationCount; i++) {
            operations[i] = getType();
            keys[i]= rand();
            values[i] = rand();
        }

        Task* tasks = new Task[threadCount];
        int step = operationCount / threadCount;
        int from = 0;
        int to = step;
        for(int i = 0; i < threadCount; i++) {
            tasks[i].current = from;
            tasks[i].end = to;
            from = to;
            to += step;
        }

        gm_map_small<int, int, 0> smallMap;
        MapBenchmark benchmark_small((gm_map<int, int>*)(&smallMap), threadCount, tasks);
        struct timeval T1, T2;
        gettimeofday(&T1, NULL);
        benchmark_small.run();
        gettimeofday(&T2, NULL);
        printf("small\t%lf\t%d\t%d\n", 1000 * (T2.tv_sec - T1.tv_sec) + 0.001 * (T2.tv_usec - T1.tv_usec), threadCount, operationCount);

        gm_map_medium<int, int, 0> medMap(threadCount);
        MapBenchmark benchmark_med((gm_map<int, int>*)(&medMap), threadCount, tasks);
        gettimeofday(&T1, NULL);
        benchmark_med.run();
        gettimeofday(&T2, NULL);
        printf("medium\t%lf\t%d\t%d\n", 1000 * (T2.tv_sec - T1.tv_sec) + 0.001 * (T2.tv_usec - T1.tv_usec), threadCount, operationCount);

        return 0;
}
