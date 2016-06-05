package org.revenj.serialization.json;

import com.dslplatform.json.*;
import com.dslplatform.json.NumberConverter;
import org.revenj.patterns.ServiceLocator;
import org.revenj.storage.S3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public abstract class StorageConverter {

	public static final JsonReader.ReadObject<S3> S3Reader = StorageConverter::deserializeS3;
	public static final JsonWriter.WriteObject<S3> S3Writer = (writer, value) -> {
		if (value == null) {
			writer.writeNull();
		} else {
			serialize(value, writer);
		}
	};

	public static void serialize(final S3 value, final JsonWriter sw) {
		sw.writeAscii("{\"Bucket\":");
		StringConverter.serializeNullable(value.getBucket(), sw);
		sw.writeAscii(",\"Key\":");
		StringConverter.serializeNullable(value.getKey(), sw);
		sw.writeAscii(",\"Length\":");
		com.dslplatform.json.NumberConverter.serialize(value.getLength(), sw);
		sw.writeAscii(",\"Name\":");
		StringConverter.serializeNullable(value.getName(), sw);
		sw.writeAscii(",\"MimeType\":");
		StringConverter.serializeNullable(value.getMimeType(), sw);
		sw.writeAscii(",\"Metadata\":");
		MapConverter.serializeNullable(value.getMetadata(), sw);
		sw.writeByte(JsonWriter.OBJECT_END);
	}

	public static S3 deserializeS3(final JsonReader<ServiceLocator> reader) throws IOException {
		String bucket = null;
		String key = null;
		long length = 0;
		String name = null;
		String mimeType = null;
		Map<String, String> metadata = null;
		String field;
		if (reader.last() != '}') {
			reader.getNextToken();
			field = reader.readString();
			if (reader.getNextToken() != ':') {
				throw new IOException("Expecting ':' at " + reader.getCurrentIndex());
			}
			reader.getNextToken();
			if ("Bucket".equalsIgnoreCase(field)) {
				bucket = StringConverter.deserializeNullable(reader);
			} else if ("Key".equalsIgnoreCase(field)) {
				key = StringConverter.deserializeNullable(reader);
			} else if ("Length".equalsIgnoreCase(field)) {
				length = com.dslplatform.json.NumberConverter.deserializeLong(reader);
			} else if ("Name".equalsIgnoreCase(field)) {
				name = StringConverter.deserializeNullable(reader);
			} else if ("MimeType".equalsIgnoreCase(field)) {
				mimeType = StringConverter.deserializeNullable(reader);
			} else if ("Metadata".equalsIgnoreCase(field)) {
				metadata = MapConverter.deserialize(reader);
			} else {
				reader.skip();
			}
			while (reader.getNextToken() == ',') {
				reader.getNextToken();
				field = reader.readString();
				if (reader.getNextToken() != ':') {
					throw new IOException("Expecting ':' at" + reader.getCurrentIndex());
				}
				reader.getNextToken();
				if ("Bucket".equalsIgnoreCase(field)) {
					bucket = StringConverter.deserializeNullable(reader);
				} else if ("Key".equalsIgnoreCase(field)) {
					key = StringConverter.deserializeNullable(reader);
				} else if ("Length".equalsIgnoreCase(field)) {
					length = NumberConverter.deserializeLong(reader);
				} else if ("Name".equalsIgnoreCase(field)) {
					name = StringConverter.deserializeNullable(reader);
				} else if ("MimeType".equalsIgnoreCase(field)) {
					mimeType = StringConverter.deserializeNullable(reader);
				} else if ("Metadata".equalsIgnoreCase(field)) {
					metadata = MapConverter.deserialize(reader);
				} else {
					reader.skip();
				}
			}
			if (reader.last() != '}') {
				throw new IOException("Expecting '}' at " + reader.getCurrentIndex());
			}
		}
		return new S3(bucket, key, length, name, mimeType, metadata);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<S3> deserializeS3Collection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(S3Reader);
	}

	public static void deserializeS3Collection(final JsonReader reader, final Collection<S3> res) throws IOException {
		reader.deserializeCollection(S3Reader, res);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<S3> deserializeS3NullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(S3Reader);
	}

	public static void deserializeS3NullableCollection(final JsonReader reader, final Collection<S3> res) throws IOException {
		reader.deserializeNullableCollection(S3Reader, res);
	}
}
