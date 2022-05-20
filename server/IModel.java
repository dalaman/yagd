import java.util.HashMap;
import java.util.Map;

public class IModel {
    StringBuffer TEXT;
    Map<Integer, String> CURSOR; 
    
    public IModel(){
        this.TEXT = new StringBuffer(""); //TEXT
        this.CURSOR = new HashMap<>();
    }

    public void updateTEXT(String newTEXT){
        int length = TEXT.length();
        TEXT.replace(0, length, newTEXT);       
    }

    public void updateCURSOR(String newCURSOR, int id){
        CURSOR.put(id, newCURSOR);
    }

    // Put Model in array and Output
    public String[] outputModel(){
        String model[] = new String[CURSOR.size()+1];
        int i = 0;
        model[0] = new String(TEXT);
        for(String val : CURSOR.values()){
            if(i<CURSOR.size()){
                model[i+1] = val;
            }
            i++;
        }
        return model;
    }
}
