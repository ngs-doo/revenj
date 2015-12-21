/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.mixinReference;



@com.fasterxml.jackson.annotation.JsonSubTypes({@com.fasterxml.jackson.annotation.JsonSubTypes.Type(gen.model.mixinReference.SpecificReport.class)})
public interface Report<T extends gen.model.mixinReference.Report<T>>  {
	
	
	gen.model.mixinReference.Author getAuthor() throws java.io.IOException;
	T setAuthor(final gen.model.mixinReference.Author value);
}
