package gen.model.test;



public final class Simple   implements java.io.Serializable {
	
	
	
	public Simple(
			final int number,
			final String text) {
			
		setNumber(number);
		setText(text);
	}

	
	
	public Simple() {
			
		this.number = 0;
		this.text = "";
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + 754965176;
		result = prime * result + (this.number);
		result = prime * result + (this.text.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;

		if (!(obj instanceof Simple))
			return false;
		final Simple other = (Simple) obj;
		
		if(!(this.number == other.number))
			return false;
		if(!(this.text.equals(other.text)))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return "Simple(" + number + ',' + text + ')';
	}
	
	private static final long serialVersionUID = 0x0097000a;
	
	private int number;

	
	public int getNumber()  {
		
		return number;
	}

	
	public Simple setNumber(final int value) {
		
		this.number = value;
		
		return this;
	}

	
	private String text;

	
	public String getText()  {
		
		return text;
	}

	
	public Simple setText(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"text\" cannot be null!");
		this.text = value;
		
		return this;
	}

	
	public Simple(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Simple>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Simple> rdr : readers) {
			rdr.read(this, reader, context);
		}
	}

	public static void configureConverter(org.revenj.postgres.ObjectConverter.Reader<Simple>[] readers, int __index___number, int __index___text) {
		
		readers[__index___number] = (item, reader, context) -> { item.number = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index___text] = (item, reader, context) -> { item.text = org.revenj.postgres.converters.StringConverter.parse(reader, context); };
	}
	
	public static void configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Simple>[] readers, int __index__extended_number, int __index__extended_text) {
		
		readers[__index__extended_number] = (item, reader, context) -> { item.number = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index__extended_text] = (item, reader, context) -> { item.text = org.revenj.postgres.converters.StringConverter.parse(reader, context); };
	}
}
