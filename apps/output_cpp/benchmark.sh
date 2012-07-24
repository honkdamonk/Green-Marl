for i in {0..5}
do
	for thread in {1..24}
	do
		for size in {0..10}
		do
			./bin/benchmark_map ${thread} ${size}
		done
	done
done
