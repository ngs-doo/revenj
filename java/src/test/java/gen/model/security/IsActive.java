/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.security;



@com.fasterxml.jackson.annotation.JsonSubTypes({@com.fasterxml.jackson.annotation.JsonSubTypes.Type(gen.model.security.Document.class)})
public interface IsActive<T extends gen.model.security.IsActive<T>>  {
	
	
	boolean getDeactivated();
	T setDeactivated(final boolean value);
}
