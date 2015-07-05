package gen.model.test;



public final class Clicked   implements java.io.Serializable {
	
	
	
	public Clicked(
			 final java.time.LocalDate date,
			 final java.math.BigDecimal number,
			 final Long bigint,
			 final java.util.Set<Boolean> bool) {
			
		setDate(date);
		setNumber(number);
		this.number = java.math.BigDecimal.ZERO;
		setBigint(bigint);
		setBool(bool);
		this.bool = new java.util.HashSet<Boolean>(4);
	}

	
	
	public Clicked() {
			
		this.number = java.math.BigDecimal.ZERO;
		this.bool = new java.util.HashSet<Boolean>(4);
	}

	
	private String URI;

	
	public String getURI()  {
		
		return this.URI;
	}

	
	private java.time.LocalDateTime ProcessedAt;

	
	public java.time.LocalDateTime getProcessedAt()  {
		
		return this.ProcessedAt;
	}

	
	private java.time.LocalDateTime QueuedAt;

	
	public java.time.LocalDateTime getQueuedAt()  {
		
		return this.QueuedAt;
	}

	
	@Override
	public int hashCode() {
		return URI != null ? URI.hashCode() : super.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;
		final Clicked other = (Clicked) obj;

		return URI != null && URI.equals(other.URI);
	}

	@Override
	public String toString() {
		return URI != null ? "Clicked(" + URI + ')' : "new Clicked(" + super.hashCode() + ')';
	}
	
	private static final long serialVersionUID = 0x0097000a;
	
	private java.time.LocalDate date;

	
	public java.time.LocalDate getDate()  {
		
		return date;
	}

	
	public Clicked setDate(final java.time.LocalDate value) {
		
		this.date = value;
		
		return this;
	}

	
	private java.math.BigDecimal number;

	
	public java.math.BigDecimal getNumber()  {
		
		return number;
	}

	
	public Clicked setNumber(final java.math.BigDecimal value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"number\" cannot be null!");
		this.number = value;
		
		return this;
	}

	
	private Long bigint;

	
	public Long getBigint()  {
		
		return bigint;
	}

	
	public Clicked setBigint(final Long value) {
		
		this.bigint = value;
		
		return this;
	}

	
	private java.util.Set<Boolean> bool;

	
	public java.util.Set<Boolean> getBool()  {
		
		return bool;
	}

	
	public Clicked setBool(final java.util.Set<Boolean> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"bool\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.bool = value;
		
		return this;
	}

	
	public Clicked(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Clicked>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Clicked> rdr : readers) {
			rdr.read(this, reader, context);
		}
	}

	public static void configureConverter(org.revenj.postgres.ObjectConverter.Reader<Clicked>[] readers, int __index____event_id, int __index___QueuedAt, int __index___ProcessedAt, int __index___date, int __index___number, int __index___bigint) {
		
		readers[__index____event_id] = (item, reader, context) -> { item.URI = org.revenj.postgres.converters.StringConverter.parse(reader, context); };
		readers[__index___QueuedAt] = (item, reader, context) -> { item.QueuedAt = org.revenj.postgres.converters.TimestampConverter.parse(reader, context, false); };
		readers[__index___ProcessedAt] = (item, reader, context) -> { item.ProcessedAt = org.revenj.postgres.converters.TimestampConverter.parse(reader, context, true); };
		readers[__index___date] = (item, reader, context) -> { item.date = org.revenj.postgres.converters.DateConverter.parse(reader, true); };
		readers[__index___number] = (item, reader, context) -> { item.number = org.revenj.postgres.converters.DecimalConverter.parse(reader, false); };
		readers[__index___bigint] = (item, reader, context) -> { item.bigint = org.revenj.postgres.converters.LongConverter.parseNullable(reader); };
	}
}
