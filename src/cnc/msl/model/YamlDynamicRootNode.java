package cnc.msl.model;

public class YamlDynamicRootNode<ClassTyp>{
	private ClassTyp key;
	private ClassTyp value;
	private ClassTyp comment;

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
   
   public void addComment(ClassTyp comment) {
      this.comment = comment;
   }

   public ClassTyp getComment() {
      return comment;
   }	
   
   public String toString() { 
	  if(comment != "" || comment == null ) {
		  return key + ":" + value + " #" + comment + System.lineSeparator();
	  }else {   
		  return key + ":" + value + System.lineSeparator();
	  }
   } 
   
}
