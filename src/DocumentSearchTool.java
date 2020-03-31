import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DocumentSearchTool {

	private Long						endTime;
	private Long						startTime;
	private Long						searchTime;

	private String						term;
	private String						path;
	private String						method;

	private Boolean						caseInsensitive;
	private Boolean						partialMatching;

	private Map< String, Long >			resultMap;

	private static Map< String, File >	indexMap;

	private static final String			INDEX_PATH		= "./index_files/";
	private static final String			DEFAULT_PATH	= "./sample_files/";
	private static final List< String >	SEARCH_METHODS	= Arrays.asList( "String Match", "Regular Expression", "Indexed" );

	private enum Method {
		STRING_MATCH, REGULAR_EXPRESSION, INDEXED;
	}

	private DocumentSearchTool( ) {
		searchTime = ( long ) 0;
		resultMap = new HashMap<>( );
	}

	/*
	 * processFiles
	 *
	 * Parse and process each file located within the specified directory
	 * documenting all occurrences of the provided search term per the user's
	 * specification.
	 */
	private void processFiles( ) throws Exception {

		File indexDir = new File( INDEX_PATH );
		if ( !indexDir.exists( ) ) {
			indexDir.mkdir( );
		}

		File fileDir = new File( StringUtils.isEmpty( path ) ? DEFAULT_PATH : path );
		if ( !fileDir.exists( ) || !fileDir.isDirectory( ) ) {
			throw new IOException( "The specified directory: " + fileDir.getPath( ) + " does not exist." );
		}

		indexMap = new HashMap<>( );
		startTime = System.nanoTime( );

		List< File > files = Arrays.asList( Objects.requireNonNull( fileDir.listFiles( ) ) );
		if ( files.isEmpty( ) ) {
			return;
		}

		for ( File file : files ) {

			if ( !file.exists( ) ) {
				throw new IOException( "The specified file: " + file.getPath( ) + " does not exist." );
			}

			String content = null;
			try ( BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( file ) ) ) ) {

				if ( StringUtils.equalsIgnoreCase( method, SEARCH_METHODS.get( Method.INDEXED.ordinal( ) ) ) ) {

					removePreviousIndexFile( file );
					File index = new File( INDEX_PATH + "/index_" + file.getName( ) );

					try ( RandomAccessFile randomAccessFile = new RandomAccessFile( index, "rw" ) ) {
						MappedByteBuffer buffer = randomAccessFile.getChannel( ).map( FileChannel.MapMode.READ_WRITE, 0, Integer.MAX_VALUE );

						reader.lines( ).parallel( ).forEach( line -> {

							if ( caseInsensitive ) {
								line = StringUtils.toLowerCase( line );
							}

							for ( String token : StringUtils.split( line, "\\s+" ) ) {

								int offset = Math.abs( token.hashCode( ) );
								long value = buffer.getLong( offset );
								buffer.putLong( offset, value + 1 );

							}

						} );

						indexMap.put( file.getName( ), index );
						index.deleteOnExit( );
						buffer.clear( );
					}

				} else {
					content = reader.lines( ).collect( Collectors.joining( "\n" ) );
					content = caseInsensitive ? StringUtils.toLowerCase( content ) : content;

				}
			}

			long start = System.nanoTime( );

			if ( StringUtils.equalsIgnoreCase( method, SEARCH_METHODS.get( Method.INDEXED.ordinal( ) ) ) ) {
				determineIndexedMatches( file.getName( ) );

			} else if ( StringUtils.equalsIgnoreCase( method, SEARCH_METHODS.get( Method.STRING_MATCH.ordinal( ) ) ) ) {
				if ( partialMatching ) {
					determineStringMatches( content, file.getName( ) );

				} else {
					determineRegexMatches( "\\b" + term + "\\b", content, file.getName( ) );

				}

			} else if ( StringUtils.equalsIgnoreCase( method, SEARCH_METHODS.get( Method.REGULAR_EXPRESSION.ordinal( ) ) ) ) {
				determineRegexMatches( term, content, file.getName( ) );

			} else {
				throw new Exception( "The specified Search Method: " + method + " does not exist." );

			}

			searchTime += System.nanoTime( ) - start;
		}

		endTime = System.nanoTime( );

	}

	/*
	 * removePreviousIndexFile
	 *
	 * Detect and remove any previously generated index files from past runs.
	 *
	 * There appears to be a bug within the RandomAccessFile.class implementation in
	 * which the referenced file is unable to be deleted following the termination
	 * of the current execution due to a Windows file lock.
	 *
	 * The following method is a work-around created to remove the previous index
	 * file prior to the creation of a new index file for the execution at hand.
	 */
	private void removePreviousIndexFile( File file ) {
		File index = new File( INDEX_PATH + "/index_" + file.getName( ) );
		if ( index.exists( ) ) {
			index.delete( );

		}
	}

	/*
	 * determineStringMatches
	 *
	 * Iterate over each token contained within the specified file, storing the
	 * number of String matches as they are encountered and storing the result
	 * within a HashMap for later use.
	 */
	private void determineStringMatches( String content, String filename ) {
		long matches = 0;
		int upper = StringUtils.indexOf( content, term );
		int lower = StringUtils.lastIndexOf( content, term );

		if ( upper == lower && upper != -1 ) {
			matches++;
		}

		while ( upper < lower ) {

			matches += 2;
			upper = StringUtils.indexOf( content, term, upper + 1 );
			lower = StringUtils.lastIndexOf( content, term, lower - 1 );

			if ( upper == lower && upper != -1 ) {
				matches++;
			}
		}

		resultMap.put( filename, matches );
	}

	/*
	 * determineRegexMatches
	 *
	 * Iterate over each token contained within the specified file, storing the
	 * number of Regular Expression matches as they are encountered and storing the
	 * result within a HashMap for later use.
	 */
	private void determineRegexMatches( String searchTerm, String content, String filename ) {
		long matches = 0;
		Pattern pattern = caseInsensitive ? Pattern.compile( searchTerm, Pattern.CASE_INSENSITIVE ) : Pattern.compile( searchTerm );

		for ( Matcher matcher = pattern.matcher( content ); matcher.find( ); ) {
			matches++;
		}

		resultMap.put( filename, matches );
	}

	/*
	 * determineIndexedMatches
	 *
	 * Calculate and access the corresponding memory location for the specified
	 * search term, storing the result within a HashMap for later use.
	 */
	private void determineIndexedMatches( String filename ) throws Exception {
		int offset = Math.abs( term.hashCode( ) );

		try ( RandomAccessFile randomAccessFile = new RandomAccessFile( indexMap.get( filename ), "rw" ) ) {
			MappedByteBuffer buffer = randomAccessFile.getChannel( ).map( FileChannel.MapMode.READ_WRITE, 0, Integer.MAX_VALUE );
			resultMap.put( filename, buffer.getLong( offset ) );
			buffer.clear( );

		} catch ( Exception ex ) {
			resultMap.put( filename, ( long ) 0 );

		}
	}

	/*
	 * promptUser
	 *
	 * Welcome the user to the Document Search Tool program, and prompt them for
	 * their desired search term, search functionality, etc.
	 */
	private void promptUser( ) throws Exception {
		try ( BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) ) ) {

			System.out.println( "--- Welcome to the Document Search Tool ---" );
			System.out.println( "\nPlease enter your desired Search Term." );
			System.out.print( "Desired Search Term: " );
			term = StringUtils.clean( reader.readLine( ) );

			System.out.println( "\nPlease select one of the provided Document Search Methods:" );
			System.out.println( "\n\t1.) String Match" );
			System.out.println( "\t2.) Regular Expression" );
			System.out.println( "\t3.) Indexed" );
			System.out.print( "\nDesired Document Search Method: " );
			method = StringUtils.clean( reader.readLine( ) );

			while ( !StringUtils.equalsIgnoreCase( SEARCH_METHODS, method ) ) {
				System.out.println( "\nIt appears you have not entered a valid Document Search Method, please do so now." );
				System.out.print( "Desired Document Search Method: " );
				method = StringUtils.clean( reader.readLine( ) );
			}

			if ( StringUtils.equalsIgnoreCase( "String Match", method ) ) {
				System.out.println( "By default, the Document Search will only return Exact Matches, would you like to disable Exact Matching?" );
				System.out.print( "Disable Exact Matching (yes / no): " );
				partialMatching = StringUtils.equalsIgnoreCase( StringUtils.clean( reader.readLine( ) ), "yes" );
				System.out.println( );
			}

			System.out.println( "By default, the Document Search will be Case-Sensitive, would you like to disable Case-Sensitivity?" );
			System.out.print( "Disable Case-Sensitive Searching (yes / no): " );
			caseInsensitive = StringUtils.equalsIgnoreCase( StringUtils.clean( reader.readLine( ) ), "yes" );
			term = caseInsensitive ? StringUtils.toLowerCase( term ) : term;

		}
	}

	/*
	 * displayResults
	 *
	 * Display the results of the specified search in order of relevance.
	 */
	private void displayResults( ) {
		System.out.println( "\nSearch results for term: " + term + "\n" );

		for ( Entry< String, Long > entry : sortEntries( ) ) {
			System.out.println( "\t" + entry.getKey( ) + " - " + entry.getValue( ) + " matches" );
		}
	}

	/*
	 * sortEntries
	 *
	 * Sort the results of the search by each file's corresponding number of matches
	 * for the specified search term.
	 */
	private List< Entry< String, Long > > sortEntries( ) {
		List< Entry< String, Long > > entries = new ArrayList<>( resultMap.entrySet( ) );
		entries.sort( Entry.comparingByValue( ) );
		Collections.reverse( entries );

		return entries;
	}

	/*
	 * displayElapsedTime
	 *
	 * Display the overall runtime of the program at hand, along with the runtime of
	 * the associated search method in milliseconds.
	 */
	private void displayElapsedTime( ) {
		long elapsedSearchTime = searchTime / 1000000;
		long elapsedTotalTime = ( endTime - startTime ) / 1000000;

		System.out.println( "\nTotal Search Time: " + elapsedSearchTime );
		System.out.println( "Total Elapsed Time: " + elapsedTotalTime );
	}

	public static void main( String[ ] args ) {
		try {

			DocumentSearchTool search = new DocumentSearchTool( );
			for ( int i = 0; i < args.length; i++ ) {
				if ( StringUtils.equalsIgnoreCase( "path", args[ i ] ) ) {
					search.path = args.length > i + 1 ? args[ i + 1 ] : "";
				}
			}

			search.promptUser( );
			search.processFiles( );
			search.displayResults( );
			search.displayElapsedTime( );

		} catch ( Exception ex ) {
			ex.printStackTrace( );

		}

	}

}
