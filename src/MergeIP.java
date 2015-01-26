package MergeIP;

import java.io.IOException;
import java.util.Iterator;

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

public class MergeIP extends Configured implements Tool {
	public static void main(String[] args) throws Exception {
		if(args.length != 2) {
			System.out.println("Usage: MergeIP <input dir> <output dir>");
			System.exit(-1);
		}
		
		int result = ToolRunner.run(new Configuration(), new MergeIP(), args);
		System.exit(result);
	}

	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		Job job = new Job(conf, "Merge IP");
		
		job.setJarByClass(MergeIP.class);
		job.setMapperClass(MergeMapper.class);
		job.setReducerClass(MergeReducer.class);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		boolean success = job.waitForCompletion(true);
		return success ? 0 : 1;
	}
	
	public static class MergeMapper extends Mapper<LongWritable, Text, Text, Text> {
		/*
         * () == key
         * Input : (offset), line
         * Output: (IP@Time), URL, Parameter, Status
         */
        @Override
        protected void map(LongWritable key, Text value,
                Mapper<LongWritable, Text, Text, Text>.Context context)
                throws IOException, InterruptedException {
            String words[] = value.toString().split("\\s+");
            String ip = words[9];
            String time = words[1];
            String url = words[5];
            String param = words[6];
            String status = words[11];
			Text output = new Text(url + " " + param + " " + status);

            context.write(new Text(ip + "@" + time), output);
		}
	}
	
	public static class MergeReducer extends Reducer<Text, Text, Text, Text> {
		/*
         * () == key
		 * Input : (IP@Time), URL, Parameter, Status
		 * Output : (IP@Time), URL, Parameter, Status
		 */
		@Override
		protected void reduce(Text key, Iterable<Text> values,
				Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			Iterator<Text> it = values.iterator();
			while(it.hasNext()) {
				Text value = it.next();
				context.write(key, value);
			}
		}
	}
}