import java.util.ArrayList;
import java.util.HashMap;


public class Banker extends FIFO {
    HashMap<ResourceManager, Integer> temp_list = new HashMap<>();
	//Initializes Banker objects that contain tasks and task_queue data structures
    public Banker() {
        tasks = new ArrayList<>();
        task_queue = new ArrayList<>();        
    }

    public void run(){
    	copy_resources();
    	while(has_tasks()){        	
    		validate_tasks();//Adds tasks to task queue that are not blocked or terminated            		    		           
    		for(TaskManager task : task_queue){//Loops through all validated tasks
                if(task.cur_state == State.RUNNING){
                	Activity cur_activity = task.activities.get(task.cycle_count); //The type of activity determines how each task is processed
                    process_activity(task, cur_activity);           		
                    }
            }            
            process_resources();//Processes any released resources after task is executed           
            ArrayList<TaskManager> temp = new ArrayList<>();
            for(TaskManager cur_task : task_queue) {//Removes any unblocked tasks from task queue before processing blocked tasks
                if(!cur_task.is_blocked || cur_task.cur_state != State.RUNNING)
                    temp.add(cur_task);
            }
            task_queue.removeAll(temp);           
        }	
        format_output();
    }
    
    //Handles each activity and determines appropriate action based on activity type & algorithm
    public void process_activity(TaskManager task, Activity cur_activity){
    	switch(cur_activity.activity_type){    		    	    	
    		case RELEASE://Pretty straightforward
    			ResourceManager cur_resource = resources.get(cur_activity.num_resource - 1);
                cur_resource.release_resource(task, cur_activity.unit_num);//We release resources here
                task.cycle_count++;
                break;
	            
	    	case REQUEST:	    		
	    		cur_resource = resources.get(cur_activity.num_resource - 1);            
                int claim = task.resource_claims.get(cur_resource);//Before we process the request, we must update the task's claim
                if (task.resource_has.containsKey(cur_resource)) {
                    claim -= task.resource_has.get(cur_resource);//Updates claim with resources currently owned
                }          
                if (!is_safe(task, cur_resource, cur_activity.unit_num)) {//Checks for safe state, if not task is blocked
                    task.is_blocked = true;
                    task.block_count++;
                    task.activities.add(task.cycle_count + 1, cur_activity);
                } else if (cur_activity.unit_num > claim) {//If in safe state but task requests more than claim then we end that task
                    end_task(task);
                    System.out.println("During cycle " + task.cycle_count + "-" + (task.cycle_count + 1) + " of Banker's algorithm");
                    int taskNum = tasks.indexOf(task) + 1;
                    System.out.println("\t Task " + taskNum + "'s request exceeds claim; aborted\n");
                } else {
                	cur_resource.add_resource(task, cur_activity.unit_num);//If passes all checks we add resources to the task
                    task.is_blocked = false;
                }
                task.cycle_count++;
                break;	    	
	    	case INITIATE:
	    		cur_resource = resources.get(cur_activity.num_resource - 1);
                if (cur_activity.unit_num > cur_resource.unit_count) {//If the resources claim is more than units available we end that task
                    end_task(task); 
                    int taskNum = tasks.indexOf(task) + 1;
                    System.out.println("Banker aborts task " + taskNum + " before run begins:");
                    System.out.println("\t Claim for resource " + (cur_resource.num_resource + 1) + " ("+ cur_activity.unit_num + ") exceeds number of units present (" + cur_resource.unit_count + ")\n");
                } else {
                    task.resource_claims.put(cur_resource, cur_activity.unit_num);//Otherwise we add the claim to the task
                    task.cycle_count++;
                }
                break;                
	    	case COMPUTE://Updates compute time based on current compute time
	    		if (task.compute_time != 0) {
                    task.activities.add(task.cycle_count + 1, cur_activity);
                } else if (task.compute_time == 0) {//Check for initialized compute time
                    task.compute_time = cur_activity.num_resource;
                }
                task.compute_time--;
                task.cycle_count++;
                break;          	                	
	    	case TERMINATE:
	    		task.cur_state = State.TERMINATED;
	    		task.cycle_count++;
	    		break;
    	}
    	
    }
  
    //Checks if a request would result in a safe state. Returns true if safe, false if not
    private boolean is_safe(TaskManager cur_task, ResourceManager cur_resource, int units) {  
    	boolean [] did_finish = new boolean[tasks.size()];
    	temp_list = new HashMap<>();
    	for(ResourceManager resource : resources) {//Creates clone of resources to allow simulation of request
            if(cur_resource == resource) {            
                temp_list.put(resource, resource.units_leftover - units);//
            } else {
                temp_list.put(resource, resource.units_leftover);
            }
        }

        for(int i = 0; i < tasks.size(); i++) {//Loops through all tasks
        	boolean isSafe = true;
            TaskManager temp_task = tasks.get(i);
            if(temp_task.cur_state == State.ABORTED || temp_task.cur_state == State.TERMINATED) {//If a task is aborted/terminated, we mark it as complete 
                did_finish[i] = true;
            } else {
            	for(ResourceManager resource : resources) {
                    int resources_req = temp_task.resource_claims.get(resource);
                    if(temp_task.resource_has.containsKey(resource)) {
                           resources_req -= temp_task.resource_has.get(resource);//Subtracts any resources already owned
                       }
                        if(temp_task == cur_task && resource == cur_resource) {//Checks if current task and resource is valid              
                            resources_req -= units;//Simulates the request
                        }
                        if (resources_req > temp_list.get(resource)) {//Otherwise if task needs too many resources it is not safe
                            isSafe = false;                           
                            break;
                        }
                    
                }
                if(!did_finish[i] && isSafe) {//Otherwise, if a request can be satisfied we must manage released resources
                    for(ResourceManager resource : resources) {
                    	int count = 0;
                    	int temp = temp_list.get(resource);                       
                        if(temp_task.resource_has.containsKey(resource)) {
                            count += temp_task.resource_has.get(resource);//Increments number of units that will be returned
                        }
                        if(temp_task == cur_task && resource == cur_resource) {//Housekeeping for additional units
                            count += units;
                        }
                        temp += count;
                        temp_list.put(resource, temp);//Adds resource, unit pair back into temp list 
                    }
                    did_finish[i] = true;//Marks as request as completed
                    i = -1;//Restarts cycle to process new resources
                }
            }
        }
        for(boolean complete : did_finish) {//Final check that all tasks did complete 
            if(!complete) {
                return false;
            }
        }
        return true;
    }
    
}
    