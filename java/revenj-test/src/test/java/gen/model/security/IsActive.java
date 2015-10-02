package gen.model.security;



@com.fasterxml.jackson.annotation.JsonSubTypes({@com.fasterxml.jackson.annotation.JsonSubTypes.Type(gen.model.security.Document.class)})
public interface IsActive<T extends gen.model.security.IsActive<T>>  {
	
	
	boolean getDeactivated();
	T setDeactivated(final boolean value);
}
