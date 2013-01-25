FILES=./bin/*

for f in $FILES
do
	echo "Running "$f
	$f
	if [ "$?" = "0" ]; then
		echo "Finished"
	else
		echo $f" Failed!!!"
		exit 1
	fi
done
echo "All Finished"