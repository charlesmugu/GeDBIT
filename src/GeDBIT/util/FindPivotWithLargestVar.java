package GeDBIT.util;

import java.util.List;

import GeDBIT.dist.Metric;
import GeDBIT.type.IndexObject;

public class FindPivotWithLargestVar {

    public static int findPivotByVar(Metric metric,
	    List<? extends IndexObject> data, int first, int datasize) {
	int imax = 0;
	double var = 0;
	for (int i = 0; i < datasize; i++) {
	    double sqrx = 0, x = 0;
	    for (int j = 0; j < data.size(); j++) {
		double dist = metric.getDistance(data.get(first + i),
			data.get(first + j));
		sqrx += dist * dist;
		x += dist;
	    }
	    double currentVar = sqrx / (double) data.size()
		    - (x / (double) data.size()) * (x / (double) data.size());
	    if (currentVar > var) {
		var = currentVar;
		imax = i;
	    }
	}
	return imax;
    }

    public static int findPivotByVarold(Metric metric,
	    List<? extends IndexObject> data) {
	int imax = 0;
	double var = 0;
	for (int i = 0; i < data.size(); i++) {
	    double sqrx = 0, x = 0;
	    for (int j = 0; j < data.size(); j++) {
		double dist = metric.getDistance(data.get(i), data.get(j));
		sqrx += dist * dist;
		x += dist;
	    }
	    double currentVar = sqrx / (double) data.size()
		    - (x / (double) data.size()) * (x / (double) data.size());
	    if (currentVar > var) {
		var = currentVar;
		imax = i;
	    }
	}
	return imax;
    }
}
