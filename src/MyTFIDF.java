package MyFreq;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class MyTFIDF extends Configured implements Tool {
	public static void main(String args[]) throws Exception {
		if(args.length != 3) {
			System.out.println("Usage: MyTFIDF <MyFreq's input dir> <input dir> <output dir>");
			System.exit(-1);
		}
		
		int result = ToolRunner.run(new Configuration(), new MyTFIDF(), args);
		System.exit(result);
	}

	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		FileSystem fs = FileSystem.get(conf);
		
		FileStatus[] lfs = fs.listStatus(new Path(args[0]));
		conf.setInt("N", lfs.length);
		
		Job job = new Job(conf, "My TFIDF");
		
		job.setJarByClass(MyTFIDF.class);
		job.setMapperClass(MyTFIDFMapper.class);
		job.setReducerClass(MyTFIDFReducer.class);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		FileInputFormat.addInputPath(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, new Path(args[2]));
		
		boolean success = job.waitForCompletion(true);
		return success ? 0 : 1;
	}
	
	public static class MyTFIDFMapper extends Mapper<LongWritable, Text, Text, Text> {
		private static int N;
	
		@Override
		protected void setup(
				Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			Configuration conf = context.getConfiguration();
			N = conf.getInt("N", 0);
		}
		
		/*
		 * Input : offset, ((word, docid), (freq, count))
		 * Output : word/docid, tfidf
		 */
		@Override
		protected void map(LongWritable key, Text value,
				Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			String words[] = value.toString().split("\\s+");
			String word = words[0];
			String docid = words[1];
			int freq = Integer.parseInt(words[2]);
			int count = Integer.parseInt(words[3]);
			
			double idf = Math.log(N / count);
			double tfidf = freq * idf;
			
			context.write(new Text(word + "/" + docid), new Text(String.valueOf(tfidf)));
		}
	}
	
	public static class MyTFIDFReducer extends Reducer<Text, Text, Text, Text> {

		/*
		 * Input : word/docid, tfidf
		 * Output : word/docid, tfidf
		 */
		@Override
		protected void reduce(Text key, Iterable<Text> values,
				Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			for(Text value : values) {
				context.write(key, value);
			}
		}
	}
}
