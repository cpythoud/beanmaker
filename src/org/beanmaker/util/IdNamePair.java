package org.beanmaker.util;

public class IdNamePair {

	private final String id;
	private final String name;
	
	public IdNamePair(final int id, final String name) {
		if (id < 0)
			throw new IllegalArgumentException("id must be zero or positive");
		
		this.id = Integer.toString(id);
		this.name = name;
	}
	
	public IdNamePair(final long id, final String name) {
		if (id < 0)
			throw new IllegalArgumentException("id must be zero or positive");
		
		this.id = Long.toString(id);
		this.name = name;
	}
	
	public IdNamePair(final String id, final String name) {
		this.id = id;
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
}

