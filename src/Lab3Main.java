import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Lab3Main {
	
	private static Scanner inputScan;

	public static void main(String [] args) {
    	//Variables used to format & keep track of input file
		String activity_type;
		int unit_num;
    	int task_count;
		int task_num;
    	int resource_num;
    	int resource_count;
    	
    	try {//Try catch to handle missing/error in input file
            inputScan = new Scanner(new File(args[0])); //Opens input file to be read
            task_count = inputScan.nextInt();           
            for(int i = 0; i < task_count; i++) {//Creates task objects based on task count
                TaskManager.add_task();
            }
            resource_count = inputScan.nextInt();
            for(int i = 0; i < resource_count; i++) {
                ResourceManager.update_resources(inputScan.nextInt()); //Creates resource objects based on number of resources
            }
            while(inputScan.hasNext()) {
                //Associates each variable with input file matrix
            	activity_type = inputScan.next();
                task_num = inputScan.nextInt();
                resource_num = inputScan.nextInt();
                unit_num = inputScan.nextInt();
                TaskManager cur_task = TaskManager.tasks.get(task_num-1); 
                cur_task.add_activity(activity_type, unit_num, resource_num);//Creates activities for each task
            }
            //Initializes & runs FIFO and banker resource managers
            System.out.println("\t\tFIFO");
            FIFO fifo = new FIFO();
            fifo.run();
            System.out.println("\t\tBANKER'S");
            Banker banker = new Banker();
            banker.run();
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
}