import java.util.ArrayList;

public class ResourceManager {
	static ArrayList<ResourceManager> resources = new ArrayList<>();;//List to keep track of resource objects	
	int units_leftover;
    int num_resource;
    int unit_count;
    int units_temp;

    //Initializes new resource objects that hold information about the number of units that need to be processed
    private ResourceManager(int units) {
    	units_temp = 0; //Stores units that still need to processed
    	units_leftover = units;
        unit_count = units;
    }
    
    //Adds resource units to a given task
    public boolean add_resource(TaskManager cur_task, int num_units) {
        if(units_leftover < num_units) {//If there aren't enough units to complete the request return false
            cur_task.resource_reqs.put(this, num_units);
            return false;
        }else{
	        units_leftover -= num_units; //Otherwise we decrease the number of units leftover by the amount needed to satisfy the request
	        int temp = 0;
	        if(cur_task.resource_has.containsKey(this)){
	            temp = cur_task.resource_has.get(this);
	        }
	        temp += num_units;
	        cur_task.resource_has.put(this, temp);//Update list of resource objects with the updated task/units
	        return true;
        }
    }

    //Checks and removes resources from a task
    public boolean release_resource(TaskManager cur_task, int num_units) {
        int units_owned = cur_task.resource_has.get(this);
        if(num_units > units_owned && num_units != -1) {//Checks if there are more units than currently owned by a task
            return false;
        } else if (num_units == -1) { //Check/validates if there are any cases where units that need to be released is -1
            units_temp += units_owned;
            units_owned = 0;
        } else { //Otherwise we release any resources owned by the task
            units_owned -= num_units;
            units_temp += num_units;
        }
        cur_task.resource_has.put(this, units_owned);//Update resource list
        return true;
    }   

    //Updates resource list with new resources from input file
    static void update_resources(int units) {
        ResourceManager new_resource = new ResourceManager(units);
        resources.add(new_resource);
        new_resource.num_resource = resources.indexOf(new_resource);//Updates resource number for new resource 
    }
    
    //Copies resources to protect the original resource list from any modification
    static ArrayList<ResourceManager> copy_resources() {
        ArrayList<ResourceManager> temp = new ArrayList<>();
        for(ResourceManager cur_resource : resources) {
            ResourceManager new_resource = new ResourceManager(cur_resource.units_leftover);
            temp.add(new_resource);
        }
        return temp;
    }
}
