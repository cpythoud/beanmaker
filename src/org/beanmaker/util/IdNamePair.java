package org.beanmaker.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IdNamePair implements Comparable<IdNamePair> {

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

	// Assumes only one "please select ..." element with id "0", throws an IllegalStateException otherwise.
	@Override
	public int compareTo(final IdNamePair idNamePair) {
		if (id.equals("0") && idNamePair.id.equals("0"))
			throw new IllegalStateException("More than one 'please select' field in IdNamePair collection.");

		if (id.equals("0"))
			return -1;
		if (idNamePair.id.equals("0"))
			return 1;

		return name.compareTo(idNamePair.name);
	}

	public static <T extends IdNamePairBean> List<IdNamePair> getPairs(final List<T> beans) {
		return getPairs(beans, null);
	}

	public static <T extends IdNamePairBean> List<IdNamePair> getPairs(
			final List<T> beans,
			final String noSelectionText)
	{
		return getPairs(beans, noSelectionText, false);
	}

	public static <T extends IdNamePairBean> List<IdNamePair> getPairs(
			final List<T> beans,
			final String noSelectionText,
			final boolean sortOnName)
	{
		final List<IdNamePair> pairs = new ArrayList<IdNamePair>();

		for (IdNamePairBean bean: beans)
			pairs.add(new IdNamePair(bean.getId(), bean.getNameForPair()));

		if (sortOnName)
			Collections.sort(pairs);

		if (noSelectionText != null)
			pairs.add(0, new IdNamePair(0, noSelectionText));

		return pairs;
	}

	public static <T extends MultilingualIdNamePairBean> List<IdNamePair> getMultilingualPairs(
			final List<T> beans,
			final DbBeanLanguage dbBeanLanguage)
	{
		return getMultilingualPairs(beans, dbBeanLanguage, null);
	}

	public static <T extends MultilingualIdNamePairBean> List<IdNamePair> getMultilingualPairs(
			final List<T> beans,
			final DbBeanLanguage dbBeanLanguage,
			final String noSelectionText)
	{
		return getMultilingualPairs(beans, dbBeanLanguage, noSelectionText, false);
	}

	public static <T extends MultilingualIdNamePairBean> List<IdNamePair> getMultilingualPairs(
			final List<T> beans,
			final DbBeanLanguage dbBeanLanguage,
			final String noSelectionText,
			final boolean sortOnName)
	{
		final List<IdNamePair> pairs = new ArrayList<IdNamePair>();

		for (MultilingualIdNamePairBean bean: beans)
			pairs.add(new IdNamePair(bean.getId(), bean.getNameForPair(dbBeanLanguage)));

		if (sortOnName)
			Collections.sort(pairs);

		if (noSelectionText != null)
			pairs.add(0, new IdNamePair(0, noSelectionText));

		return pairs;
	}
}

