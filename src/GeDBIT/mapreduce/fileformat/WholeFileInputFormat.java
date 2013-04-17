package GeDBIT.mapreduce.fileformat;

import java.io.IOException;
import GeDBIT.mapreduce.fileformat.WholeFileRecordReader;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

@SuppressWarnings("rawtypes")
public class WholeFileInputFormat extends FileInputFormat {
    protected boolean isSplitable(FileSystem fs, Path filename) {
	return false;
    }

    @Override
    public RecordReader createRecordReader(InputSplit split,
	    TaskAttemptContext context) {
	WholeFileRecordReader wfrr = new WholeFileRecordReader();
	try {
	    wfrr.initialize(split, context);
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	return wfrr;
    }

}