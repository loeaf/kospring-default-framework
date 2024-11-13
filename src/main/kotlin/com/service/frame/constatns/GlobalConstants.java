package com.service.frame.constatns;

public class GlobalConstants {

	public static final String SESSION_COOKIE_NAME = "SESSION";
	public static final String SESSION_NAME = "SessionDTO";

	/**
	 * Yes 값
	 */
	public static final String Yes = "Yes";
	
	/**
	 * No 값
	 */
	public static final String No = "No";
	
	/**
	 * YES 값
	 */
	public static final String YES = "YES";
	
	/**
	 * NO 값
	 */
	public static final String NO = "NO";
	
	/**
	 * Y 값
	 */
	public static final String Y = "Y";
	
	/**
	 * N 값
	 */
	public static final String N = "N";
	
	/**
	 * 콤마(,) 를 나타내는 값
	 */
	public static final String COMMA = ",";
	
	/**
	 * 쌍점(:) 을 나타내는 값
	 */
	public static final String COLON = ":";
	
	/**
	 * 세미콜론(;)을 나타내는 값
	 */
	public static final String SEMI_COLON = ";";
	
	/**
	 * 공백문자열 : 공백(스페이스) 0개를 나타내는 값
	 */
	public static final String SPACE_0 = "";

	/**
	 * 공백(스페이스) 1개를 나타내는 값
	 */
	public static final String SPACE_1 = " ";

	/**
	 * 언더스코어(_) 를 나타내는 값
	 */
	public static final String UNDER_SCORE = "_";
	
	/**
	 * 별표(* : asterisk) 를 나타내는 값
	 */
	public static final String ASTERISK = "*";
	
	/**
	 * 대쉬(-) 를 나타내는 값
	 */
	public static final String DASH = "-";
	
	/**
	 * 슬래쉬(/)를 나타내는 값
	 */
	public static final String SLASH = "/";
	
	/**
	 * 물결표(Tilde : 틸드)(~)를 나타내는 값
	 */
	public static final String TILDE = "~";
	
	/**
	 * 퍼센트(Percent : 퍼센트)(%)를 나타내는 값
	 */
	public static final String PERCENT = "%";
	
	/**
	 * 여는 소괄호(() 를 나타내는 값 
	 */
	public static final String PARENTHESE_OPEN = "(";

	/**
	 * 닫는  소괄호()) 를 나타내는 값 
	 */
	public static final String PARENTHESE_CLOSE = ")";
	
	/**
	 * 여는 대괄호([) 를 나타내는 값 
	 */
	public static final String BRACKET_OPEN = "[";

	/**
	 * 닫는 대괄호(]) 를 나타내는 값 
	 */
	public static final String BRACKET_CLOSE = "]";

	/**
	 * 여는 중괄호({) 를 나타내는 값 
	 */
	public static final String BRACE_OPEN = "{";

	/**
	 * 닫는 중괄호(}) 를 나타내는 값 
	 */
	public static final String BRACE_CLOSE = "}";

	/**
	 * "" 값을 문자열로 나타내는 값
	 */
	public static final String STRING_EMPTY = "";

	/**
	 * 0 값을 문자열로 나타내는 값
	 */
	public static final String STRING_0 = "0";

	/**
	 * 1 값을 문자열로 나타내는 값
	 */
	public static final String STRING_1 = "1";

	/**
	 * 2 값을 문자열로 나타내는 값
	 */
	public static final String STRING_2 = "2";

	/**
	 * 3 값을 문자열로 나타내는 값
	 */
	public static final String STRING_3 = "3";

	/**
	 * null 문자열
	 */
	public static final String NULL = "null";

	/**
	 * Incident Id 문자열
	 */
	public static final String KEY_INCIDENT_ID = "incdntId";
	
	public static class DataSource {
		
		public static final String ORACLE_TX_MANAGER = "oracleTxManager";

		public static final String MARIA_TX_MANAGER = "mariaTxManager";
		
		public static final String VERTICA_TX_MANAGER = "verticaTxManager";

	}
	
	public static class SearchCondition {
		
		/**
		 * 다중 검색 : 시작 날짜
		 */
		public static final String DATE_FROM = "dateFrom";
		
		/**
		 * 다중 검색 : 종료 날짜
		 */
		public static final String DATE_TO = "dateTo";
		
		/**
		 * 다중 검색 : 날짜 기간
		 */
		public static final String DATE_LIST = "dateList";
		
		/**
		 * 기간 선택 : 시작 시간
		 */
		public static final String START_OCCUR_DATE = "startOccurDt";
		
		/**
		 * 기간 선택 : 종료 시간
		 */
		public static final String END_OCCUR_DATE = "endOccurDt";
	}

}
