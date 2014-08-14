package model;

public class JaccardSummary {
	public final String author;
	public final int period;
	public final int intersect;
	public final int union;
	public final double jaccard;
	public final int weightedIntersect;
	public final int weightedUnion;
	public final double weightedJaccard;
	
	public JaccardSummary(String author, int period, int intersect, int union, double jaccard, int weightedIntersect, int weightedUnion, double weightedJaccard) {
		this.author = author;
		this.period = period;
		this.intersect = intersect;
		this.union = union;
		this.jaccard = jaccard;
		this.weightedIntersect = weightedIntersect;
		this.weightedUnion = weightedUnion;
		this.weightedJaccard = weightedJaccard;
	}
}
