package gen.model.binaries;



public final class ReadOnlyDocument   implements java.io.Serializable, org.revenj.patterns.DataSource {
	
	
	@com.fasterxml.jackson.annotation.JsonCreator 
	public ReadOnlyDocument(
			@com.fasterxml.jackson.annotation.JsonProperty("ID")  final java.util.UUID ID,
			@com.fasterxml.jackson.annotation.JsonProperty("Name")  final String Name) {
			
		this.ID = ID == null ? java.util.UUID.randomUUID() : ID;
		this.Name = Name == null ? "" : Name;
	}

	private static final long serialVersionUID = -4715846197549042640L;
	
	private final java.util.UUID ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public java.util.UUID getID()  {
		
		return this.ID;
	}

	
	private final String Name;

	
	@com.fasterxml.jackson.annotation.JsonProperty("Name")
	public String getName()  {
		
		return this.Name;
	}

}
