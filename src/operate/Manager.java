package operate;

import java.util.function.BiFunction;

public class Manager<T> {
    
    private Cache inputs;
    private T output;
    private final BiFunction<Integer, Trail, T> f;
    
    public Manager(BiFunction<Integer, Trail, T> f){
        this.f = f;
    }
    
    public T get(Integer limit, Trail trail){
        Cache c = new Cache(limit, trail);
        if(!c.equals(inputs)){
            output = f.apply(limit, trail);
        }
        return output;
    }
}
