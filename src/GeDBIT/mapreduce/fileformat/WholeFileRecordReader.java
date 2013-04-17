package GeDBIT.mapreduce.fileformat;

import java.io.IOException;

import javax.imageio.IIOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class WholeFileRecordReader extends RecordReader<Text, BytesWritable> {
    String FileName;
    private FileSplit fileSplit;
    private FSDataInputStream fis;
    private Text key = null;
    private BytesWritable value = null;
    private boolean processed = false;

    @Override
    public Text getCurrentKey() throws IOException, InterruptedException {
	return this.key;
    }

    @Override
    public BytesWritable getCurrentValue() throws IOException,
	    InterruptedException {
	return this.value;
    }

    @Override
    public void initialize(InputSplit inputSplit, TaskAttemptContext tacontext)
	    throws IOException, InterruptedException {
	fileSplit = (FileSplit) inputSplit;
	Configuration job = tacontext.getConfiguration();
	Path file = fileSplit.getPath();
	FileSystem fs = file.getFileSystem(job);
	fis = fs.open(file);
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
	if (key == null) {
	    key = new Text();
	}

	if (value == null) {
	    value = new BytesWritable();
	}

	if (!processed) {
	    byte[] content = new byte[(int) fileSplit.getLength()];
	    Path file = fileSplit.getPath();
	    System.out.println(file.getName());
	    key.set(file.getName());

	    try {
		IOUtils.readFully(fis, content, 0, content.length);
		value.set(new BytesWritable(content));
	    } catch (IIOException e) {
		e.printStackTrace();
	    } finally {
		IOUtils.closeStream(fis);
	    }

	    processed = true;
	    return true;
	}
	return false;
    }

    public float getProgress() throws IOException, InterruptedException {
	return processed ? fileSplit.getLength() : 0;
    }

    @Override
    public void close() throws IOException {
	// TODO Auto-generated method stub

    }
}
