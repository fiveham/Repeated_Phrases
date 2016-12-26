package operate;

public class Cache {
    private final Integer limit;
    private final Trail trail;
    
    public Cache(Integer limit, Trail trail){
        this.limit = limit;
        this.trail = trail;
    }
    
    public Integer getLimit(){
        return limit;
    }
    
    public Trail getTrail(){
        return trail;
    }
    
    @Override
    public boolean equals(Object o){
        if(o == this){
            return true;
        }
        if(o instanceof Cache){
            Cache c = (Cache) o;
            return equal(limit, c.limit) && equal(trail, c.trail); 
        }
        return false;
    }
    
    private static boolean equal(Object a, Object b){
        return a == null 
                ? b == null 
                : a.equals(b);
    }
}
