package com.humansarehuman.blue2factor.test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.enums.DeviceClass;
import com.humansarehuman.blue2factor.entities.enums.OsClass;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

public class Test {

	public static void main(String[] args) {
		Test test = new Test();
		test.convert("PAYPALISHIRING", 3);
	}

	public String convert(String s, int numRows) {
		int length = (s.length() / numRows) + 1;
		int height = numRows;
		String[][] matrix = new String[length][height];
		int currRow = 0;
		int currCol = 0;
		boolean down = true;
		for (int i = 0; i < s.length(); i++) {
			matrix[currCol][currRow] = s.substring(i, i + 1);
			System.out.println("putting " + s.substring(i, i + 1) + " in " + currRow + "," + currCol);
			if (currRow == numRows - 1) {
				down = false;
			} else if (currRow == 0) {
				down = true;
			}
			if (down) {
				currRow++;
			} else {
				currRow--;
				currCol++;
			}
		}
		return readRows(matrix, length, height);
	}

	public String readRows(String[][] al, int len, int height) {
		String fullString = "";
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < len; j++) {
				if (al[i][j] != null) {
					fullString += al[i][j];
				}
			}
		}
		return fullString;
	}

	// Definition for singly-linked list.
	public class ListNode {
		int val;
		ListNode next;

		ListNode() {
		}

		ListNode(int val) {
			this.val = val;
		}

		ListNode(int val, ListNode next) {
			this.val = val;
			this.next = next;
		}
	}

	public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
		int currVal1;
		int currVal2;
		int currSum;
		int carryOver = 0;
		ArrayList<Integer> sumArray = new ArrayList<>();
		while (l1 != null || l2 != null || carryOver != 0) {
			if (l1 == null) {
				currVal1 = 0;
			} else {
				currVal1 = l1.val;
			}
			if (l2 == null) {
				currVal2 = 0;
			} else {
				currVal2 = l2.val;
			}
			currSum = currVal1 + currVal2 + carryOver;
			if (currSum > 9) {
				carryOver = 1;
				currSum = currSum - 10;
			} else {
				carryOver = 0;
			}
			sumArray.add(currSum);
			l1 = l1.next;
			l2 = l2.next;
		}

		return arrayToList(sumArray);
	}

	private ListNode arrayToList(List<Integer> sumArray) {
		int size = sumArray.size();
		ListNode fullList = new ListNode();
		for (int i = 0; i < size; i++) {
			int currListNodeVal = sumArray.get(size - 1 - i);
			fullList = new ListNode(currListNodeVal, fullList);
		}
		return fullList;
	}

	public boolean areBracketsBalanced(String exp) {
		boolean balanced = true;
		List<String> openParenz = new ArrayList<>();
		String prevParens;
		for (int i = 0; i < exp.length(); i++) {
			String nextLetter = exp.substring(i, i + 1);
			if (nextLetter.equals("(") || nextLetter.equals("{") || nextLetter.equals("[")) {
				openParenz.add(nextLetter);
			} else if (nextLetter.equals(")") || nextLetter.equals("}") || nextLetter.equals("]")) {
				prevParens = openParenz.remove(openParenz.size() - 1);
				if (prevParens.equals("(")) {
					if (!nextLetter.equals(")")) {
						balanced = false;
						break;
					}
				} else if (prevParens.equals("{")) {
					if (!nextLetter.equals("}")) {
						balanced = false;
						break;
					}
				} else if (prevParens.equals("[")) {
					if (!nextLetter.equals("]")) {
						balanced = false;
						break;
					}
				}
			}
		}
		if (balanced) {
			if (openParenz.size() != 0) {
				balanced = false;
			}
		}
		System.out.println("balanced: " + balanced);
		return balanced;
	}

	public int reverse(int x) {
		boolean neg = x < 0;
		String xStr = Integer.toString(x);
		if (neg) {
			xStr = xStr.substring(1);
		}
		char[] xCharArr = xStr.toCharArray();
		for (int i = 0; i < xCharArr.length / 2; i++) {
			char firstSwap = xCharArr[i];
			char lastSwap = xCharArr[xCharArr.length - 1 - i];
			xCharArr[i] = lastSwap;
			xCharArr[xCharArr.length - 1 - i] = firstSwap;
		}
		int resp = charArrayToInt(xCharArr, neg);
		System.out.println(resp);
		return resp;
	}

	int charArrayToInt(char[] charArr, boolean neg) {
		int resp;
		String charStr = new String(charArr);
		if (neg) {
			charStr = "-" + charStr;
		}
		try {
			resp = Integer.parseInt(charStr);
		} catch (Exception e) {
			resp = 0;
		}
		return resp;
	}

	List<List<Integer>> rsp = new ArrayList<>();
	HashMap<Integer, Boolean> map = new HashMap<>();
	List<String> successes = new ArrayList<>();

	public List<List<Integer>> combinationSum(int[] candidates, int target) {
		combo(candidates, target, new ArrayList<Integer>());
		return rsp;
	}

	public boolean combo(int[] candidates, int target, List<Integer> intList) {
		String sList = intListToStr(intList);
		System.out.println("looking at: " + intList);
		boolean success = false;
		if (target >= 0) {
			if (target == 0) {
				System.out.println("winner");
				if (!successes.contains(sList)) {
					rsp.add(intList);
					successes.add(sList);
				}
			} else {
				Boolean oldSuccess = map.get(target);
				if (oldSuccess == null) {
					boolean anySuccess = false;
					for (int i = 0; i < candidates.length; i++) {
						List<Integer> newIntList = new ArrayList<Integer>(intList);
						int candidate = candidates[i];
						newIntList.add(candidate);
						success = combo(candidates, target - candidate, newIntList);
						if (success) {
							anySuccess = true;
						}
					}
					map.put(target, anySuccess);
				} else {
					System.out.println("bailing on " + target);
				}
			}
		} else {
			success = false;
		}

		return success;
	}

	String intListToStr(List<Integer> ls) {
		Collections.sort(ls);
		String s = "";
		for (int i = 0; i < ls.size(); i++) {
			s += ls.get(i).toString();
		}
		return s;
	}

	List<String> res = new ArrayList<String>();

