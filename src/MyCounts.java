package MyFreq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
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

public class MyCounts extends Configured implements Tool {
	public static void main(String[] args) throws Exception {
		if(args.length != 2) {
			System.out.println("Usage: MyCounts <input dir> <output dir>");
			System.exit(-1);
		}
		
		int result = ToolRunner.run(new Configuration(), new MyCounts(), args);
		System.exit(result);
	}
	
	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		Job job = new Job(conf, "My Counts");
		
		job.setJarByClass(MyCounts.class);
		job.setMapperClass(MyCountsMapper.class);
		job.setReducerClass(MyCountsReducer.class);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		boolean success = job.waitForCompletion(true);
		return success ? 0 : 1;
	}
	
	public static class MyCountsMapper extends Mapper<LongWritable, Text, Text, Text> {

		/*
		 * Input : offset, (word + " " + docid, freq)
		 * Output : word, (docid + " " + freq, 1)
		 */
		@Override
		protected void map(LongWritable key, Text value,
				Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			String input[] = value.toString().split("\\s+");
			String word = input[0];
			String docid = input[1];
			int freq = Integer.parseInt(input[2]);
			
			context.write(new Text(word), new Text(docid + " " + freq));
		}
	}
	
	public static class MyCountsReducer extends Reducer<Text, Text, Text, Text> {

		/*
		 * Input : word, (docid + " " + freq, 1)[]
		 * Output : (word, docid), (freq, count)
		 */
		@Override
		protected void reduce(Text key, Iterable<Text> values,
				Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			Map<Text, String> map = new HashMap<Text, String>();
			int count = 0;
			
			for(Text value : values) {
				String[] docfreq = value.toString().split("\\s+");
				String docid = docfreq[0];
				String freq = docfreq[1];
				
				/* 
				 * ex) key   : john bible
				 *     value : 15 
				 */
				map.put(new Text(key.toString() + " " + docid), freq);
				count++;
			}
			
			for(Text worddoc : map.keySet()) {
				String freq = map.get(worddoc);
				/* 
				 * ex) key   : john bible
				 *     value : 15, 7
				 * ex) key   : john book
				 *     value : 15, 8
				 *     
				 *     value : Total freq, each file's freq
				 */
				context.write(worddoc, new Text(freq + " " + count));
			}
		}
	}
}