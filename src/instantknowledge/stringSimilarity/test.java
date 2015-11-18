package instantknowledge.stringSimilarity;

/**
 *
 * @author Aale
 */
public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SimilarityStrategy strategy = new JaroWinklerStrategy();
		String target = "McDonalds";
		String source = "McDonalds";
		StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
		double score = service.score(source, target);
		System.out.println(score);

	}

}
