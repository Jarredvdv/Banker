import java.util.ArrayList;

public class FIFO {
    
	//Data structures for storing tasks & resource objects
	ArrayList<TaskManager> tasks = new ArrayList<>();
    ArrayList<TaskManager> task_queue = new ArrayList<>();
    ArrayList<TaskManager> temp_list = new ArrayList<>();
	ArrayList<ResourceManager> resources = new ArrayList<>();
    
	
    //Checks whether there are still running tasks
    public boolean has_tasks() {
        for(TaskManager task : tasks) {
            if(task.cur_state == State.RUNNING) {
                return true;
            }
        }
        return false;
    }
    
    //Loops through each resource and returns any units that are released
    public void process_resources() {
        for(ResourceManager cur_resource : resources) {
        	cur_resource.units_leftover += cur_resource.units_temp;
        	cur_resource.units_temp = 0;
        }
    
    }
    
    //Adds tasks to task queue that are not blocked or terminated
    public void validate_tasks(){  	
        for(TaskManager task : tasks) {
            if(!task_queue.contains(task)){
	        	if(task.cur_state != State.ABORTED && task.cur_state != State.TERMINATED){
	                task_queue.add(task);
            	}
            }
        }
    }
    
    //Ends a task and returns resources to resource manager
    public void end_task(TaskManager cur_task) {
        cur_task.cur_state = State.ABORTED;
        for (ResourceManager cur_resource : cur_task.resource_has.keySet()) {
        	cur_resource.release_resource(cur_task, -1);//returns resources back to resource manager
        }
        process_resources();//Once those resources are returned we process them once more

    }
    
    //Handles each activity and determines appropriate action that handles resources based on activity type
    public void process_activity(TaskManager task, Activity cur_activity){
    	switch(cur_activity.activity_type){    		    	    	
	    	case REQUEST:	    		
	    		ResourceManager cur_resource = resources.get(cur_activity.num_resource - 1);           
	            if (!cur_resource.add_resource(task, cur_activity.unit_num)){//Checks if current number of resources can satisfy claim. If not, block.
	            	task.activities.add(task.cycle_count + 1, cur_activity); //If blocked, we add activity back in for the next loop
	            	task.is_blocked = true;
	            	task.block_count++;	                
	            }else{
	                task.is_blocked = false;
	            }
	            break;	
    		case RELEASE:
	    		cur_resource = resources.get(cur_activity.num_resource - 1);
	            cur_resource.release_resource(task, cur_activity.unit_num);//Releases resources from current task
	            break;    		
	    	case INITIATE:
	    		break;	    	
	    	case COMPUTE:
	            if(task.compute_time == 0){
	                task.compute_time = cur_activity.num_resource;
	                task.compute_time--;
	            } else {
	            	task.activities.add(task.cycle_count + 1, cur_activity);
	            }
	            break;  	
	    	case TERMINATE:
	    		task.cur_state = State.TERMINATED;	            
	    		break;
    	}
    	task.cycle_count++;
    }
    
    public void run() {
    	copy_resources();//Ensures that resources can be modified without affecting other resources or tasks   	
    	
    	while(has_tasks()){        	
    		validate_tasks();//Adds tasks to task queue that are not blocked or terminated            		    		
            
    		for(TaskManager task : task_queue){//Loops through all validated tasks
                if(task.cur_state == State.RUNNING){
                	Activity cur_activity = task.activities.get(task.cycle_count); //The type of activity determines how each task is processed
                    process_activity(task, cur_activity);           		
                    }
            }            
            process_resources();//Processes any released resources after task is executed
            int num_task = 0;        
            while(is_deadlocked()) { //Determines if a deadlock exists and handles tasks if so
                TaskManager cur_task = tasks.get(num_task);
                while(cur_task.cur_state != State.RUNNING){
                    num_task++;
                    cur_task = tasks.get(num_task);
                }
                cur_task.cur_state = State.ABORTED;
                for(ResourceManager cur_resource : cur_task.resource_has.keySet()){//Loops through and removes resources from a task     
                	cur_resource.release_resource(cur_task, -1);
                }                             
                process_resources();//Similar to before, if resources are released, we add them back to the task manager
                end_task(cur_task);
                check_blocks();             
                num_task++;
            }          
            for(TaskManager cur_task : task_queue) {//Removes any unblocked tasks from task queue before processing blocked tasks
                if(!cur_task.is_blocked || cur_task.cur_state != State.RUNNING)
                    temp_list.add(cur_task);
            }

            task_queue.removeAll(temp_list);           
        }
    	
        format_output();
    }
    
    //Checks for any blocked tasks and checks if the number of units the resource requires is greater than the number of units left
    public void check_blocks(){	
        for(TaskManager task : tasks) {
            if (task.is_blocked) {
                task.is_blocked = false;
                for (ResourceManager cur_resource : task.resource_reqs.keySet()) {
                    if(task.resource_reqs.get(cur_resource) > cur_resource.units_leftover){
                        task.is_blocked = true;
                    }
                }
            }
        }
    }
    
    //Prints and formats output for all tasks & computes total sum
    public void format_output() {     
    	int cycleSum = 0;
        int waitSum = 0;

        for(int i = 0; i < tasks.size(); i++) {//Prints details for individual tasks
            System.out.print("Task " + (i+1) + "\t\t");
            TaskManager cur_task = tasks.get(i);
            cur_task.print_output();

            if(cur_task.cur_state != State.ABORTED) {//If task is aborted we increment the total number of cycles and wait time
                cycleSum += tasks.get(i).cycle_count;
                waitSum += cur_task.block_count;
            }
        }
        
        double pct = (double)waitSum/cycleSum * 100;
        int rounded_pct = (int) Math.rint(pct);
        System.out.printf("Total\t\t%-7s %-7s %s%%\n\n", cycleSum, waitSum, rounded_pct);//Formating for final/total output
    }
    
    //Check for any deadlocks that exist
    public boolean is_deadlocked(){
        if(!has_tasks()) {//There can't be deadlocks if there are no tasks yet
            return false;
        } 
        for(TaskManager task : tasks){
            if(!task.is_blocked && task.cur_state == State.RUNNING){//If there is a task that's not blocked and running then there is no deadlock
            return false;
            }
        }
        return true;
    }
    
    //Creates a clone of resources and tasks so that they can be modified
    public void copy_resources(){
    	for(TaskManager task : TaskManager.tasks) {
            tasks.add(task.copy_activities());
        }
        resources = ResourceManager.copy_resources();
    }
    
}
