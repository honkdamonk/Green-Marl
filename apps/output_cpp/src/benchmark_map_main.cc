#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <algorithm>
#include "gm.h"
#include <omp.h>
#include <sys/time.h>

using namespace std;

typedef int Key;
typedef int Value;

bool debug = true;
int threadCount;
size_t runSize;

class Benchmark
{
private:
    gm_map<Key, Value>* map;
    const Value* values;
    const size_t size;
    const char* type;
    static const int iteration = 1000;

    typedef pair<time_t, time_t> Time;
    typedef pair<const char*, Time> Result;
    list<Result> results;

    void printDebug(const char* message) {
        if(debug) {
            printf("%s", message);
            fflush(stdout);
        }
    }

    void insert() {
        printDebug("insert...");
        insert_seq();
        printDebug("...");
        map->clear();
        insert_par();
        printDebug("finished\n");
    }

    void insert_seq() {
        struct timeval T1, T2;
        gettimeofday(&T1, NULL);
        for (Key key = 0; key < size; key++) {
            map->setValue(key, values[key]);
        }
        gettimeofday(&T2, NULL);
        results.push_back(Result("insert_seq", Time(T2.tv_sec - T1.tv_sec, T2.tv_usec - T1.tv_usec)));
    }

    void insert_par() {
        struct timeval T1, T2;
        gettimeofday(&T1, NULL);
        #pragma omp parallel for
        for (Key key = 0; key < size; key++) {
            map->setValue(key, values[key]);
        }
        gettimeofday(&T2, NULL);
        results.push_back(Result("insert_par", Time(T2.tv_sec - T1.tv_sec, T2.tv_usec - T1.tv_usec)));
    }

    void read() {
        printDebug("read...");
        read_seq();
        printDebug("...");
        read_par();
        printDebug("finished\n");
    }

    void read_seq() {
        struct timeval T1, T2;
        gettimeofday(&T1, NULL);
        for (Key key = 0; key < size; key++) {
            Value value = map->getValue(key);
            assert(value == values[key]);
        }
        gettimeofday(&T2, NULL);
        results.push_back(Result("read_seq", Time(T2.tv_sec - T1.tv_sec, T2.tv_usec - T1.tv_usec)));
    }

    void read_par() {
        struct timeval T1, T2;
        gettimeofday(&T1, NULL);
        #pragma omp parallel for
        for (Key key = 0; key < size; key++) {
            Value value = map->getValue(key);
            assert(value == values[key]);
        }
        gettimeofday(&T2, NULL);
        results.push_back(Result("read_par", Time(T2.tv_sec - T1.tv_sec, T2.tv_usec - T1.tv_usec)));
    }

    void hasKey() {
        printDebug("hasKey...");
        hasKey_seq();
        printDebug("...");
        hasKey_par();
        printDebug("finished\n");
    }

    void hasKey_seq() {
        struct timeval T1, T2;
        gettimeofday(&T1, NULL);
        for (int i = 0; i < iteration; i++) {
            Key key = rand();
            assert(map->hasKey(key) == (key < size));
        }
        gettimeofday(&T2, NULL);
        results.push_back(Result("hasKey_seq", Time(T2.tv_sec - T1.tv_sec, T2.tv_usec - T1.tv_usec)));
    }

    void hasKey_par() {
        struct timeval T1, T2;
        gettimeofday(&T1, NULL);
        #pragma omp parallel for
        for (int i = 0; i < iteration; i++) {
            Key key = rand();
            assert(map->hasKey(key) == (key < size));
        }
        gettimeofday(&T2, NULL);
        results.push_back(Result("hasKey_par", Time(T2.tv_sec - T1.tv_sec, T2.tv_usec - T1.tv_usec)));
    }

    void getMinKey() {
        printDebug("getMinKey...");
        getMinKey_seq();
        printDebug("...");
        getMinKey_par();
        printDebug("finished\n");
    }

    void getMinKey_seq() {
        struct timeval T1, T2;
        gettimeofday(&T1, NULL);
        Key minKey = map->getMinKey();
        for (int i = 0; i < iteration; i++) {
            assert(minKey == map->getMinKey());
        }
        gettimeofday(&T2, NULL);
        results.push_back(Result("getMinKey_seq", Time(T2.tv_sec - T1.tv_sec, T2.tv_usec - T1.tv_usec)));
    }

    void getMinKey_par() {
        struct timeval T1, T2;
        gettimeofday(&T1, NULL);
        Key minKey = map->getMinKey();
        #pragma omp parallel for
        for (int i = 0; i < iteration; i++) {
            assert(minKey == map->getMinKey());
        }
        gettimeofday(&T2, NULL);
        results.push_back(Result("getMinKey_par", Time(T2.tv_sec - T1.tv_sec, T2.tv_usec - T1.tv_usec)));
    }

    void getMinValue() {
        printDebug("getMinValue...");
        getMinValue_seq();
        printDebug("...");
        getMinValue_par();
        printDebug("finished\n");
    }

    void getMinValue_seq() {
        struct timeval T1, T2;
        gettimeofday(&T1, NULL);
        Value minValue = map->getMinValue();
        for (int i = 0; i < iteration; i++) {
            assert(minValue == map->getMinValue());
        }
        gettimeofday(&T2, NULL);
        results.push_back(Result("getMinValue_seq", Time(T2.tv_sec - T1.tv_sec, T2.tv_usec - T1.tv_usec)));
    }

    void getMinValue_par() {
        struct timeval T1, T2;
        gettimeofday(&T1, NULL);
        Value minValue = map->getMinValue();
        #pragma omp parallel for
        for (int i = 0; i < iteration; i++) {
            assert(minValue == map->getMinValue());
        }
        gettimeofday(&T2, NULL);
        results.push_back(Result("getMinValue_par", Time(T2.tv_sec - T1.tv_sec, T2.tv_usec - T1.tv_usec)));
    }


public:
    Benchmark(gm_map<Key, Value>* sut, Value* values, size_t size, const char* type) :
            map(sut), values(values), size(size), type(type) {
        srand(1);
    }

    void start() {
        insert();
        read();
        hasKey();
        getMinKey();
        getMinValue();
    }

    void printResults() {
        printf("Map_Type\tOperation\tTime\t#Threads\tSize\n");
        while(!results.empty()) {
            Result& result = results.front();
            printf("%s\t%s\t%lf\t%d\t%li\n", type, result.first, result.second.first * 1000 + result.second.second * 0.001, threadCount, size);
            results.pop_front();
        }
    }

};

int main(int argc, char** argv) {

    if(argc != 3) {
        printf("benchmark_map\t[threadCount]\t[size]\n");
        return -1;
    }

    threadCount = atoi(argv[1]);
    gm_rt_set_num_threads(threadCount);

    runSize = atoi(argv[2]);

    Value* values = new Value[runSize];
    for(size_t i = 0; i < runSize; i++) {
        values[i] = (Value)rand();
    }

    gm_map_small<Key, Value, 0> smallMap;
    Benchmark benchmarkSmall(((gm_map<Key, Value>*)(&smallMap)), values, runSize, "small");
    benchmarkSmall.start();
    benchmarkSmall.printResults();
    printf("finished small\n");
    gm_map_large<Key, Value, 0> largeMap(runSize);
    Benchmark benchmarkLarge(((gm_map<Key, Value>*)(&largeMap)), values, runSize, "large");
    benchmarkLarge.start();
    benchmarkLarge.printResults();
    printf("finished large\n");

    return 0;
}