//    public List<String> letterCombinations2(String digits) {
//        if (digits.length() == 0) {
//            return res;
//        }
//        HashMap<Integer, List<Character>> data = new HashMap<Integer, List<Character>>();
//        char[] keys = digits.toCharArray();
//        int[] checker = new int[digits.length()];
//
//        for (char letter : keys) {
//            if (!data.containsKey(letter)) {
//                getChar(Character.getNumericValue(letter), data);
//            }
//        }
//
//        pair(keys, data, "", 0);
//        return res;
//    }

	public void pair(char[] keys, HashMap<Integer, List<Character>> data, String s, int ki) {
		List<Character> chars = data.get(Character.getNumericValue(keys[ki]));
		int length = chars.size();
		for (int i = 0; i < length; i++) {
			if (ki == keys.length - 1) {
				res.add(s + chars.get(i));
			} else {
				pair(keys, data, s + chars.get(i), ki + 1);
			}
		}
	}

	public void getChar(int x, HashMap<Integer, List<Character>> data) {
		String alpha = "abcdefghijklmnopqrstuvwxyz";
		data.put(x, new ArrayList<Character>());

		int bound = x == 7 || x == 9 ? 4 : 3;
		int ex = x >= 8 ? 1 : 0;
		for (int i = 0; i < bound; i++) {
			int start = (x - 2) * 3 + ex;
			data.get(x).add(alpha.charAt(start + i));
		}
	}

	public List<String> letterCombinations(String digits) {
		HashMap<String, String> map = new HashMap<>();
		map.put("2", "abc");
		map.put("3", "def");
		map.put("4", "ghi");
		map.put("5", "jkl");
		map.put("6", "mno");
		map.put("7", "pqrs");
		map.put("8", "tuv");
		map.put("9", "wxyz");
		addLetter(map, "", digits);
		return res;
	}

	void addLetter(HashMap<String, String> map, String currStr, String letters) {
		System.out.println("currStr: " + currStr);
		if (letters.length() == 0) {
			res.add(currStr);
		} else {
			String currLetters = map.get(letters.substring(0, 1));
			int letterLen = currLetters.length();
			for (int i = 0; i < letterLen; i++) {
				String sub = currLetters.substring(i, i + 1);
				System.out.println("adding " + sub);
				addLetter(map, currStr + sub, letters.substring(1));
			}
		}
	}

	HashMap<String, Boolean> alreadyTried = new HashMap<>();

	public String longestPalindrome(String s) {
		int longestLen = 0;
		String longest = "";
		char[] charArray = s.toCharArray();
		int fullLen = charArray.length;
		for (int i = 0; i < fullLen; i++) {
			int currLen = charArray.length - i;
			if (currLen > longestLen) {
				for (int j = 0; j <= fullLen - currLen; j++) {
					if (currLen > longestLen) {
						char[] next = Arrays.copyOfRange(charArray, j, j + currLen);
						String nextStr = new String(next);
						System.out.println("looking at " + nextStr);
						Boolean tried = alreadyTried.get(nextStr);
						if (tried == null) {
							if (isPalindrome(next)) {
								System.out.println("new longest " + new String(next));
								longest = new String(next);
								longestLen = next.length;
							}
						} else {
							break;
						}
					} else {
						break;
					}
				}
			} else {
				break;
			}
		}
		System.out.println("longest: " + longest);
		return longest;
	}

	public String longestPalindrome2(String s) {
		int longestLen = 0;
		String longest = "";
		char[] charArray = s.toCharArray();
		int fullLen = charArray.length;
		for (int i = 0; i < fullLen; i++) {
			int currLen = fullLen - i;
			if (currLen > longestLen) {
				for (int j = i + 1; j <= fullLen; j++) {
					if (j - i > longestLen) {
						char[] next = Arrays.copyOfRange(charArray, i, j);
//                          System.out.println("looking at " + new String(next));
						if (isPalindrome(next)) {
							System.out.println("new longest " + new String(next));
							longest = new String(next);
							longestLen = next.length;
						}
					}
				}
			} else {
				break;
			}
		}
		System.out.println("longest: " + longest);
		return longest;
	}

	public boolean isPalindrome(char[] charArray) {
		String charStr = new String(charArray);
		boolean success = false;
		Boolean tried = alreadyTried.get(charStr);
		if (tried == null) {
			if (charArray.length <= 1) {
				success = true;
			} else {
				if (charArray[0] == charArray[charArray.length - 1]) {
					char[] nextArray = Arrays.copyOfRange(charArray, 1, charArray.length - 1);
					if (isPalindrome(nextArray)) {
						success = true;
					}
				} else {
					success = false;
				}
			}
			alreadyTried.put(charStr, success);
		} else {
			System.out.println("skipping " + charStr);
			success = tried;
		}
		return success;
	}

	public List<List<String>> groupAnagrams(String[] strs) {
		HashMap<String, ArrayList<String>> map = new HashMap<>();
		for (int i = 0; i < strs.length; i++) {
			String str = alphabetize(strs[i]);
			ArrayList<String> foundVal = map.get(str);
			if (foundVal == null) {
				foundVal = new ArrayList<>();
				System.out.println("adding: " + strs[i]);
				foundVal.add(strs[i]);
				map.put(str, foundVal);
			} else {
				System.out.println("updating to add: " + strs[i]);
				foundVal.add(strs[i]);
				map.put(str, foundVal);
			}
		}
		List<List<String>> list = new ArrayList<List<String>>(map.values());
		return list;
	}

	String alphabetize(String currStr) {
		char[] charArr = currStr.toCharArray();
		Arrays.sort(charArr);
		return new String(charArr);
	}

	public List<String> getAnagrams(String currStr) {
		for (int i = 0; i < currStr.length(); i++) {

		}
		return null;
	}

	public boolean isAlreadyCompleted(String str, List<List<String>> output) {
		boolean alreadyCompleted = false;
		for (int i = 0; i < output.size(); i++) {
			List<String> currList = output.get(i);
			for (int j = 0; j < currList.size(); j++) {
				if (currList.get(j).equals(str)) {
					alreadyCompleted = true;
					break;
				}
			}
		}
		return alreadyCompleted;
	}

	public int myAtoi(String s) {
		int resp = 0;
		s = s.trim();
		if (s.length() > 0) {
			s = getNumber(s);
			int len = s.length();
			if (len > 0) {
				if (len < 12) {
					if (isNumeric(s)) {
						long lg = Long.valueOf(s);
						if (lg > Integer.MAX_VALUE) {
							resp = Integer.MAX_VALUE;
						} else if (lg < Integer.MIN_VALUE) {
							resp = Integer.MIN_VALUE;
						} else {
							resp = (int) lg;
						}
					}
				} else {
					if (s.startsWith("-")) {
						resp = Integer.MIN_VALUE;
					} else {
						resp = Integer.MAX_VALUE;
					}
				}
			}
		}
		return resp;
	}

	private String getNumber(String s) {
		String dig = "";
		try {

			if (s.startsWith("-")) {

			}
			String[] digArr = s.split("[^-?\\d]");
			if (digArr.length > 0) {
				dig = removeLeadingZeroes(digArr[0]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dig;
	}

	private String removeLeadingZeroes(String num) {
		return num.replaceAll("^0+", "");
	}

	private boolean isNumeric(String s) {
		return s.matches("^[-?\\d].*");
		// Matcher matcher = pattern.matcher(INPUT);
		// return matcher.matches();
	}

	List<String> sentences = new ArrayList<>();

	public List<String> wordBreak(String s, List<String> wordDict) {
		boolean[] failureArray = new boolean[s.length() + 1];
		wordBreak2("", s, wordDict, failureArray);
		return sentences;
	}

	public String wordBreak2(String incomingWords, String s, List<String> wordDict, boolean[] failureArray) {
		// if a word is found, chop it off an run wordBreak on the new s
		// keep track of the ends that already failed
		String sSub;
		String currWord;
		String newWords = null;

//        if (!(failureArray[s.length()])) {
		for (int i = 0; i < wordDict.size(); i++) {
			currWord = wordDict.get(i);
			if (s.equals(currWord)) {
				newWords = addWord(incomingWords, currWord);
				System.out.println(newWords);
				sentences.add(newWords);
			} else {
				if (s.startsWith(currWord)) {
					newWords = addWord(incomingWords, currWord);
					sSub = s.substring(currWord.length());
					newWords = wordBreak2(newWords, sSub, wordDict, failureArray);
				}
			}
		}
		if (newWords == null) {
			System.out.println("incoming: " + incomingWords);
			System.out.println("failed with " + s.length() + " letters left");
//                failureArray[s.length()] = true;
		}
//        }
		return newWords;
	}

	String addWord(String incomingWords, String newWord) {
		if (!incomingWords.equals("")) {
			incomingWords += " ";
		}
		return incomingWords + newWord;
	}

	public boolean[] failureArray;

	public boolean wordBreak1(String s, List<String> wordDict) {
		failureArray = new boolean[s.length() + 1];
		return wordBreak2(s, wordDict);
	}

	public boolean wordBreak2(String s, List<String> wordDict) {
		// if a word is found, chop it off an run wordBreak on the new s
		// keep track of the ends that already failed
		String.join("", wordDict);
		String sSub;
		String currWord;
		boolean success = false;

		if (!alreadyFailedFromHere(failureArray, s.length())) {
			for (int i = 0; i < wordDict.size(); i++) {
				currWord = wordDict.get(i);
				if (s.equals(currWord)) {
					success = true;
					break;
				} else {
					if (s.startsWith(currWord)) {
						sSub = s.substring(currWord.length());
						success = wordBreak2(sSub, wordDict);
						if (success) {
							break;
						}
					}
				}
			}
			if (!success) {
				failureArray[s.length()] = true;
			}
		}

		return success;
	}

	public boolean alreadyFailedFromHere(boolean[] failure, int lettersLeft) {
		return failure[lettersLeft];
	}

	public String multiply(String num1, String num2) {
		long num1N = stringToInt(num1);
		long num2N = stringToInt(num2);
		System.out.println(String.valueOf(num1N * num2N));
		return String.valueOf(num1N * num2N);

	}

	public long stringToInt(String num) {
		long runningTally = 0;
		long place;
		long currNum;
		long len = num.length();
		for (int i = 0; i < len; i++) {
			place = (long) Math.pow(10, len - i - 1);
			currNum = Integer.parseInt(num.substring(i, i + 1)) * place;
			runningTally += currNum;
		}

		return runningTally;
	}

	public String getHint(String secret, String guess) {
		int bulls = 0;
		int cows = 0;
		String currSecret;
		for (int i = 0; i < secret.length(); i++) {
			currSecret = secret.substring(i, i + 1);
			if (guess.length() > 1) {
				if (currSecret.equals(guess.substring(i, i + 1))) {
					secret = replace(secret, i);
					bulls++;
				}
			}
		}
		for (int i = 0; i < secret.length(); i++) {
			currSecret = secret.substring(i, i + 1);
			if (!currSecret.equals("x")) {
				int idx = guess.indexOf(currSecret);
				if (idx != -1) {
					guess = replace(guess, idx);
					cows++;
				}
			}
		}
		System.out.println(bulls + "A" + cows + "B");
		return bulls + "A" + cows + "B";
	}

	String replace(String str, int idx) {
		return str.substring(0, idx) + "x" + str.substring(idx + 1);
	}

	public int firstMissingPositive(int[] nums) {
		int[] trackArray = new int[nums.length + 1];
		int len = nums.length;
		for (int i = 0; i < len; i++) {
			if (nums[i] > 0 && nums[i] <= len) {
				trackArray[nums[i]] = 1;
			}
		}
		int j = 0;
		for (j = 1; j < len + 1; j++) {
			if (trackArray[j] == 0) {
				break;
			}
		}
		return j;
	}

	public int divide(long dividend, long divisor) {
		boolean isNegative = isNegative(dividend, divisor);
		if (dividend < 0) {
			dividend = 0 - dividend;
		}
		if (divisor < 0) {
			divisor = 0 - divisor;
		}
		int i = 0;
		int mySum = 0;
		int answer = 0;
		if (divisor <= dividend) {
			while (mySum <= dividend) {
				mySum += divisor;
				i++;
				if (mySum % 10000 == 0) {
					System.out.println(mySum);
				}
			}
			answer = i - 1;
		}
		if (isNegative) {
			answer = 0 - answer;
		}
		return answer;
	}

	boolean isNegative(long dividend, long divisor) {
		int multiplier1 = 1;
		int multiplier2 = 1;
		if (dividend < 0) {
			multiplier1 = -1;
		}
		if (divisor < 0) {
			multiplier2 = -1;
		}
		int sum = multiplier1 + multiplier2;
		return sum == 0;
	}

	public void method1() {
		System.out.println("1: " + DataAccess.getMethodName());
		method2();
	}

	public void method2() {
		System.out.println("2: " + DataAccess.getMethodName());
		method3();
	}

	public void method3() {
		System.out.println("3: " + DataAccess.getMethodName());
	}

	public String getTest() {
		Test test = new Test();
		test.addTestData();
		;
		System.out.println("done");
		return "result";
	}

	@SuppressWarnings("unused")
	private void showRandomStrings() {
		String rand1 = GeneralUtilities.randomString();
		String rand2 = GeneralUtilities.randomString();
		System.out.println(rand1);
		System.out.println(rand2);
	}

	// @SuppressWarnings("unused")
	public void addTestData() {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		int seed = GeneralUtilities.randInt(0, 10000000);
		Timestamp ts = DateTimeUtilities.getCurrentTimestamp();
		DeviceDbObj device = new DeviceDbObj("8JIZJNtflFTPQfHAi3cBSzMeIBHjGLwugYNBHYar",
				"eHDjf6pngBMeKZ8CCOszghGRsMMTxYgk62ZIWdBv", "OKgcZY2kMobCzKVd89pQqms0FlFewYxsIram7HAB", seed, true, "",
				"", new Date(), DateTimeUtilities.getCurrentTimestamp(), "", OsClass.OSX, "C02V81MKHV2H", new Date(),
				-18000, "10_14_6", "en-US", "1440900%4019201200", "", true, 650.0, false, 0, false, ts, false, false,
				"", "", false, ts, false, ts, false, DeviceClass.COMPUTER, false, true, ts, null, true, false, null,
				false, true, null, false);

		dataAccess.addDevice(device);

	}

	@SuppressWarnings("unused")
	private void testEncryption() {
		String randString = "";
		String encrypted = "";
		String decrypted = "";
		int success = 0;
		int MAX_TRIES = 5000000;
		Encryption encryption = new Encryption();
		for (int i = 0; i < MAX_TRIES; i++) {
			randString = GeneralUtilities.randomStringWithSymbols(40);
			encrypted = encryption.b2fEncrypt(randString);
			decrypted = encryption.b2fDecrypt(encrypted);
			if (!randString.equals(decrypted)) {
				System.out.println(randString + " != ");
				System.out.println(decrypted);
			} else {
				success++;
			}
			if (i % 10000 == 0) {
				System.out.println(i);
			}
		}
		System.out.println("successes: " + success + "/" + MAX_TRIES);
	}

}
