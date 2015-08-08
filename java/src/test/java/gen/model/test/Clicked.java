package gen.model.test;



public final class Clicked   implements java.io.Serializable, org.revenj.patterns.DomainEvent {
	
	
	
	public Clicked(
			 final java.time.LocalDate date,
			 final java.math.BigDecimal number,
			 final Long bigint,
			 final java.util.Set<Boolean> bool,
			 final gen.model.test.En en) {
			
		setDate(date);
		setNumber(number);
		this.number = java.math.BigDecimal.ZERO;
		setBigint(bigint);
		setBool(bool);
		this.bool = new java.util.HashSet<Boolean>(4);
		setEn(en);
	}

	
	
	public Clicked() {
			
		this.number = java.math.BigDecimal.ZERO;
		this.bool = new java.util.HashSet<Boolean>(4);
	}

	
	private String URI;

	
	@com.fasterxml.jackson.annotation.JsonProperty("URI")
	public String getURI()  {
		
		return this.URI;
	}

	
	private java.time.OffsetDateTime ProcessedAt;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ProcessedAt")
	public java.time.OffsetDateTime getProcessedAt()  {
		
		return this.ProcessedAt;
	}

	
	private java.time.OffsetDateTime QueuedAt;

	
	@com.fasterxml.jackson.annotation.JsonProperty("QueuedAt")
	public java.time.OffsetDateTime getQueuedAt()  {
		
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
	private static final long serialVersionUID = 400408826033464727L;
	
	private java.time.LocalDate date;

	
	@com.fasterxml.jackson.annotation.JsonProperty("date")
	public java.time.LocalDate getDate()  {
		
		return date;
	}

	
	public Clicked setDate(final java.time.LocalDate value) {
		
		this.date = value;
		
		return this;
	}

	
	private java.math.BigDecimal number;

	
	@com.fasterxml.jackson.annotation.JsonProperty("number")
	public java.math.BigDecimal getNumber()  {
		
		return number;
	}

	
	public Clicked setNumber(final java.math.BigDecimal value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"number\" cannot be null!");
		this.number = value;
		
		return this;
	}

	
	private Long bigint;

	
	@com.fasterxml.jackson.annotation.JsonProperty("bigint")
	public Long getBigint()  {
		
		return bigint;
	}

	
	public Clicked setBigint(final Long value) {
		
		this.bigint = value;
		
		return this;
	}

	
	private java.util.Set<Boolean> bool;

	
	@com.fasterxml.jackson.annotation.JsonProperty("bool")
	public java.util.Set<Boolean> getBool()  {
		
		return bool;
	}

	
	public Clicked setBool(final java.util.Set<Boolean> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"bool\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.bool = value;
		
		return this;
	}

	
	private gen.model.test.En en;

	
	@com.fasterxml.jackson.annotation.JsonProperty("en")
	public gen.model.test.En getEn()  {
		
		return en;
	}

	
	public Clicked setEn(final gen.model.test.En value) {
		
		this.en = value;
		
		return this;
	}

	

public static class BetweenNumbers   implements java.io.Serializable, org.revenj.patterns.Specification<Clicked> {
	
	
	
	public BetweenNumbers(
			 final java.math.BigDecimal min,
			 final java.util.Set<java.math.BigDecimal> inSet,
			 final gen.model.test.En en) {
			
		setMin(min);
		setInSet(inSet);
		setEn(en);
	}

	
	
	public BetweenNumbers() {
			
		this.min = java.math.BigDecimal.ZERO;
		this.inSet = new java.util.HashSet<java.math.BigDecimal>(4);
	}

	private static final long serialVersionUID = 3349159424317714348L;
	
	private java.math.BigDecimal min;

	
	@com.fasterxml.jackson.annotation.JsonProperty("min")
	public java.math.BigDecimal getMin()  {
		
		return min;
	}

	
	public BetweenNumbers setMin(final java.math.BigDecimal value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"min\" cannot be null!");
		this.min = value;
		
