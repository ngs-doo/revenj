package gen.model.security;



@com.fasterxml.jackson.annotation.JsonSubTypes({@com.fasterxml.jackson.annotation.JsonSubTypes.Type(gen.model.security.Document.class)})
public interface Dummy<T extends gen.model.security.Dummy<T>>  {
	
}
