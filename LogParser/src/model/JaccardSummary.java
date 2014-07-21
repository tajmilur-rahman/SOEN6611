package model;

public class JaccardSummary {
	public final String author;
	public final int period;
	public final int intersect;
	public final int union;
	public final double jaccard;
	
	public JaccardSummary(String author, int period, int intersect, int union, double jaccard) {
		this.author = author;
		this.period = period;
		this.intersect = intersect;
		this.union = union;
		this.jaccard = jaccard;
	}
}