		return this;
	}

	
	private java.util.Set<java.math.BigDecimal> inSet;

	
	@com.fasterxml.jackson.annotation.JsonProperty("inSet")
	public java.util.Set<java.math.BigDecimal> getInSet()  {
		
		return inSet;
	}

	
	public BetweenNumbers setInSet(final java.util.Set<java.math.BigDecimal> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"inSet\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.inSet = value;
		
		return this;
	}

	
	private gen.model.test.En en;

	
	@com.fasterxml.jackson.annotation.JsonProperty("en")
	public gen.model.test.En getEn()  {
		
		return en;
	}

	
	public BetweenNumbers setEn(final gen.model.test.En value) {
		
		this.en = value;
		
		return this;
	}

	
		public boolean test(gen.model.test.Clicked it) {
			return ( ( it.getNumber().compareTo(this.getMin()) >= 0 && (this.getInSet().contains(it.getNumber()))) &&  it.getEn().equals(this.getEn()));
		}
}

	
	@com.fasterxml.jackson.annotation.JsonCreator private Clicked(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JsonProperty("ProcessedAt") final java.time.OffsetDateTime ProcessedAt,
			@com.fasterxml.jackson.annotation.JsonProperty("QueuedAt") final java.time.OffsetDateTime QueuedAt,
			@com.fasterxml.jackson.annotation.JsonProperty("date") final java.time.LocalDate date,
			@com.fasterxml.jackson.annotation.JsonProperty("number") final java.math.BigDecimal number,
			@com.fasterxml.jackson.annotation.JsonProperty("bigint") final Long bigint,
			@com.fasterxml.jackson.annotation.JsonProperty("bool") final java.util.Set<Boolean> bool,
			@com.fasterxml.jackson.annotation.JsonProperty("en") final gen.model.test.En en) {
		this.URI = URI != null ? URI : "new " + new java.util.UUID(0L, 0L).toString();
		this.ProcessedAt = ProcessedAt == null ? null : ProcessedAt;
		this.QueuedAt = QueuedAt == null ? null : QueuedAt;
		this.date = date;
		this.number = number == null ? java.math.BigDecimal.ZERO : number;
		this.bigint = bigint;
		this.bool = bool == null ? new java.util.HashSet<Boolean>(4) : bool;
		this.en = en;
	}

	
	public Clicked(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Clicked>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Clicked> rdr : readers) {
			rdr.read(this, reader, context);
		}
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Clicked>[] readers, int __index____event_id, int __index___QueuedAt, int __index___ProcessedAt, int __index___date, int __index___number, int __index___bigint, int __index___bool, int __index___en) {
		
		readers[__index____event_id] = (item, reader, context) -> { item.URI = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); };
		readers[__index___QueuedAt] = (item, reader, context) -> { item.QueuedAt = org.revenj.postgres.converters.TimestampConverter.parseOffset(reader, context, false, true); };
		readers[__index___ProcessedAt] = (item, reader, context) -> { item.ProcessedAt = org.revenj.postgres.converters.TimestampConverter.parseOffset(reader, context, true, true); };
		readers[__index___date] = (item, reader, context) -> { item.date = org.revenj.postgres.converters.DateConverter.parse(reader, true); };
		readers[__index___number] = (item, reader, context) -> { item.number = org.revenj.postgres.converters.DecimalConverter.parse(reader, false); };
		readers[__index___bigint] = (item, reader, context) -> { item.bigint = org.revenj.postgres.converters.LongConverter.parseNullable(reader); };
		readers[__index___bool] = (item, reader, context) -> { { java.util.List<Boolean> __list = org.revenj.postgres.converters.BoolConverter.parseCollection(reader, context, false); if(__list != null) item.bool = new java.util.HashSet<Boolean>(__list); else item.bool = new java.util.HashSet<Boolean>(4); }; };
		readers[__index___en] = (item, reader, context) -> { item.en = gen.model.test.converters.EnConverter.fromReader(reader); };
	}
}
