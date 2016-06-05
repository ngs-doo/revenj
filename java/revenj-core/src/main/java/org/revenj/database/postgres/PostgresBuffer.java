package org.revenj.database.postgres;

public interface PostgresBuffer {

	char[] getTempBuffer();

	void initBuffer();

	void initBuffer(char c);

	void addToBuffer(char c);

	void addToBuffer(char[] c);

	void addToBuffer(char[] c, int len);

	void addToBuffer(char[] c, int start, int emd);

	void addToBuffer(String input);

	String bufferToString();
}
