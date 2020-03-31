import java.util.List;

public class StringUtils {

	public static boolean equals( String strA, String strB ) {
		if ( strA == null ) {
			return strB == null;
		}

		return strA.equals( strB );
	}

	public static boolean equalsIgnoreCase( String strA, String strB ) {
		return strA == null ? strB == null : strA.equalsIgnoreCase( strB );
	}

	public static boolean equalsIgnoreCase( List< String > stringList, String searchStr ) {
		for ( String str : stringList ) {
			if ( StringUtils.equalsIgnoreCase( str, searchStr ) ) {
				return true;
			}
		}

		return false;
	}

	public static long parseLong( String str ) {
		try {
			return Long.parseLong( str );

		} catch ( Exception ex ) {
			return 0;

		}
	}

	public static String[ ] split( String str, String regex ) {
		if ( StringUtils.isEmpty( str ) ) {
			return new String[ ] { };
		}

		return str.split( regex );
	}

	public static String substring( String str, int begin, int end ) {
		if ( StringUtils.isEmpty( str ) ) {
			return "";
		}

		if ( begin > end || begin < 0 || end > str.length( ) ) {
			return "";
		}

		return str.substring( begin, end );
	}

	public static String rightChomp( String str, String searchStr ) {
		if ( StringUtils.isEmpty( str ) ) {
			return "";
		} else if ( StringUtils.isEmpty( searchStr ) ) {
			return str;
		}

		if ( StringUtils.lastIndexOf( str, searchStr ) == -1 ) {
			return "";
		}

		return StringUtils.substring( str, StringUtils.lastIndexOf( str, searchStr ) + 1, str.length( ) );
	}

	public static String leftChomp( String str, String searchStr ) {
		if ( StringUtils.isEmpty( str ) ) {
			return "";
		} else if ( StringUtils.isEmpty( searchStr ) ) {
			return str;
		}

		if ( StringUtils.lastIndexOf( str, searchStr ) == 0 ) {
			return "";
		}

		return StringUtils.substring( str, 0, StringUtils.lastIndexOf( str, searchStr ) );
	}

	public static String replace( String str, String regex, String replacement ) {
		if ( StringUtils.isEmpty( str ) ) {
			return "";
		} else if ( StringUtils.isEmpty( regex ) ) {
			return str;
		}

		return str.replaceAll( regex, StringUtils.isEmpty( replacement ) ? "" : replacement );
	}

	public static String stripSpecialCharacters( String str ) {
		return StringUtils.stripSpecialCharacters( str, "[^a-zA-Z0-9\\s+]" );
	}

	public static String stripSpecialCharacters( String str, String regex ) {
		return StringUtils.replace( str, regex, "" );
	}

	public static String toLowerCase( String str ) {
		if ( StringUtils.isEmpty( str ) ) {
			return "";
		}

		return str.toLowerCase( );
	}

	public static int indexOf( String str, String searchStr ) {
		if ( StringUtils.isEmpty( str ) || StringUtils.isEmpty( searchStr ) ) {
			return -1;
		}

		return str.indexOf( searchStr );
	}

	public static int lastIndexOf( String str, String searchStr ) {
		if ( StringUtils.isEmpty( str ) || StringUtils.isEmpty( searchStr ) ) {
			return -1;
		}

		return str.lastIndexOf( searchStr );
	}

	public static int indexOf( String str, String searchStr, int index ) {
		if ( StringUtils.isEmpty( str ) || StringUtils.isEmpty( searchStr ) ) {
			return -1;
		}

		return str.indexOf( searchStr, index );
	}

	public static int lastIndexOf( String str, String searchStr, int index ) {
		if ( StringUtils.isEmpty( str ) || StringUtils.isEmpty( searchStr ) ) {
			return -1;
		}

		return str.lastIndexOf( searchStr, index );
	}

	public static boolean isEmpty( String str ) {
		return str == null || str.equals( "" );
	}

	public static String clean( String str ) {
		return StringUtils.isEmpty( str ) ? "" : str.trim( );
	}
}
