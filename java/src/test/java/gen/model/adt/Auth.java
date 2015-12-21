/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.adt;



@com.fasterxml.jackson.annotation.JsonSubTypes({@com.fasterxml.jackson.annotation.JsonSubTypes.Type(gen.model.adt.DigestSecurity.class),@com.fasterxml.jackson.annotation.JsonSubTypes.Type(gen.model.adt.Anonymous.class),@com.fasterxml.jackson.annotation.JsonSubTypes.Type(gen.model.adt.Token.class),@com.fasterxml.jackson.annotation.JsonSubTypes.Type(gen.model.adt.BasicSecurity.class)})
public interface Auth<T extends gen.model.adt.Auth<T>>  {
	
}
