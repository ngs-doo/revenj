/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.security;



@com.fasterxml.jackson.annotation.JsonSubTypes({@com.fasterxml.jackson.annotation.JsonSubTypes.Type(gen.model.security.Document.class)})
public interface Dummy<T extends gen.model.security.Dummy<T>>  {
	
}
