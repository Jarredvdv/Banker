enum ActivityType {
    INITIATE, REQUEST, COMPUTE, RELEASE, TERMINATE
}
public class Activity {
    ActivityType activity_type;
    int unit_num;
    int num_resource;
    
    
    /*Constructor for activity objects as outlined in assignment
  	  This includes the type of activity, task number and num of units*/

    public Activity(ActivityType activity_type, int unit_num, int num_resource) {
        this.activity_type = activity_type;
        this.unit_num = unit_num;
        this.num_resource = num_resource;
        
    }
}
