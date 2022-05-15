public class IModel {
    
    //static int id;
    //static void cursor;
    static StringBuffer text = new StringBuffer("");
    
    public static void updateTEXT(String newTEXT){
        int length = text.length();
        text.replace(0, length, newTEXT);       
    }

    public static void updateCHAT(String newCHAT){

    }

    public static void updateCURSOR(String newCURSOR){

    }

    public static String outputModel(){
        return new String(text);
    }
}
