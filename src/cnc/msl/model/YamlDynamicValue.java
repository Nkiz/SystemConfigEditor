package cnc.msl.model;

public class YamlDynamicValue<ClassTyp>{
	private ClassTyp key;
	private ClassTyp value;

	public void addKey(ClassTyp key) {
      this.key = key;
   }

   public ClassTyp getKey() {
      return key;
   }	
	
   public void addValue(ClassTyp value) {
      this.value = value;
   }

   public ClassTyp getValue() {
      return value;
   }
   
   public String toString() { 
	    return "  - " +  key + ":" + value + System.lineSeparator();
	} 
   
}
