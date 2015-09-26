package gen.model.test;



public final class Simple   implements java.lang.Cloneable, java.io.Serializable {
	
	
	
	public Simple(
			final int number,
			final String text,
			final gen.model.test.En en,
			final gen.model.test.En en2,
			final Boolean nb,
			final java.time.OffsetDateTime ts) {
			
		setNumber(number);
		setText(text);
		setEn(en);
		setEn2(en2);
		setNb(nb);
		setTs(ts);
	}

	
	
	public Simple() {
			
		this.number = 0;
		this.text = "";
		this.en2 = gen.model.test.En.A;
		this.ts = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC);
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + 1414295721;
		result = prime * result + (this.number);
		result = prime * result + (this.text.hashCode());
		result = prime * result + (this.en != null ? this.en.hashCode() : 0);
		result = prime * result + (this.en2.hashCode());
		result = prime * result + (this.nb != null ? this.nb.hashCode() : 0);
		result = prime * result + (this.ts == null ? 0 : this.ts.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Simple))
			return false;
		return deepEquals((Simple) obj);
	}

	public boolean deepEquals(final Simple other) {
		if (other == null)
			return false;
		
		if(!(this.number == other.number))
			return false;
		if(!(this.text.equals(other.text)))
			return false;
		if(!(this.en == other.en || this.en != null && this.en.equals(other.en)))
			return false;
		if(!(this.en2.equals(other.en2)))
			return false;
		if(!(this.nb == other.nb || this.nb != null && this.nb.equals(other.nb)))
			return false;
		if(!(this.ts == other.ts || this.ts != null && other.ts != null && this.ts.equals(other.ts)))
			return false;
		return true;
	}

	private Simple(Simple other) {
		
		this.number = other.number;
		this.text = other.text;
		this.en = other.en;
		this.en2 = other.en2;
		this.nb = other.nb;
		this.ts = other.ts;
	}

	@Override
	public Object clone() {
		return new Simple(this);
	}

	@Override
	public String toString() {
		return "Simple(" + number + ',' + text + ',' + en + ',' + en2 + ',' + nb + ',' + ts + ')';
	}
	
	@com.fasterxml.jackson.annotation.JsonCreator private Simple(
			@com.fasterxml.jackson.annotation.JsonProperty("_helper") final boolean _helper ,
			@com.fasterxml.jackson.annotation.JsonProperty("number") final int number,
			@com.fasterxml.jackson.annotation.JsonProperty("text") final String text,
			@com.fasterxml.jackson.annotation.JsonProperty("en") final gen.model.test.En en,
			@com.fasterxml.jackson.annotation.JsonProperty("en2") final gen.model.test.En en2,
			@com.fasterxml.jackson.annotation.JsonProperty("nb") final Boolean nb,
			@com.fasterxml.jackson.annotation.JsonProperty("ts") final java.time.OffsetDateTime ts) {
		
		this.number = number;
		this.text = text == null ? "" : text;
		this.en = en;
		this.en2 = en2 == null ? gen.model.test.En.A : en2;
		this.nb = nb;
		this.ts = ts == null ? java.time.OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, java.time.ZoneOffset.UTC) : ts;
	}

	private static final long serialVersionUID = 1675082603100134519L;
	
	private int number;

	
	@com.fasterxml.jackson.annotation.JsonProperty("number")
	public int getNumber()  {
		
		return number;
	}

	
	public Simple setNumber(final int value) {
		
		this.number = value;
		
		return this;
	}

	
	private String text;

	
	@com.fasterxml.jackson.annotation.JsonProperty("text")
	public String getText()  {
		
		return text;
	}

	
	public Simple setText(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"text\" cannot be null!");
		this.text = value;
		
		return this;
	}

	
	private gen.model.test.En en;

	
	@com.fasterxml.jackson.annotation.JsonProperty("en")
	public gen.model.test.En getEn()  {
		
		return en;
	}

	
	public Simple setEn(final gen.model.test.En value) {
		
		this.en = value;
		
		return this;
	}

	
	private gen.model.test.En en2;

	
	@com.fasterxml.jackson.annotation.JsonProperty("en2")
	public gen.model.test.En getEn2()  {
		
		return en2;
	}

	
	public Simple setEn2(final gen.model.test.En value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"en2\" cannot be null!");
		this.en2 = value;
		
		return this;
	}

	
	private Boolean nb;

	
	@com.fasterxml.jackson.annotation.JsonProperty("nb")
	public Boolean getNb()  {
		
		return nb;
	}

	
	public Simple setNb(final Boolean value) {
		
		this.nb = value;
		
		return this;
	}

	
	private java.time.OffsetDateTime ts;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ts")
	public java.time.OffsetDateTime getTs()  {
		
		return ts;
	}

	
	public Simple setTs(final java.time.OffsetDateTime value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"ts\" cannot be null!");
		this.ts = value;
		
		return this;
	}

	
	public Simple(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Simple>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Simple> rdr : readers) {
			rdr.read(this, reader, context);
		}
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Simple>[] readers, int __index___number, int __index___text, int __index___en, int __index___en2, int __index___nb, int __index___ts) {
		
		readers[__index___number] = (item, reader, context) -> { item.number = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index___text] = (item, reader, context) -> { item.text = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); };
		readers[__index___en] = (item, reader, context) -> { item.en = gen.model.test.converters.EnConverter.fromReader(reader); };
		readers[__index___en2] = (item, reader, context) -> { item.en2 = gen.model.test.converters.EnConverter.fromReader(reader); };
		readers[__index___nb] = (item, reader, context) -> { item.nb = org.revenj.postgres.converters.BoolConverter.parseNullable(reader); };
		readers[__index___ts] = (item, reader, context) -> { item.ts = org.revenj.postgres.converters.TimestampConverter.parseOffset(reader, context, false, true); };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Simple>[] readers, int __index__extended_number, int __index__extended_text, int __index__extended_en, int __index__extended_en2, int __index__extended_nb, int __index__extended_ts) {
		
		readers[__index__extended_number] = (item, reader, context) -> { item.number = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index__extended_text] = (item, reader, context) -> { item.text = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); };
		readers[__index__extended_en] = (item, reader, context) -> { item.en = gen.model.test.converters.EnConverter.fromReader(reader); };
		readers[__index__extended_en2] = (item, reader, context) -> { item.en2 = gen.model.test.converters.EnConverter.fromReader(reader); };
		readers[__index__extended_nb] = (item, reader, context) -> { item.nb = org.revenj.postgres.converters.BoolConverter.parseNullable(reader); };
		readers[__index__extended_ts] = (item, reader, context) -> { item.ts = org.revenj.postgres.converters.TimestampConverter.parseOffset(reader, context, false, true); };
	}
}
