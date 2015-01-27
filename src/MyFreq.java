package MyTFIDF;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class MyFreq extends Configured implements Tool {
	public static void main(String[] args) throws Exception {
		if(args.length != 2) {
			System.out.println("Usage: MyFreq <input dir> <output dir>");
			System.exit(-1);
		}
	
		int result = ToolRunner.run(new Configuration(),
				new MyFreq(), args);
		System.exit(result);
	}

	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		Job job = new Job(conf, "My Freq");
		
		job.setJarByClass(MyFreq.class);
		job.setMapperClass(MyMapper.class);
		job.setReducerClass(MyReducer.class);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		boolean success = job.waitForCompletion(true);
		return success ? 0 : 1;
	}
	
	public static class MyMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
		private static Pattern p = Pattern.compile("\\w+");
		/*
		 * Input : offset, line
		 * Output : word + " " + docid, 1
		 */
		@Override
		protected void map(LongWritable key, Text value,
				Mapper<LongWritable, Text, Text, IntWritable>.Context context)
				throws IOException, InterruptedException {
			FileSplit fs = (FileSplit)context.getInputSplit();
			String docid = fs.getPath().getName();
			
			Matcher m = p.matcher(value.toString());
			while(m.find()) {
				String word = m.group().toLowerCase();
				context.write(new Text(word + " " + docid), new IntWritable(1));
			}
		}
	}
	
	public static class MyReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		/*
		 * Input : word + " " + docid, 1[]
		 * Output : word + " " + docid, freq
		 */
		@Override
		protected void reduce(Text key, Iterable<IntWritable> values,
				Reducer<Text, IntWritable, Text, IntWritable>.Context context)
				throws IOException, InterruptedException {
			int freq = 0;
			
			for(IntWritable value : values) {
				freq += value.get();
			}
			
			context.write(key, new IntWritable(freq));
		}
	}
}
