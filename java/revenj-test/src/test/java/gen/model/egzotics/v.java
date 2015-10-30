package gen.model.egzotics;



public final class v   implements java.lang.Cloneable, java.io.Serializable {
	
	
	
	public v(
			final int x) {
			
		setX(x);
	}

	
	
	public v() {
			
		this.x = 0;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + 831;
		result = prime * result + (this.x);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof v))
			return false;
		return deepEquals((v) obj);
	}

	public boolean deepEquals(final v other) {
		if (other == null)
			return false;
		
		if(!(this.x == other.x))
			return false;
		return true;
	}

	private v(v other) {
		
		this.x = other.x;
	}

	@Override
	public Object clone() {
		return new v(this);
	}

	@Override
	public String toString() {
		return "v(" + x + ')';
	}
	
	@com.fasterxml.jackson.annotation.JsonCreator private v(
			@com.fasterxml.jackson.annotation.JsonProperty("_helper") final boolean _helper ,
			@com.fasterxml.jackson.annotation.JsonProperty("x") final int x) {
		
		this.x = x;
	}

	private static final long serialVersionUID = -8100861505247078999L;
	
	private int x;

	
	@com.fasterxml.jackson.annotation.JsonProperty("x")
	public int getX()  {
		
		return x;
	}

	
	public v setX(final int value) {
		
		this.x = value;
		
		return this;
	}

	
	public v(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<v>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<v> rdr : readers) {
			rdr.read(this, reader, context);
		}
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<v>[] readers, int __index___x) {
		
		readers[__index___x] = (item, reader, context) -> { item.x = org.revenj.postgres.converters.IntConverter.parse(reader); };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<v>[] readers, int __index__extended_x) {
		
		readers[__index__extended_x] = (item, reader, context) -> { item.x = org.revenj.postgres.converters.IntConverter.parse(reader); };
	}
}
