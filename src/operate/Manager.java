package operate;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class Manager<T> {
    
    private Cache inputs;
    private T output;
    private final BiFunction<Integer, Trail, T> f;
    private final BiPredicate<? super Cache, ? super Cache> match;
    
    public Manager(
            BiFunction<Integer, Trail, T> f, 
            BiPredicate<? super Cache, ? super Cache> match){
        
        this.f = f;
        this.match = match;
    }
    
    public T get(Integer limit, Trail trail){
        Cache c = new Cache(limit, trail);
        if(!match.test(c, inputs)){
            output = f.apply(limit, trail);
            inputs = c;
        }
        return output;
    }
}
