import java.util.AbstractMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class WordSearcher {

	private enum Direction {

		HORIZONTAL_RIGHT(0, 1),
		HORIZONTAL_LEFT(0, -1),
		VERTICAL_UP(-1, 0),
		VERTICAL_DOWN(1, 0),
		DIAGONAL_UP_RIGHT(-1, 1),
		DIAGONAL_UP_LEFT(-1, -1),
		DIAGONAL_DOWN_RIGHT(1, 1),
		DIAGONAL_DOWN_LEFT(1, -1);

		private final int incrementX;
		private final int incrementY;

		private Direction(final int y, final int x) {
			this.incrementY = y;
			this.incrementX = x;
		}

		public int getIncrementX() {
			return incrementX;
		}

		public int getIncrementY() {
			return incrementY;
		}

		public static Direction getDirectionByIncrements(final int x, final int y) {
			for (Direction d : EnumSet.allOf(Direction.class)) {
				if (d.getIncrementX() == x && d.getIncrementY() == y) {
					return d;
				}
			}
			return null;
		}

	}

	public Map<String, Optional<WordLocation>> search(final Set<String> searchWords, final char[][] vv) {
		return searchWords
				.stream()
				.map(word -> search(word, vv))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	private Entry<String, Optional<WordLocation>> search(final String searchWord, final char[][] vv) {
		char firstLetter = searchWord.charAt(0);

		Optional<WordLocation> wordLocation =
				IntStream.rangeClosed(1, vv.length)
      	.mapToObj(y ->
      		IntStream.rangeClosed(1, vv[y - 1].length)
      			.filter(x -> firstLetter == vv[y - 1][x - 1])
      			.mapToObj(x -> searchFrom(searchWord, vv, y, x))
        		.filter(optionalWordLocation -> optionalWordLocation.isPresent())
      	)
    		.flatMap(in -> in)
    		.findFirst()
    		.orElse(Optional.empty());

		return new AbstractMap.SimpleEntry<>(searchWord, wordLocation);

	}

	private Optional<WordLocation> searchFrom(final String searchWord, final char[][] vv, final int startY, final int startX) {
		final int limitY = vv.length;
		final int limitX = vv[0].length;
		final char secondChar = searchWord.charAt(1);

		Optional<Pair> findFirst =
				IntStream.rangeClosed(startY - 1, startY + 1)
				.mapToObj(y ->
      		IntStream.rangeClosed(startX - 1, startX + 1)
    		    .filter(x -> y > 0 && y <= limitY && x > 0 && x <= limitX && secondChar == vv[y - 1][x - 1])
        		.mapToObj(colIndex -> {
      				int diffY = y - startY;
      				int diffX = colIndex - startX;
      				Direction dir = Direction.getDirectionByIncrements(diffX, diffY);
      				return verifyDirection(searchWord, vv, y, colIndex, dir);
        		})
        		.filter(pair -> pair != null)
    		)
    		.flatMap(in -> in)
    		.findFirst();

		if (findFirst.isPresent()) {
			return Optional.of(new WordLocation(new Pair(startX, startY), findFirst.get()));
		}
		return Optional.empty();
	}

	private Pair verifyDirection(final String searchWord, final char[][] vv, final int startY, final int startX, final Direction dir) {
		final int limitY = vv.length;
		final int limitX = vv[0].length;
		AtomicInteger y = new AtomicInteger(startY);
		AtomicInteger x = new AtomicInteger(startX);
		AtomicInteger idx = new AtomicInteger(2);

		boolean allMatch = Stream
			.generate(() -> new Pair(x.addAndGet(dir.getIncrementX()), y.addAndGet(dir.getIncrementY())))
			.limit(searchWord.length() - 2)
			.allMatch(pair -> {
		    boolean first = pair.getY() > 0 && pair.getY() <= limitY && pair.getX() > 0 && pair.getX() <= limitX;
		    return first && searchWord.charAt(idx.getAndIncrement()) == vv[pair.getY() - 1][pair.getX() - 1];
			});

		return allMatch ? new Pair(x.get(), y.get()) : null;
	}

	public static void main(final String[] args) {
		WordSearcher wordSearcher= new WordSearcher();
		Map<String, Optional<WordLocation>> expectedLocations = new HashMap<>();
		expectedLocations.put("coffee", Optional.of(new WordLocation(new Pair(2, 1), new Pair(7, 1))));

		Set<String> searchWords = expectedLocations.keySet();

		Map<String, Optional<WordLocation>> actualLocations = wordSearcher.search(
				searchWords,
				new char[][]{
					{'x', 'c', 'o', 'f', 'f', 'e', 'e', 'z', 'l', 'p'}
				}
				);

		System.out.println(actualLocations);
	}

}