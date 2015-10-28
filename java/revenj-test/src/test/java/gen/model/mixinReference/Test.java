package gen.model.mixinReference;



public final class Test   implements java.lang.Cloneable, java.io.Serializable {
	
	
	
	public Test(
			final int x,
			final gen.model.mixinReference.Author author) {
			
		setX(x);
		setAuthor(author);
	}

	
	
	public Test() {
			
		this.x = 0;
		this.author = new gen.model.mixinReference.Author();
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + 23844169;
		result = prime * result + (this.x);
		result = prime * result + (this.author.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Test))
			return false;
		return deepEquals((Test) obj);
	}

	public boolean deepEquals(final Test other) {
		if (other == null)
			return false;
		
		if(!(this.x == other.x))
			return false;
		if(!(this.author.equals(other.author)))
			return false;
		return true;
	}

	private Test(Test other) {
		
		this.x = other.x;
		this.author = other.author;
	}

	@Override
	public Object clone() {
		return new Test(this);
	}

	@Override
	public String toString() {
		return "Test(" + x + ',' + author + ')';
	}
	private static final long serialVersionUID = -4276154699263718026L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private Test(
			@com.fasterxml.jackson.annotation.JsonProperty("_helper") final boolean _helper ,
			@com.fasterxml.jackson.annotation.JsonProperty("x") final int x,
			@com.fasterxml.jackson.annotation.JsonProperty("author") final gen.model.mixinReference.Author author) {
		
		this.x = x;
		this.author = author == null ? new gen.model.mixinReference.Author() : author;
	}

	
	private int x;

	
	@com.fasterxml.jackson.annotation.JsonProperty("x")
	public int getX()  {
		
		return x;
	}

	
	public Test setX(final int value) {
		
		this.x = value;
		
		return this;
	}

	
	private gen.model.mixinReference.Author author;

	
	@com.fasterxml.jackson.annotation.JsonProperty("author")
	public gen.model.mixinReference.Author getAuthor()  {
		
		return author;
	}

	
	public Test setAuthor(final gen.model.mixinReference.Author value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"author\" cannot be null!");
		this.author = value;
		
		return this;
	}

}
