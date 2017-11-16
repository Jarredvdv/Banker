import java.util.ArrayList;
import java.util.HashMap;

enum State {//Enum provides a versatile special data type for already known task types
    RUNNING, ABORTED, TERMINATED
}

public class TaskManager {
	//Lists used to keep track of resource needs & tasks/activities
	static ArrayList<TaskManager> tasks = new ArrayList<>();
    ArrayList<Activity> activities = new ArrayList<>();
    HashMap<ResourceManager, Integer> resource_has = new HashMap<>();
    HashMap<ResourceManager, Integer> resource_reqs = new HashMap<>();
    HashMap<ResourceManager, Integer> resource_claims = new HashMap<>();
    
    ArrayList<Activity> activity_copy = new ArrayList<>();
    
    State cur_state;
    
    int cycle_count;
    boolean is_blocked;
    int block_count;
    int compute_time;
    
    //Constructor to initialize task objects
    private TaskManager() {
    	
    	//Variables to keep track of task properties
    	cycle_count = 0;
    	block_count = 0;
        compute_time = 0;
        
        cur_state = State.RUNNING;
        is_blocked = false;
    }

    //Adds creates & adds task objects to task list based on file input
    static void add_task() {
        tasks.add(new TaskManager());
    }
    
    //Adds activities object to tasks based on parameters provided
    void add_activity(String activity_type, int num_units, int resource_num) {
        Activity cur_activity = null;
        switch(activity_type){//Creates activity objects based on type specified
        	case "initiate":
        		cur_activity = new Activity(ActivityType.INITIATE, num_units, resource_num);
        		break;
        	case "compute":
        		cur_activity = new Activity(ActivityType.COMPUTE, num_units, resource_num);
        		break;
        	case "release":
        		cur_activity = new Activity(ActivityType.RELEASE, num_units, resource_num);
        		break;    
        	case "request":
        		cur_activity = new Activity(ActivityType.REQUEST, num_units, resource_num);
        		break;     	
        	case "terminate":
        		cur_activity = new Activity(ActivityType.TERMINATE, num_units, resource_num);
        		break;
        	   
        }
        activities.add(cur_activity);   
        activity_copy.add(cur_activity);//copy of list keeps copy of original list of activities to protect against modification and later used for copying                     
    }
   
    //Checks aborted processes and prints/formats output accordingly
    public void print_output() {   	
    	if(cur_state == State.ABORTED) {//Checks for any aborted tasks and displays aborted if needed
            System.out.println("aborted");
        } else {
            cycle_count--;//Accounts for unnecessary extra cycle added on during task processing
            double pct = (double)block_count/cycle_count * 100;
            int formatted_pct = (int) Math.rint(pct);
            System.out.println(cycle_count + "\t" + block_count + "\t" + formatted_pct + "%");
        }
    }
    
    //Copies resources to protect the original resource list from any modification
    TaskManager copy_activities() {
        TaskManager temp = new TaskManager();
        for(Activity act : activity_copy) {//Since we kept a copy of activities we can easily duplicate the original list
            temp.activities.add(act);
            temp.activity_copy.add(act);
        }
        return temp;
    }

}
